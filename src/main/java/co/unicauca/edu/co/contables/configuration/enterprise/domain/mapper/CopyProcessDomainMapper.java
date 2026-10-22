package co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.CopyProcess;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.CopyProcessRes;

/**
 * @brief Mapeador de dominio a DTO de respuesta para un proceso de copia.
 *
 * No se usa MapStruct aquí porque {@code tipo}/{@code estado} son enums de
 * dominio que el DTO de respuesta expone como {@code String} (así los
 * consume el frontend en {@code CopyProcess.ts}).
 */
@Component
public class CopyProcessDomainMapper {

    public CopyProcessRes toRes(CopyProcess domain) {
        if (domain == null) {
            return null;
        }
        return CopyProcessRes.builder()
                .idProceso(domain.getIdProceso())
                .tipo(domain.getTipo() == null ? null : domain.getTipo().name())
                .estado(domain.getEstado() == null ? null : domain.getEstado().name())
                .empresaOrigen(domain.getEmpresaOrigen())
                .empresaDestino(domain.getEmpresaDestino())
                .iniciadoPor(domain.getIniciadoPor())
                .iniciadoEn(domain.getIniciadoEn())
                .snapshotCorte(domain.getSnapshotCorte())
                .finalizadoEn(domain.getFinalizadoEn())
                .backupRef(domain.getBackupRef())
                .faseActual(domain.getFaseActual())
                .errorResumen(domain.getErrorResumen())
                .build();
    }
}
