package co.unicauca.edu.co.contables.configuration.enterprise.presentation.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.ILogoStorageService;
import lombok.RequiredArgsConstructor;

/**
 * @brief Controlador REST para la pre-carga del logo de una empresa.
 */
@RestController
@RequestMapping("/api/enterprises/upload/logo")
@RequiredArgsConstructor
public class EnterprisePreUploadLogoController {

    private final ILogoStorageService logoStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> preUploadLogo(@RequestPart("file") MultipartFile file) {
        String logoKey = logoStorageService.save(file);
        return ResponseEntity.ok(Map.of("url", "/api/enterprises/logos/" + logoKey));
    }
}
