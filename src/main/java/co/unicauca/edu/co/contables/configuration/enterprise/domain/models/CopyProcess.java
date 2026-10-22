package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import java.time.LocalDateTime;
import java.util.UUID;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessEstadoEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessTipoEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio de un proceso de copia (BACKUP/DUPLICATE/RESTORE),
 * tal como lo consume el frontend (Hito 6).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CopyProcess {
    private UUID idProceso;
    private CopyProcessTipoEnum tipo;
    private CopyProcessEstadoEnum estado;
    private UUID empresaOrigen;
    private String empresaDestino;
    private String iniciadoPor;
    private LocalDateTime iniciadoEn;
    private LocalDateTime snapshotCorte;
    private LocalDateTime finalizadoEn;
    private String backupRef;
    private Integer faseActual;
    private String errorResumen;
}
