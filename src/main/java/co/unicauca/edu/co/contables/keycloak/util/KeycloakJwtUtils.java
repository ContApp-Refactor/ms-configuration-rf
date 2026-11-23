package co.unicauca.edu.co.contables.keycloak.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utilidades para leer claims de un JWT sin verificar la firma.
 * <p>
 * El monolito usa jjwt 0.9.1, que exige una clave para cualquier
 * {@code parseClaimsJws} (sin clave lanza {@link IllegalArgumentException});
 * los consumidores de esta clase (blacklist de JWT y logout) solo necesitan
 * leer {@code jti}, {@code sub} y {@code exp}. Por eso el payload se decodifica
 * directamente en Base64URL y se mapea con Jackson.
 * </p>
 * <p>
 * Los tokens que llegan a la cadena de seguridad ya son validados por
 * {@code oauth2-resource-server} (firma e issuer); leer claims acá sin
 * re-verificar es seguro porque un payload forjado solo puede producir un
 * falso positivo de blacklist (401 inofensivo) o caer en la cadena de
 * seguridad, que rechaza la firma inválida.
 * </p>
 * <p>
 * Semántica preservada del origen: token malformado devuelve {@code null} en
 * {@link #getJti(String)} / {@link #getSub(String)} y {@code 0} en
 * {@link #getTtlSeconds(String)}.
 * </p>
 */
public final class KeycloakJwtUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private KeycloakJwtUtils() {
    }

    /**
     * Extrae el {@code jti} (id del token) del payload.
     *
     * @param token JWT completo en formato {@code header.payload.signature}.
     * @return el {@code jti}, o {@code null} si el token es nulo, malformado
     *         o no contiene el claim.
     */
    public static String getJti(String token) {
        JsonNode claims = parsePayload(token);
        return claims != null && claims.hasNonNull("jti") ? claims.get("jti").asText() : null;
    }

    /**
     * Extrae el {@code sub} (sujeto, usualmente el id del usuario) del payload.
     *
     * @param token JWT completo en formato {@code header.payload.signature}.
     * @return el {@code sub}, o {@code null} si el token es nulo, malformado
     *         o no contiene el claim.
     */
    public static String getSub(String token) {
        JsonNode claims = parsePayload(token);
        return claims != null && claims.hasNonNull("sub") ? claims.get("sub").asText() : null;
    }

    /**
     * Calcula los segundos restantes de vida del token a partir del {@code exp}.
     *
     * @param token JWT completo en formato {@code header.payload.signature}.
     * @return segundos restantes, nunca negativos; {@code 0} si el token es
     *         nulo, malformado o no tiene {@code exp}.
     */
    public static long getTtlSeconds(String token) {
        JsonNode claims = parsePayload(token);
        if (claims == null || !claims.hasNonNull("exp")) {
            return 0L;
        }
        long now = System.currentTimeMillis();
        long expMs = claims.get("exp").asLong() * 1000L;
        long ttlMs = Math.max(0L, expMs - now);
        return ttlMs / 1000L;
    }

    /**
     * Decodifica el payload (segmento central) del JWT como JSON.
     *
     * @param token JWT completo.
     * @return el nodo JSON del payload, o {@code null} si no se pudo parsear.
     */
    private static JsonNode parsePayload(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        try {
            byte[] decoded = DECODER.decode(parts[1]);
            return MAPPER.readTree(new String(decoded, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | IOException e) {
            return null;
        }
    }
}
