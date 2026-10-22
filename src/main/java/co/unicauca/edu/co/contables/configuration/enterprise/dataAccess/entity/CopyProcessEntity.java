package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessEstadoEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessTipoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

/**
 * @brief Entidad JPA para un proceso de copia (BACKUP/DUPLICATE/RESTORE),
 * tal como lo consume el frontend (Hito 6).
 *
 * No se usa Flyway/Liquibase en este microservicio (ver
 * {@code application-dev.yml}: {@code ddl-auto: update}), así que esta
 * tabla, igual que el resto de entidades del módulo enterprise, se crea/
 * actualiza por {@code ddl-auto}.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "copy_process")
public class CopyProcessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idProceso;

    @Enumerated(EnumType.STRING)
    private CopyProcessTipoEnum tipo;

    @Enumerated(EnumType.STRING)
    private CopyProcessEstadoEnum estado;

    private UUID empresaOrigen;

    private String empresaDestino;

    private String iniciadoPor;

    private LocalDateTime iniciadoEn;

    private LocalDateTime snapshotCorte;

    private LocalDateTime finalizadoEn;

    private String backupRef;

    private Integer faseActual;

    @Column(length = 2000)
    private String errorResumen;

    @TenantId
    private String tenantId;
}
