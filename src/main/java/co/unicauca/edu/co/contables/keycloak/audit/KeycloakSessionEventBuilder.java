package co.unicauca.edu.co.contables.keycloak.audit;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 * Construye eventos de sesión (login/logout) a partir del JWT y la request.
 * <p>
 * Equivalente al {@code SessionEventBuilder} del origen. Diferencias de
 * migración: publica el {@code SessionEventDto} del módulo audit (campos
 * idénticos al {@code SessionEventDTO} del origen) y usa la API de jjwt
 * 0.9.1 que trae el monolito ({@code setSigningKey/parseClaimsJws/getBody}),
 * en lugar de la API 0.12.x ({@code verifyWith/parseSignedClaims/getPayload})
 * del origen.
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakSessionEventBuilder {

    private final KeycloakIpAddressUtil ipAddressUtil;

    @Value("${jwt.public.key}")
    private String publicKeyString;

    public SessionEventDto buildLoginEvent(String jwtToken, HttpServletRequest request) {
        return buildEvent(jwtToken, request, "LOGIN");
    }

    public SessionEventDto buildLogoutEvent(String jwtToken, HttpServletRequest request) {
        return buildEvent(jwtToken, request, "LOGOUT");
    }

    private SessionEventDto buildEvent(String jwtToken, HttpServletRequest request, String action) {
        try {
            PublicKey publicKey = getPublicKey(publicKeyString);
            Claims claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .parseClaimsJws(jwtToken)
                    .getBody();

            String userId = claims.getSubject();
            String username = (String) claims.get("preferred_username");
            if (username == null) {
                username = (String) claims.get("email");
            }

            List<String> roles = extractRoles(claims);

            String sessionId = (String) claims.get("sid");
            if (sessionId == null) {
                sessionId = (String) claims.get("session_state");
            }

            return SessionEventDto.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .userName(username)
                    .userRole(roles)
                    .action(action)
                    .actionAt(Instant.now())
                    .ipAddress(ipAddressUtil.getClientIpAddress(request))
                    .build();

        } catch (Exception e) {
            log.error("Error building session event", e);
            return fallbackEvent(request, action);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Claims claims) {

        Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");

        if (realmAccess == null) {
            return List.of();
        }

        Object rolesObj = realmAccess.get("roles");

        if (!(rolesObj instanceof List<?> rolesList)) {
            return List.of();
        }

        return rolesList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(r -> !r.equals("offline_access"))
                .filter(r -> !r.startsWith("default-roles-"))
                .filter(r -> !r.equals("uma_authorization"))
                .toList();
    }

    private SessionEventDto fallbackEvent(HttpServletRequest request, String action) {
        return SessionEventDto.builder()
                .action(action)
                .actionAt(Instant.now())
                .ipAddress(ipAddressUtil.getClientIpAddress(request))
                .build();
    }

    private PublicKey getPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
