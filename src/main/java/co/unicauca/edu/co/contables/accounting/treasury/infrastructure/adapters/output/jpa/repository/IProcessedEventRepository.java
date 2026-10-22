package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.repository;

import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IProcessedEventRepository extends JpaRepository<ProcessedEventEntity,Long> { boolean existsByEventId(String eventId); }
