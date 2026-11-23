package co.unicauca.edu.co.contables.commons.backup;

/**
 * @brief Entrada NDJSON abierta dentro del ZIP: cada {@link #write} agrega una línea.
 *
 * {@link #close()} cierra la entrada del ZIP y registra conteo, tamaño y
 * SHA-256 en el manifest; NO cierra el ZIP.
 */
public interface NdjsonEntryWriter extends AutoCloseable {

    /** Serializa {@code record} como una línea JSON (sin indentación) terminada en {@code \n}. */
    void write(Object record);

    /** Registros escritos hasta ahora. */
    long count();

    @Override
    void close();
}
