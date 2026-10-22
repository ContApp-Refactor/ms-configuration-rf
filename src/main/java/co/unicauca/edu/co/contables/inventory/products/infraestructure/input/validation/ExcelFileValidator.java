package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.validation;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductFileSizeExceededException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductFileValidationException;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.FileUploadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @brief Validador especializado para archivos Excel
 *
 * Implementa validaciones específicas de archivos Excel incluyendo
 * tamaño máximo, extensiones permitidas y tipos MIME válidos.
 */
@Component("productsExcelFileValidator")
@RequiredArgsConstructor
public class ExcelFileValidator implements FileValidator {

    private final FileUploadProperties fileProperties;

    @Override
    public void validate(MultipartFile file) {
        validateNotNull(file);
        validateNotEmpty(file);
        validateSize(file);
        validateExtension(file);
    }

    /**
     * @brief Valida que el archivo no sea nulo
     * @param file archivo a validar
     */
    private void validateNotNull(MultipartFile file) {
        if (file == null) {
            throw ProductFileValidationException.forNullFile();
        }
    }

    /**
     * @brief Valida que el archivo no esté vacío
     * @param file archivo a validar
     */
    private void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty() || file.getSize() == 0) {
            throw ProductFileValidationException.forEmptyFile(file.getOriginalFilename());
        }
    }

    /**
     * @brief Valida tamaño máximo del archivo Excel
     * @param file archivo a validar
     */
    private void validateSize(MultipartFile file) {
        if (file.getSize() > fileProperties.getMaxSize()) {
            throw new ProductFileSizeExceededException(fileProperties.getMaxSize());
        }
    }

    /**
     * @brief Valida extensión del archivo Excel
     * @param file archivo a validar
     */
    private void validateExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw ProductFileValidationException.forInvalidExtension("archivo sin nombre", getSupportedExtensions());
        }

        String lowerFilename = filename.toLowerCase();
        boolean validExtension = false;

        for (String ext : getSupportedExtensions()) {
            if (lowerFilename.endsWith(ext)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            throw ProductFileValidationException.forInvalidExtension(filename, getSupportedExtensions());
        }
    }

    @Override
    public String[] getSupportedMimeTypes() {
        List<String> mimeTypes = fileProperties.getAllowedMimeTypes().get("excel");
        return mimeTypes != null ? mimeTypes.toArray(new String[0]) : new String[0];
    }

    @Override
    public String[] getSupportedExtensions() {
        List<String> extensions = fileProperties.getAllowedExtensions().get("excel");
        return extensions != null ? extensions.toArray(new String[0]) : new String[0];
    }
}