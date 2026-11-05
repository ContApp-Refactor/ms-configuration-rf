package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.ImportConstants;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.FileValidationException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.FileSizeExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @brief Servicio para validación de archivos de importación
 *
 * Servicio para validación directa de archivos de importación.
 * Valida tamaño, extensión y contenido básico del archivo.
 */
@Slf4j
@Service
public class AccountCatalogueFileValidationService {

    /**
     * Valida el archivo de importación.
     * 
     * @param file archivo a validar
     * @throws FileValidationException si el archivo no es válido
     * @throws FileSizeExceededException si el archivo excede el tamaño máximo
     */
    public void validate(MultipartFile file) {
        validateNotNull(file);
        validateNotEmpty(file);
        validateSize(file);
        validateExtension(file);
    }

    /**
     * @brief Valida que archivo no sea null
     * @param file archivo a validar
     */
    private void validateNotNull(MultipartFile file) {
        if (file == null) {
            throw FileValidationException.forNullFile();
        }
    }

    /**
     * @brief Valida que archivo no esté vacío
     * @param file archivo a validar
     */
    private void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty() || file.getSize() == 0) {
            throw FileValidationException.forEmptyFile(file.getOriginalFilename());
        }
    }

    /**
     * @brief Valida tamaño máximo del archivo
     * @param file archivo a validar
     */
    private void validateSize(MultipartFile file) {
        if (file.getSize() > ImportConstants.MAX_FILE_SIZE) {
            throw new FileSizeExceededException(ImportConstants.MAX_FILE_SIZE);
        }
    }

    /**
     * @brief Valida extensión de archivo Excel
     * @param file archivo a validar
     */
    private void validateExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw FileValidationException.forInvalidExtension(
                "archivo sin nombre", 
                ImportConstants.SUPPORTED_EXTENSIONS
            );
        }

        String lowerFilename = filename.toLowerCase();
        boolean validExtension = false;

        for (String ext : ImportConstants.SUPPORTED_EXTENSIONS) {
            if (lowerFilename.endsWith(ext)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            throw FileValidationException.forInvalidExtension(
                filename, 
                ImportConstants.SUPPORTED_EXTENSIONS
            );
        }
    }
}

