package co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "financial_statement_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialStatementTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}