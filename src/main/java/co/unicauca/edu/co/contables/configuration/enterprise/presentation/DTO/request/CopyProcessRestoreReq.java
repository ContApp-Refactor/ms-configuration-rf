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
 */
@Getter
@Setter
@NoArgsConstructor
public class CopyProcessRestoreReq {
    private String backupRef;
    private String empresaDestino;
    private boolean inplace;
}
