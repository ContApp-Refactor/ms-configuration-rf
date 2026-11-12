package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CopyProcessEntity;

public interface CopyProcessRepository extends JpaRepository<CopyProcessEntity, UUID> {

    List<CopyProcessEntity> findAllByOrderByIniciadoEnDesc();

    /**
     * {@code true} si algún proceso DEL TENANT ACTUAL (filtro de {@code @TenantId})
     * referencia el backup: es la prueba de pertenencia de un {@code backupRef}.
     */
    boolean existsByBackupRef(String backupRef);

    /** Procesos del tenant actual que referencian el backup (para no borrar un ZIP compartido). */
    long countByBackupRef(String backupRef);
}
