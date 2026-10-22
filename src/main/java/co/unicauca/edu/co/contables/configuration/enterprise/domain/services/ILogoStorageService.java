package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * @brief Puerto de salida para el almacenamiento de logos de empresa.
 */
public interface ILogoStorageService {

    String save(MultipartFile file);

    Resource load(String logoKey);

    void delete(String logoKey);
}
