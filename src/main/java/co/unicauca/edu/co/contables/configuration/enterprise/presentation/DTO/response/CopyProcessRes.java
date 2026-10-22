package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO de respuesta de un proceso de copia, con la forma exacta que
 * espera el frontend ({@code CopyProcess.ts}, Hito 6).
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyProcessRes {
    private UUID idProceso;
    private String tipo;
    private String estado;
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
