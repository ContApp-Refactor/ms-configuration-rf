package co.unicauca.edu.co.contables.audit.application.port.output;

import java.util.List;

import co.unicauca.edu.co.contables.audit.application.internal.CombinedSession;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditSessionCriteria;
import co.unicauca.edu.co.contables.audit.application.internal.query.PageResult;
import co.unicauca.edu.co.contables.audit.application.internal.query.QueryOptions;

public interface AuditSessionQueryPort {

    PageResult<CombinedSession> findCombinedSessions(AuditSessionCriteria criteria, QueryOptions options);

    long countByCriteria(AuditSessionCriteria criteria);

    List<CombinedSession> findAllForExport(AuditSessionCriteria criteria);
}
