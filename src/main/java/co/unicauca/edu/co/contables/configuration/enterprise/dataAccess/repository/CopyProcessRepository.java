package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CopyProcessEntity;

public interface CopyProcessRepository extends JpaRepository<CopyProcessEntity, UUID> {

    List<CopyProcessEntity> findAllByOrderByIniciadoEnDesc();
}
