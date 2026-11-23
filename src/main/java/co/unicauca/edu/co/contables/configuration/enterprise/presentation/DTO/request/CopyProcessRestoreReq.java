package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Cuerpo de {@code POST /api/enterprises/copy/restore}.
 *
 * {@code empresaDestino} es, según {@code inplace}, el id de una empresa
 * existente a sobrescribir ({@code inplace=true}) o el nombre de la empresa
 * nueva a crear ({@code inplace=false}).
 *
 * Campos opcionales (aditivos): {@code nombreDestino} (gana sobre
 * {@code empresaDestino} como nombre), {@code nitDestino} (para evitar el 409
 * por NIT repetido, D3) y {@code skipUnknownModules} (omitir módulos de la
 * copia que esta versión no soporta).
 */
@Getter
@Setter
@NoArgsConstructor
public class CopyProcessRestoreReq {
    private String backupRef;
    private String empresaDestino;
    private boolean inplace;
    private String nombreDestino;
    private String nitDestino;
    private boolean skipUnknownModules;
}
