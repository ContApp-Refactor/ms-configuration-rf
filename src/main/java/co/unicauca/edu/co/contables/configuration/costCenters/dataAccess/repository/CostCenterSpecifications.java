package co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones para consultas dinámicas de centros de costo
 * Permite construir queries complejas sin usar @Query manual
 */
public class CostCenterSpecifications {

    /**
     * Especificación para obtener centros de costo auxiliares (último nivel)
     * Filtra por: empresa, estado activo y código con longitud >= 5
     * 
     * @param idEnterprise ID de la empresa
     * @return Specification para centros de costo auxiliares
     */
    public static Specification<CostCenterEntity> isAuxiliaryCostCenter(String idEnterprise) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filtro por empresa
            predicates.add(criteriaBuilder.equal(root.get("idEnterprise"), idEnterprise));
            
            // Filtro por estado activo
            predicates.add(criteriaBuilder.equal(root.get("status"), true));
            
            // Filtro por longitud de código >= 5 (centros de costo auxiliares/último nivel)
            // LENGTH(code) >= 5
            predicates.add(
                criteriaBuilder.greaterThanOrEqualTo(
                    criteriaBuilder.length(root.get("code")), 
                    5
                )
            );
            
            // Ordenar por código
            query.orderBy(criteriaBuilder.asc(root.get("code")));
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
