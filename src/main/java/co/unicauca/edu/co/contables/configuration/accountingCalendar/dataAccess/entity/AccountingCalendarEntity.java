package co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.TenantId;
import lombok.*;

import java.time.LocalDate;

/**
 * @brief Entidad JPA para el calendario contable
 *
 * Representa la tabla 'accounting_calendar' en la base de datos, almacenando
 * fechas contables asociadas a una empresa específica. Incluye índices para
 * optimizar consultas por empresa y fecha, y soporte para multi-tenancy.
 */
@Entity
@Table(
    name = "accounting_calendar",
    indexes = {
        @Index(name = "idx_ac_enterprise", columnList = "idEnterprise"),
        @Index(name = "idx_ac_enterprise_date", columnList = "idEnterprise,date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingCalendarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String idEnterprise;    

    @Column(nullable = false)
    private LocalDate date;

    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;
}


