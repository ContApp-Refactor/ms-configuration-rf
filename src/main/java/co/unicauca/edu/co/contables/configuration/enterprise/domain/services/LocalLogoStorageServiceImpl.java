package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * @brief Implementación local (disco) del almacenamiento de logos de empresa.
 */
@Service
public class LocalLogoStorageServiceImpl implements ILogoStorageService {

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
