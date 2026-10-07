package co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "financial_statement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialStatementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}