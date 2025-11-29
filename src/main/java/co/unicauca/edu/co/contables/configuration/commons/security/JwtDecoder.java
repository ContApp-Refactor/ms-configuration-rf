package co.unicauca.edu.co.contables.configuration.commons.security;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

/** @brief Componente para decodificar tokens JWT y extraer información específica */
@Component
@Slf4j
public class JwtDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extrae el tenant ID del token JWT.
     * Decodifica el payload del JWT y extrae el claim "sub" que contiene el tenant ID.
     * 
     * @param jwtToken el token JWT como string
     * @return el tenant ID extraído del token, o null si no se puede extraer
     */
    public String extractTenantId(String jwtToken) {
        try {
            // Remover el prefijo "Bearer " si existe
            String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
            
            // Un JWT tiene 3 partes separadas por puntos: header.payload.signature
            String[] chunks = token.split("\\.");
            
            if (chunks.length != 3) {
                log.error("Token JWT inválido: no tiene el formato correcto");
                return null;
            }
            
            // Decodificar el payload (segunda parte)
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            
            // Parsear el JSON del payload
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            // Extraer el claim "sub" que contiene el tenant ID
            JsonNode subNode = jsonNode.get("sub");
            if (subNode != null) {
                String tenantId = subNode.asText();
                log.debug("Tenant ID extraído del JWT: {}", tenantId);
                return tenantId;
            } else {
                log.warn("No se encontró el claim 'sub' en el token JWT");
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error al decodificar el token JWT: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extrae cualquier claim del token JWT.
     * 
     * @param jwtToken el token JWT como string
     * @param claimName nombre del claim a extraer
     * @return el valor del claim como string, o null si no se puede extraer
     */
    public String extractClaim(String jwtToken, String claimName) {
        try {
            // Remover el prefijo "Bearer " si existe
            String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
            
            // Un JWT tiene 3 partes separadas por puntos: header.payload.signature
            String[] chunks = token.split("\\.");
            
            if (chunks.length != 3) {
                log.error("Token JWT inválido: no tiene el formato correcto");
                return null;
            }
            
            // Decodificar el payload (segunda parte)
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            
            // Parsear el JSON del payload
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            // Extraer el claim solicitado
            JsonNode claimNode = jsonNode.get(claimName);
            if (claimNode != null) {
                String claimValue = claimNode.asText();
                log.debug("Claim '{}' extraído del JWT: {}", claimName, claimValue);
                return claimValue;
            } else {
                log.warn("No se encontró el claim '{}' en el token JWT", claimName);
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error al extraer el claim '{}' del token JWT: {}", claimName, e.getMessage(), e);
            return null;
        }
    }
}
