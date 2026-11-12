package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @brief Implementación local (disco) del almacenamiento de logos de empresa.
 */
@Service
@Slf4j
public class LocalLogoStorageServiceImpl implements ILogoStorageService {

    /** Claves generadas por este servicio (UUID); evita path traversal al abrir. */
    private static final Pattern SAFE_KEY = Pattern.compile("^[A-Za-z0-9-]{1,64}$");

    @Value("${app.storage.logo-dir:uploads/enterprises/logos}")
    private String logoDir;

    @Value("${app.storage.max-size-mb:2}")
    private long maxSizeMb;

    @Override
    public String save(MultipartFile file) {
        validateFile(file);

        try {
            Path dir = Paths.get(logoDir);
            Files.createDirectories(dir);

            String logoKey = UUID.randomUUID().toString();
            Path target = dir.resolve(logoKey);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return logoKey;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el logo", e);
        }
    }

    @Override
    public Resource load(String logoKey) {
        try {
            Path file = Paths.get(logoDir).resolve(logoKey);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("Logo no encontrado: " + logoKey);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al cargar el logo", e);
        }
    }

    @Override
    public void delete(String logoKey) {
        try {
            Path file = Paths.get(logoDir).resolve(logoKey);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el logo", e);
        }
    }

    @Override
    public Optional<LogoContent> open(String logoKey) {
        Path file = resolveSafe(logoKey);
        if (file == null || !Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            long size = Files.size(file);
            String contentType = detectContentType(file);
            if (contentType == null) {
                log.warn("El logo '{}' no es PNG/JPEG/SVG; no se incluye en la copia", logoKey);
                return Optional.empty();
            }
            return Optional.of(new LogoContent(Files.newInputStream(file), contentType, size));
        } catch (IOException e) {
            log.warn("No se pudo leer el logo '{}' para la copia", logoKey, e);
            return Optional.empty();
        }
    }

    @Override
    public String saveAfterCommit(InputStream content, String contentType) {
        if (contentType == null || !isValidContentType(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de logo no permitido en la copia. Solo se permiten imágenes PNG, JPG, JPEG, SVG");
        }
        byte[] bytes = readBounded(content);
        if (bytes.length == 0) {
            throw new IllegalArgumentException("El logo de la copia está vacío");
        }
        String logoKey = UUID.randomUUID().toString();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    writeQuietly(logoKey, bytes);
                }
            });
        } else {
            write(logoKey, bytes);
        }
        return logoKey;
    }

    private byte[] readBounded(InputStream content) {
        long max = maxSizeMb * 1024 * 1024;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        try {
            int n;
            while ((n = content.read(buf)) != -1) {
                if (out.size() + (long) n > max) {
                    throw new IllegalArgumentException(
                            "El logo de la copia es demasiado grande. Tamaño máximo: " + maxSizeMb + " MB");
                }
                out.write(buf, 0, n);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el logo de la copia", e);
        }
        return out.toByteArray();
    }

    private void write(String logoKey, byte[] bytes) {
        try {
            Path dir = Paths.get(logoDir);
            Files.createDirectories(dir);
            Files.write(dir.resolve(logoKey), bytes);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el logo", e);
        }
    }

    /** En afterCommit la BD ya confirmó: un fallo de disco solo deja la empresa sin logo visible. */
    private void writeQuietly(String logoKey, byte[] bytes) {
        try {
            write(logoKey, bytes);
        } catch (RuntimeException e) {
            log.error("La empresa se restauró pero no se pudo escribir su logo '{}'", logoKey, e);
        }
    }

    private Path resolveSafe(String logoKey) {
        if (logoKey == null || !SAFE_KEY.matcher(logoKey).matches()) {
            return null;
        }
        Path dir = Paths.get(logoDir).toAbsolutePath().normalize();
        Path file = dir.resolve(logoKey).normalize();
        return file.startsWith(dir) ? file : null;
    }

    /** Tipo por "magic bytes": los logos se guardan sin extensión y probeContentType no sirve en Windows. */
    static String detectContentType(Path file) throws IOException {
        byte[] head;
        try (InputStream in = Files.newInputStream(file)) {
            head = in.readNBytes(512);
        }
        if (head.length >= 8 && (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G') {
            return "image/png";
        }
        if (head.length >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        String text = new String(head, StandardCharsets.UTF_8).toLowerCase();
        if (text.contains("<svg")) {
            return "image/svg+xml";
        }
        return null;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidContentType(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Solo se permiten imágenes PNG, JPG, JPEG, SVG");
        }

        long maxSizeBytes = maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("El archivo es demasiado grande. Tamaño máximo: " + maxSizeMb + " MB");
        }
    }

    private boolean isValidContentType(String contentType) {
        return contentType.equals("image/png") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/jpeg") ||
                contentType.equals("image/svg+xml");
    }
}
