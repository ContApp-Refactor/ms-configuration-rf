package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.io.InputStream;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * @brief Puerto de salida para el almacenamiento de logos de empresa.
 */
public interface ILogoStorageService {

    /** Prefijo de la URL pública que se guarda en {@code enterprise.logo}. */
    String PUBLIC_URL_PREFIX = "/api/enterprises/logos/";

    String save(MultipartFile file);

    Resource load(String logoKey);

    void delete(String logoKey);

    /**
     * Abre un logo existente para copiarlo (backup). Vacío si la clave no es
     * válida o el archivo no existe. El llamador cierra {@link LogoContent#content()}.
     */
    Optional<LogoContent> open(String logoKey);

    /**
     * Guarda un logo con una clave NUEVA validando tipo y tamaño. Si hay una
     * transacción activa el archivo se escribe recién en {@code afterCommit}
     * (los bytes se retienen en memoria, acotados por {@code app.storage.max-size-mb}),
     * así un rollback no deja archivos huérfanos. Sin transacción se escribe ya.
     *
     * @return la clave nueva (usar {@link #PUBLIC_URL_PREFIX} + clave como URL)
     */
    String saveAfterCommit(InputStream content, String contentType);

    /** Clave de un logo local a partir de la URL guardada en la empresa, o {@code null} si no es local. */
    static String keyFromUrl(String logoUrl) {
        if (logoUrl == null || !logoUrl.startsWith(PUBLIC_URL_PREFIX)) {
            return null;
        }
        String key = logoUrl.substring(PUBLIC_URL_PREFIX.length());
        return key.isBlank() ? null : key;
    }

    /** Contenido de un logo: stream abierto, tipo detectado y tamaño en bytes. */
    record LogoContent(InputStream content, String contentType, long sizeBytes) {
    }
}
