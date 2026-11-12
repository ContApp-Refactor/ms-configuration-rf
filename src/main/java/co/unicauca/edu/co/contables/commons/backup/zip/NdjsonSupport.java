package co.unicauca.edu.co.contables.commons.backup.zip;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * @brief Utilidades del formato ZIP v2: configuración de Jackson para NDJSON,
 * SHA-256 y validación de nombres de entrada (anti zip-slip).
 */
public final class NdjsonSupport {

    /** Segmentos {@code [A-Za-z0-9_.-]+} separados por {@code /}; sin barra inicial ni final. */
    private static final Pattern SAFE_PATH = Pattern.compile("^[A-Za-z0-9_.\\-]+(/[A-Za-z0-9_.\\-]+)*$");
    private static final int MAX_PATH_LENGTH = 255;

    private NdjsonSupport() {
    }

    /**
     * Copia del ObjectMapper de la app para datos: fechas ISO (no arrays),
     * sin indentación (una línea = un registro) y tolerante a propiedades
     * desconocidas (la compatibilidad la controla {@code schema} del módulo).
     */
    public static ObjectMapper dataMapper(ObjectMapper base) {
        return base.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /** Writer de una línea NDJSON (garantiza que no haya saltos de línea internos). */
    public static ObjectWriter lineWriter(ObjectMapper dataMapper) {
        return dataMapper.writer().without(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * {@code true} si el nombre es seguro: relativo, con {@code /}, sin
     * {@code ..}/{@code .}, sin {@code \}, sin letra de unidad ni caracteres
     * de control, y de longitud acotada.
     */
    public static boolean isSafePath(String path) {
        if (path == null || path.isEmpty() || path.length() > MAX_PATH_LENGTH) {
            return false;
        }
        if (!SAFE_PATH.matcher(path).matches()) {
            return false;
        }
        for (String segment : path.split("/")) {
            if (segment.equals(".") || segment.equals("..")) {
                return false;
            }
        }
        return true;
    }

    public static MessageDigest newSha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en la JVM", e);
        }
    }

    public static String hex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    public static String sha256Hex(String value) {
        return hex(newSha256().digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    /** {@code true} si es un SHA-256 hex de 64 caracteres. */
    public static boolean isSha256Hex(String value) {
        return value != null && value.length() == 64 && value.chars().allMatch(c -> Character.digit(c, 16) >= 0);
    }
}
