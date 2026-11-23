package co.unicauca.edu.co.contables.financial.infrastructure.in.rest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisualSignatureRequest {
    private String fileName;
    private String contentType;
    private String base64Content;
    private String signerName;
    private String signerRole;
}