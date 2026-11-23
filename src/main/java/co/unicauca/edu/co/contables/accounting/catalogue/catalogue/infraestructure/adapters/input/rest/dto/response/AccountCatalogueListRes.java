package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para respuesta de lista jerárquica de cuentas contables
 *
 * Representa una cuenta en estructura de árbol con sus cuentas hijas,
 * utilizado para mostrar catálogos organizados jerárquicamente.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountCatalogueListRes {
    Long id;
    String code;
    String description;
    String nature;
    String financialStatus;
    String classification;
    String parent;
    Boolean crossing;
    Boolean costCenter;
    Boolean status;
    Integer usageCount;

    List<AccountCatalogueListRes> children;
}
