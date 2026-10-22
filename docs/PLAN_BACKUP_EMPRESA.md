# Plan: Copia de seguridad de Empresa (Backup / Restore / Duplicate)

Referencia funcional: módulo `copy` del microservicio `enterprises-management` (solo lectura, no se toca).
Objetivo: replicar el comportamiento (BACKUP, RESTORE, DUPLICATE) dentro del monolito `ms-configuration-rf`,
sin el motor de saga distribuido (`SagaEngineService`, HTTP entre participantes, AMQP) porque todo vive
en el mismo proceso y la misma base de datos.

## Hallazgo clave (confirmado por auditoría del micro)

`DUPLICATE` no es un motor aparte: es **BACKUP del origen + RESTORE en un destino nuevo**, encadenados.
Por eso el plan construye BACKUP y RESTORE primero; DUPLICATE sale casi gratis como wrapper.

Tablas que toca el restore en el micro (via `JdbcTemplate` directo): `person_type`, `location`,
`enterprise`, `enterprise_tax_liabilities`, `enterprise_subject`.

## Hito 1 — Modelo y contrato (dataAccess/domain)
- [x] Definir `BackupManifest` (qué exportamos: empresa, direcciones, materias/subjects, terceros asociados).
- [x] Definir modo `CopyOperationType` enum: `BACKUP`, `RESTORE` (sin `DUPLICATE` como tipo real, igual que el micro).
- [x] Definir `EquivalenceMap` (idViejo → idNuevo) por tabla, para el remapeo en restore.
- [x] Config en `application.yml`: `app.backup.dir`, `app.backup.max-size-bytes` (reusar valores del micro como referencia, no copiar literal).

## Hito 2 — Export (BACKUP)
- [x] `IEnterpriseBackupService` (puerto) + implementación en `domain/services`.
- [x] Query de todo lo relacionado a una empresa: datos propios, address (país/depto/ciudad), subjects (materias), terceros vinculados.
- [x] Serializar a JSON (`enterprise.json` + archivos relacionados) y empaquetar en ZIP bajo `app.backup.dir`.
- [x] Validar `max-size-bytes` antes de escribir.
- [x] Endpoint: `POST /api/enterprises/{id}/backup` → crea el ZIP, devuelve referencia/id del backup.
- [x] Endpoint: `GET /api/enterprises/backups/{backupId}` → streaming de descarga (con validación de path traversal, igual que el micro).

## Hito 3 — Restore
- [x] `IEnterpriseRestoreService` (puerto) + implementación.
- [x] Leer ZIP (por `backupId` existente o archivo subido), parsear `enterprise.json`.
- [x] Insertar en una sola transacción local (`@Transactional`), generando nuevos IDs y llevando el `EquivalenceMap`.
- [x] Soporte `inplace=true` (restaurar sobre la misma empresa, borra-y-recrea) vs `inplace=false` (crea empresa nueva).
- [x] Endpoint: `POST /api/enterprises/restore` (body: `backupId` o archivo ZIP, flag `inplace`).

## Hito 4 — Duplicate (wrapper)
- [x] `IEnterpriseDuplicateService`: llama a BACKUP sobre origen → llama a RESTORE con `inplace=false` sobre el ZIP generado → limpia el ZIP temporal siempre (no hay opción de conservarlo, no la pidió el plan de endpoints).
- [x] Endpoint: `POST /api/enterprises/{id}/duplicate`.

## Hito 5 — Seguridad y validaciones
- [x] `SecurityConfig`: decidido — mismos roles de cliente que `EnterpriseController` (`admin_client`/`user_client`/`super_client`), nada agregado a `permitAll`.
- [x] Validar tamaño máximo de archivo subido en restore.
- [x] Manejo de errores: backup corrupto, empresa no encontrada, violación de FK en restore.

## Hito 6 — Pruebas y entrega
- [x] `mvn compile` en verde en cada hito antes de commitear.
- [ ] Probar manualmente: backup de una empresa real → descargar ZIP → restore con `inplace=false` → verificar que la empresa nueva tiene materias/direcciones correctas. (no se hizo: no había entorno con BD/Keycloak levantado en esta sesión)
- [ ] Probar duplicate end-to-end. (idem, pendiente de un entorno corriendo)
- [x] Commit en `feature/enterprise-datalayer-pattern` (única rama permitida), push a fork.
- [ ] PR a `upstream/develop`. (NO se hizo a propósito: instrucción explícita del usuario para esta tarea fue no abrir PR ni mergear nada)

## Decisiones tomadas por ambigüedad del plan

- **Subjects (materias) no son por-empresa**: en el monolito `SubjectEntity` no tiene FK a `enterprise`, es dato del tenant (filtrado por `@TenantId`/Hibernate multi-tenancy), a diferencia de `enterprise_subject` en el micro. El backup exporta las materias visibles en el tenant actual; el restore las aplica como upsert por `code` (crea las que falten, actualiza el nombre de las que ya existen) tanto en `inplace=true` como en `inplace=false`, y nunca las borra (podrían estar en uso por otra empresa del mismo tenant).
- **Catálogos no se reinsertan**: país/departamento/ciudad, responsabilidad tributaria, tipo de contribuyente y tipo de empresa se referencian por id tal cual vienen en el backup (no se exportan por valor ni se regeneran). Si el id no existe en el destino, el restore falla con un 400 claro (`RestoreFailedException`) en vez de un 500 genérico.
- **`inplace=true` requiere `targetEnterpriseId`**: el plan no define cómo se indica la empresa a sobrescribir; se agregó ese parámetro al endpoint `POST /api/enterprises/restore`, obligatorio solo cuando `inplace=true`.
- **`idUser` se preserva del backup**: al restaurar/duplicar se conserva el `idUser` original de la empresa exportada (no se reasigna al usuario que ejecuta el restore).

## Fuera de alcance (explícito)
- No se replica `SagaEngineService`, `IParticipantClientPort`, HTTP entre microservicios, ni AMQP.
- No se incluyen participantes `products`/`thirds` como procesos saga externos — si en el futuro el backup debe incluir datos de `inventory`/`thirds`, se extiende el `BackupManifest`, no se reintroduce orquestación.
