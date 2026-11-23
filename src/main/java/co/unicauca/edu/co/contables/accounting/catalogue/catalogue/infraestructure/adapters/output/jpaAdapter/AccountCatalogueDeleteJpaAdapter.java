package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueDeleteOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @brief Adaptador JPA para operaciones de eliminación física de cuentas
 *
 * Gestiona eliminación completa de cuentas con cascada recursiva:
 * - Eliminación de cuenta raíz y todas sus hijas
 * - Mantiene integridad referencial eliminando de abajo hacia arriba
 */
@Component
@Data
public class AccountCatalogueDeleteJpaAdapter implements IAccountCatalogueDeleteOutputPort {

    private final IAccountCatalogueRepository accountCatalogueRepository;

    /**
     * @brief Elimina cuenta y jerarquía completa de forma recursiva
     *
     * Inicia proceso de eliminación física que incluye:
     * - Validación de existencia de la cuenta
     * - Eliminación recursiva de toda la jerarquía descendiente
     * @param id identificador único de la cuenta raíz a eliminar
     */
    @Override
    public void deleteById(Long id) {
        AccountCatalogueEntity entity = accountCatalogueRepository.findById(id).orElse(null);
        if (entity != null) {
            // Eliminar físicamente la cuenta principal y sus hijas
            deletePhysical(entity);
        }
    }
    
    /**
     * @brief Eliminación física recursiva de jerarquía padre-hijo
     *
     * Algoritmo recursivo que elimina desde las hojas hacia la raíz:
     * - Primero elimina recursivamente todas las cuentas hijas
     * - Luego elimina la cuenta padre
     * @param entity cuenta raíz desde donde iniciar eliminación recursiva
     */
    private void deletePhysical(AccountCatalogueEntity entity) {
        // Eliminar recursivamente todas las cuentas hijas primero
        if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
            for (AccountCatalogueEntity child : entity.getChildren()) {
                deletePhysical(child);
            }
        }
        
        accountCatalogueRepository.delete(entity);
    }
}
