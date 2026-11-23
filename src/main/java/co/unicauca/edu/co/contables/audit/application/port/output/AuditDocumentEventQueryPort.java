package co.unicauca.edu.co.contables.audit.application.port.output;

import java.util.List;

import co.unicauca.edu.co.contables.audit.application.internal.DocumentSummaryProjection;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditDocumentCriteria;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditDocumentExportCriteria;
import co.unicauca.edu.co.contables.audit.application.internal.query.PageResult;
import co.unicauca.edu.co.contables.audit.application.internal.query.QueryOptions;
import co.unicauca.edu.co.contables.audit.domain.model.AuditDocumentEvent;

public interface AuditDocumentEventQueryPort {

        PageResult<DocumentSummaryProjection> findDocumentSummaries(
                        AuditDocumentCriteria criteria, QueryOptions options);

        List<AuditDocumentEvent> findEventsByDocumentCode(
                        String enterpriseId, String documentCode);

        List<DocumentSummaryProjection> findAllDocumentSummariesForExport(AuditDocumentCriteria criteria);

        List<AuditDocumentEvent> findAllEventsForExport(AuditDocumentExportCriteria criteria);

        long countDistinctDocuments(AuditDocumentCriteria criteria);
}
