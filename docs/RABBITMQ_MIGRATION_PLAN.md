# Migration Plan: RabbitMQ → Spring Events (cell by cell, tests included)

> Incremental migration of `ms-configuration-rf` from RabbitMQ to Spring events,
> one **cell** (producer → consumer pair) at a time. Every cell migrates **main code + tests**,
> is verified, and ends with an explicit gate question before proceeding.
>
> Companion document: `docs/RABBITMQ_MATRIX.md` (current topology matrix).
>
> **Validated against code (2026-11-02):** all file paths, line numbers, listener/publisher
> inventories (24 `@RabbitListener` methods, 34 `convertAndSend` sites, 11 `AbstractMessageListener`
> copies, 24 Rabbit config classes), dead-code claims, and yml/pom/docker line refs verified —
> gaps found fixed: Cell 17's unlisted `ReceiptPublisher` purchase block + footnotes G2–G5.

---

## Cell 0 — Decisions & prerequisites (ask before touching any file)

| # | Decision | Status |
|---|---|---|
| D1 | **Delivery semantics** | ✅ **DECIDED — 🅰 for DB flows** (`@TransactionalEventListener(AFTER_COMMIT)`, matches Rabbit: fired after commit, same thread) **· 🅱 for audit/session** (plain `@EventListener`). |
| D2 | **Sync vs async** | ✅ **DECIDED — sync default** (normal `ApplicationEventPublisher`; context inherited on caller thread) **· `@Async` + existing `TenantAwareTaskDecorator` only for kardex/peps `FactureListener`** (heavy consumers). **No global async multicaster.** |
| D3 | **Error handling** | ✅ **DECIDED — keep at-most-once parity**: `try/catch` → `IMessageErrorHandlingPort.saveProcessingError(...)` → swallow (was: ACK on failure). Keep `IEventRecoveryActionPort` re-sync actions. |
| D4 | **External flows (7 bindings)** | ✅ **DECIDED — everything stays, transport only, no drops, no new transports**: 4 unfinished `.used` flows (no producer ever built) → **dormant Spring listeners** (Cell 22) · 3 treasury flows are **unfinished features** → **Rabbit + outbox removed, business code kept** (port + use-case calls intact, transport implemented later when the feature is developed) (Cells 21/23). |
| D5 | **Payload objects** | ✅ **DECIDED — immutable records/DTOs, never domain entities** (mapped at both boundaries, D8). |
| D6 | **Test style per cell** | ✅ **DECIDED — 🅰 migrate existing tests in place** (no round-trip tests). Missing tests (test-gaps list) get basic happy-path + error-path unit tests. |
| **D7** | **Outbox pattern** | ✅ **DECIDED — both outboxes deleted** (facture at Cell 17, treasury at Cell 21 = D7-B). **No retry pump, no rethrow fix** — nothing publishes cross-JVM anymore: the facture path becomes in-process `publishEvent`, the treasury port gets a no-op adapter until its unfinished flow defines a transport. See [Outbox decision](#outbox-decision--d7). |
| **D8** | **Event ownership & mapping** | ✅ **DECIDED — 🅰 producer-owned contracts** in `commons/events/<bounded-context>/<submodule>/` (see [D8 section](#d8--event-ownership--mapping-structure)). |
| **D9** | **Postman Rabbit-injecting scenarios → Java** | ✅ **DECIDED — `@SpringBootTest` + `@AutoConfigureMockMvc(addFilters = false)`**, whole scenarios move, each rewrite in its owning cell; Postman collections stay **frozen as historical record** (zero edits); "field missing" sad paths dropped (see [Postman → Java section](#postman--java-test-migration-rabbit-injecting-scenarios)). |

> **Scope guard (decided):** this migration is **transport swap only** — RabbitMQ → Spring events.
> No redesigns bundled (package moves excepted for the Cell 3 publisher relocation, no new ports,
> no feature work). Pre-existing design flaws (e.g. the one-way `usageCount` ratchet) are **out of scope**.

**Cell 0 file changes:**
- Create `commons/events/IntegrationEvent.java` base record: `eventId, eventType, tenantId, enterpriseId, correlationId, occurredAt` — mirrors current message headers (`x-tenant-id`, `x-jwt-token`, `eventId`).
- ~~`commons/config/EventContextAspect.java`~~ — **not needed**: D2 = 🅰 (sync listeners keep the caller's ThreadLocal context; no context hand-off to an async thread except kardex/peps, which the existing `TenantAwareTaskDecorator` already covers).
- (D8) `commons/events/package-info.java` + empty BC folders.
- (D9) add `org.awaitility:awaitility` (**test scope only**) to `pom.xml` — used by the Postman→Java flow tests for `@Async`/AFTER_COMMIT polling. *Alternative if you want zero new deps: a 10-line polling helper in a test util class (decide at Cell 0, either is fine).*

**Cell 0 tests:** run full suite as baseline: `./mvnw -q test` (record current failures, if any).

---

## Example: how Spring events are used in this migration

One `publishEvent` call fans out to every registered listener — this **replaces the fanout exchange**
(no exchange, no queue, no binding, no serialization):

```java
// 1) Event — immutable record, lives in the producer's module (D5)
public record ProductChangedEvent(String eventId, String tenantId, String enterpriseId,
        String correlationId, Instant occurredAt, ProductDto data) implements IntegrationEvent {}

// 2) Publisher — infrastructure adapter implementing the existing port
@Component
@RequiredArgsConstructor
public class ProductEventPublisher implements IProductEventPort {          // port unchanged
    private final ApplicationEventPublisher events;                         // was: RabbitTemplate

    @Override
    public void notifyProductChanged(ProductChangedEvent event) {
        events.publishEvent(event);                                        // was: convertAndSend(EXCHANGE, "", event, headers)
    }
}

// 3) Consumers — one per former queue; Spring delivers to ALL of them (fanout semantics)
@Component
@RequiredArgsConstructor
public class StockListener {                                               // no extends, no Channel, no deliveryTag
    private final IStockCommandRepositoryPort stockCommandPort;
    private final IMessageErrorHandlingPort errorPort;                     // D3 — same port as today

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)     // D1 🅰
    public void on(ProductChangedEvent event) {
        try {
            stockCommandPort.save(map(event.data()));
        } catch (Exception e) {                                            // was: catch + basicAck
            errorPort.saveProcessingError(event.eventId(), e.getMessage(),
                    event, "STOCK");                                       // at-most-once, same as Rabbit today
        }
    }
}
```

- 1 event class + 1 publisher line + 1 listener method per cell.
- Retry/DLQ chains (`.dlx/.dlq/.retry.queue`) have no equivalent — replaced by the D3 catch above.

---

## D8 — Event ownership & mapping structure

### The problem Rabbit solved for free

With Rabbit + JSON, the contract was the **wire format**, not a class — that's why DTOs are
duplicated today (`inventory/products/.../ProductAsyncDto`, separate copies in `kardex` and `peps`,
each with its own `EventDto` + mapper). Neither module imported the other.
Naive Spring events break this: `publishEvent(productsDto)` makes `kardex` compile-time dependent
on `products`. Same JVM makes it *possible*, not *legitimate*.

**Option 🅰 (recommended): producer-owned contracts in `commons/events/`** — the published-language
container. One class per event; ownership = producer; consumers are read-only dependents.

> **Rule: the event lives in the folder of the module that publishes it.** Folder = owner, not topic.
> E.g. `third.used` is produced by `debt_payments`, consumed by `configuration/thirds` →
> `commons/events/accounting/debt_payments/ThirdUsed.java`.

### Structure — mirrors the `src/main/java/.../contables/<bc>/<submodule>` tree

```
commons/events/
├── IntegrationEvent.java                  # envelope: eventId, eventType, tenantId,
│                                          # enterpriseId, correlationId, occurredAt
├── package-info.java                      # ownership rule written down
│
├── accounting/
│   ├── debt_payments/
│   │   ├── ReceiptChanged.java            # receipt.exchange
│   │   ├── WriteOffChanged.java           # writeoff.exchange
│   │   ├── InvoicePaymentChanged.java     # invoice.exchange (debt_payments leg)
│   │   ├── InvoiceReminderRequested.java  # notifications.exchange
│   │   ├── ThirdUsed.java                 # third.used.exchange      ← owned here (producer)
│   │   ├── CostCenterUsed.java            # costcenter.used.exchange ← owned here
│   │   └── PaymentMethodUsed.java         # paymentmethod.used.exchange ← owned here
│   ├── facture/
│   │   ├── InvoiceIssued.java             # invoice.exchange (facture leg)
│   │   ├── PurchaseInvoiceCreated.java    # purchase.invoice.exchange
│   │   ├── WeightedAverageRecalculated.java # weighted.average.exchange
│   │   ├── PepsValuationCalculated.java   # peps.exchange
│   │   └── DocumentAudited.java           # audit.exchange rk document.event
│   ├── catalogue/accounting/ ...          # (consumers here own nothing yet)
│   └── treasury/ ...                      # empty for now — flows unfinished, transport TBD
│
├── inventory/
│   ├── products/
│   │   ├── ProductChanged.java            # product.exchange → kardex, peps, stock
│   │   └── ProductUsed.java               # product.used.exchange (see tie-breaker below)
│   ├── kardex/
│   └── peps/
│
├── configuration/
│   ├── thirds/
│   │   └── ThirdUpdated.java              # third.updated.exchange → notifications
│   └── ...                                # costCenters, typesOfDocuments only consume
│
├── notifications/                         # likely empty — notifications only consumes
├── keycloak/
│   └── audit/SessionAudited.java          # audit.exchange rk session.event
└── commons/
    └── audit/OperationAudited.java        # audit.exchange rk operation.event
```

**Two-level depth** `<bounded-context>/<submodule>/` — identical shape to the modules, so a class at
`commons/events/accounting/debt_payments/` reads instantly as *accounting BC, debt_payments module,
producer*. Single-module BCs (`keycloak`, `notifications`, `audit`) may collapse to one level, but
always keeping `<bc>/` first makes the rule mechanical.

**Tie-breaker (multiple producers):** `product.used` has two producers (kardex, peps).
Contract goes to the BC that owns the subject/exchange → `inventory/products/ProductUsed.java`
(`products` already declares `RabbitProductUsedConfig` today — broker topology and contract get
the same owner). Rule: *multiple producers → contract lives with the subject owner.*

### Mapping

- **Producer**: maps domain → contract inside its publisher adapter (existing `messageBroker/mapper`
  code already does this pre-JSON) → `publishEvent(contract)`.
- **Consumer**: maps contract → local DTO/domain inside its listener (existing mappers reused).
  Only the JSON serialize/deserialize hop disappears; the mapping stays.
- Coupling is to a **stable immutable contract**, not to a mutable implementation class —
  the same coupling a REST/Kafka schema gives you. Zero coupling is impossible; Rabbit already
  coupled you to the JSON schema.

### Rules

1. `commons/events/*` contains **only**: Java records / primitives / String / Instant / `IntegrationEvent`.
   **Forbidden**: domain entities, module enums, services, validation logic, any import of a module
   package. It must be a *leaf* — if a contract can't be written without importing a module, it's a leak.
2. **Imports allowed**: any module → `commons.events.**` + its own packages. Never module → another
   module's packages *because of events* (existing port-sharing debt like
   `debt_payments.domain.ports.IMessageErrorHandlingPort` is out of scope — no new cases).
3. **Internal-only events** (never crossing a BC) stay in their module; Reactor/Spring app events
   inside a module need no ceremony. `commons/events` is strictly cross-BC.
4. **Versioning**: no `v1/` folders until a breaking change actually happens; additive fields only;
   contract class renames = breaking.

### Cell 0 delta (D8)

- Create `commons/events/IntegrationEvent.java`, `commons/events/package-info.java` (ownership rule),
  empty BC folders.
- Per cell (protocol step 1 now reads): *create the contract record in
  `commons/events/<bc>/<submodule>/` if the event crosses BCs; otherwise a local event class.*

---

## Outbox decision (D7) — boilerplate to remove

The outbox solves **one** problem: the dual-write between a DB transaction and a **separate process**.
In-process Spring events remove it — `@TransactionalEventListener(AFTER_COMMIT)` already gives
commit-ordering with no second system. Remaining gap = JVM crash between commit and afterCommit
(millisecond window), and current listeners are already **at-most-once** (ACK on failure).

| Flow | Action |
|---|---|
| **16 internal cells** | **No outbox.** Plain `publishEvent` + D3 error handler |
| **`facture` outbox** (Cell 17 — consumer `invoice.treasury.queue` is the **same JVM**) | **Delete entirely** (files below) |
| **`treasury` outbox** (Cell 21 — outbound flow is an **unfinished feature**) | **Delete entirely** (D7-B files below). The port stays with a **no-op adapter** (logs the event, TODO for the real transport when the counterpart feature is built). No retry pump, no HTTP client — nothing to deliver to yet |

> **D7-C (keep + strip + add retry pump) — DROPPED by decision.** The outbox is unnecessary
> complexity once we control the transport; the dead queries (`findUnpublishedAcrossTenants()`,
> `findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()`), the decorative fields (`nextAttemptAt`,
> `attempts`, `lastError`, `FAILED` transitions) and the rethrow-after-commit behavior all die
> together with `IOutboxEventRepository` / `OutboxEventAdapter` at Cell 21.

### Boilerplate to REMOVE

**A. `facture` outbox — delete all (Cell 17):**
- `accounting/facture/application/service/skeleton/service/PurchaseInvoiceOutboxService.java`
- `accounting/facture/application/service/skeleton/model/PurchaseInvoiceOutboxEvent.java` (entity)
- `accounting/facture/application/service/skeleton/repository/PurchaseInvoiceOutboxRepository.java` (+ table `purchase_invoice_outbox` — `ddl-auto` will drop it in dev; drop manually in prod)
- Caller changes: `SkeletonFactureService.java` — replace `outbox.enqueue(...)` with the direct publisher call; delete enqueue/dto-building block in `PurchaseInvoiceEventPublisher` if it duplicates it

**B. `treasury` outbox — delete all (Cell 21):**
- `accounting/treasury/infrastructure/adapters/output/messageBroker/OutboxEventAdapter.java`
- `accounting/treasury/infrastructure/adapters/output/messageBroker/support/TreasuryOutboxMessenger.java`
- `accounting/treasury/infrastructure/adapters/output/jpa/entity/OutboxEventEntity.java` (table `outbox_events`)
- `accounting/treasury/infrastructure/adapters/output/jpa/repository/IOutboxEventRepository.java`
- ~~Port `application/output/ITreasuryEventPublisher.java`~~ — **KEPT** (it's a proper port; only its
  adapter changes from outbox to a no-op logger (transport TBD), so `PaymentVoucherService`,
  `PayableWriteOffService`, both `Transactional*UseCase` constructors stay untouched)

**C. `treasury` outbox — ~~keep if D4=🅱~~ DROPPED** — see the D7-C note above: no keeping, no
stripping, no retry pump. Everything in B is deleted.

**D. Cross-cutting boilerplate removed with the migration (see Phase 10):** all `AbstractMessageListener` copies (Channel/ack/error template), `JWTContextRabbitMqAspect` + rabbit context methods in `JwtTokenService`, `Rabbit*Config` classes, `spring-boot-starter-amqp`, yml rabbit blocks.

---

## Test patterns (why tests migrate too)

| Pattern | Current form | Migrated form |
|---|---|---|
| **Listener tests** (12 files) | mock `Channel` + `Message`, call `handleX(event, message, channel, DELIVERY_TAG)`, `verify(channel).basicAck(...)` | call `handleX(event)` — no channel/message/deliveryTag; **replace ACK assertions** with: on success → port invoked; on failure → `IMessageErrorHandlingPort.saveProcessingError(...)` invoked **and no exception thrown** |
| **Publisher tests** (5 files) | `@Mock RabbitTemplate/AmqpTemplate` + `verify(convertAndSend(exchange, key, dto))` | `@Mock ApplicationEventPublisher` + `verify(publishEvent(isA(Event.class)))`; "never breaks business flow" test kept (`doThrow` on `publishEvent` → `assertDoesNotThrow`) |
| **Context/topology tests** (3 files) | `@SpringBootTest` asserting Rabbit topology wires (`TreasuryApplicationTests`, `AuditApplicationTests`, `MsNotificationsApplicationTests`) | same test, no Rabbit assumptions; must pass **without a broker** |

**Test-profile nuance (verified):** most Rabbit configs (`products`, `peps`, `facture`, …) carry
`@Profile("!test")` — their queue/exchange beans **never load in tests**; only `TreasuryRabbitConfig`
has no profile guard (that's why `TreasuryApplicationTests` can assert topology). Listener unit tests
are therefore pure Mockito (mock `Channel`/`Message` today, mock nothing but ports after migration) —
context-level tests never had a broker dependency to begin with.

**Every cell's gate = compile + migrated tests green:**

```bash
./mvnw -q -DskipTests compile
./mvnw -q test -Dtest='<tests of this cell>'
```

Then the plan asks: **"Cell N ✅ (code + tests). Proceed to next cell?"** — answers allowed: *yes / hold / skip / abort*.

---

## Postman → Java test migration (Rabbit-injecting scenarios)

**Scope (verified 2026-11-02):** 79 requests across 3 Postman collections publish through the
RabbitMQ management API (`POST {{baseUrlRabbit}}/api/exchanges/%2F/<exchange>/publish`) — they die
with Rabbit. Everything else in the collections is pure HTTP and untouched by this migration.

| Collection | # | Exchange(es) | Injects messages for | Java test → **cell** |
|---|---|---|---|---|
| `audit-rf - AUDITORIA-INTEGRATION-TEST` | 33 | `audit.exchange` (topic, 3 rks) | audit session (8) / operation (12) / document (13) listeners | `AuditSessionEventFlowTest` → **1** · `AuditOperationEventFlowTest` → **2** · `AuditDocumentEventFlowTest` → **3** |
| `inventory-weighted-average-rf - TEST` | 32 | `product.exchange` (1) + `weighted.average.exchange` (31) | kardex/peps/stock product listeners (fixture step); kardex `FactureListener` (`@Async`, D2) | `ProductEventFlowTest` → **10** · `WeightedAverageFactureFlowTest` → **11** |
| `ms-debt-payments-rf - [INTEGRATION]` | 14 (+ §3 REST) | `invoice.exchange` | debt_payments `InvoiceEventListener`; §3 REST tests use event-created invoices as fixtures | `InvoiceEventFlowTest` + `ReceiptWriteOffRestFlowTest` → **16** |

**D9 decisions (all settled):**
1. **Form:** `@SpringBootTest` + `@AutoConfigureMockMvc(addFilters = false)` — repo precedent
   (`FinancialStatementFlowIntegrationTest`), `spring-security-test` already in the pom.
   **No TestRestTemplate** — nothing here tests Tomcat/network; MockMvc shares the JVM/context with
   the event injection (the beans `publishEvent` mutated are the beans MockMvc routes to).
2. **Whole scenario moves**: fixture injection **and** the HTTP assertions those fixtures feed
   (e.g. debt-payments invoices → ReceiptController tests).
3. **Each rewrite lands in its owning cell** (D6): the Postman event section dies exactly when its
   cell migrates, so no gate is ever green-with-Rabbit-but-broken-Java.
4. **Postman stays frozen** — collections and envs get **zero edits**; they remain historical
   records (they were run as gates before). Their Rabbit sections **fail by design** after their
   cell lands; from then on the Java flow tests are the gate. (`baseUrlRabbit`/`Username`/`Password`
   env vars stay too — historical.) A **`.postman/README.md`** index (plain Markdown, not part of
   the JSONs) maps each dead section → its Java replacement test + cell, so nobody debugs a
   `connection refused` on `:15672` wondering what broke.
5. **"Field missing" JSON sad paths (~20 requests) are DROPPED** — a typed record component cannot
   be omitted, so schema drift is now a *compile* error (`commons/events` records, D8). **Kept:**
   null-field tests (records accept null) and listener-throw tests asserting
   `saveProcessingError(...)` + no exception (D3 at-most-once parity — Postman verified this via
   `message-processing-errors`, it maps 1:1 to a repository assertion).

### Recipe — every rewritten scenario follows it

```java
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("audit-rf §2/Sesiones — flujo de eventos de sesión")   // Postman folder → class DisplayName
class AuditSessionEventFlowTest {

    @Autowired ApplicationEventPublisher events;  // injection point (was POST :15672)
    @Autowired TransactionTemplate tx;            // only for D1 AFTER_COMMIT listeners (see gotcha 1)
    @Autowired MockMvc mvc;                       // only where the scenario asserts an endpoint
    @Autowired AuditEntryRepository repo;         // default: assert effects via repository

    // postman: audit-rf §2/Sesiones/[Happy Path] Login exitoso/Evento Login
    @Test
    @DisplayName("Evento LOGIN publicado → se persiste en auditoría")   // pm.test string, kept
    void loginEvent_isPersisted() {                 // happy path
        events.publishEvent(new SessionContract(...));    // audit-session = D1 plain @EventListener
        // was: routed:true (broker ack) — ahora se asserta el efecto real
        assertThat(repo.findBy...).isNotEmpty();          // sync listener → assert immediately
    }

    // postman: audit-rf §2/Sesiones/[Sad Path] userName falta → traducido: listener falla → error guardado (D9-5: field-missing descartado)
    @Test
    @DisplayName("Listener falla → saveProcessingError y la excepción se traga")   // D3 parity (kept sad path)
    void listenerFailure_savesProcessingError_andSwallows() {
        ...
        assertThat(errorRepo.findAll()).hasSize(1);        // swallowed — nothing propagates
    }
}
```

**Rules:**
- **Inject at the event bus, not through the producer.** `publishEvent(contract)` bypasses the
  producer exactly like the broker injection did → *same coverage level* as the Postman test
  (scope guard respected). Going through the producer adapter would be a different, deeper test —
  not this migration's job.
- **Assertion upgrade comes free:** what was `routed: true` + `setTimeout(3000)` no-op becomes
  repository assertions. Polling (`await()`) only for the `@Async` kardex/peps `FactureListener`
  (D2) and AFTER_COMMIT flows; everything sync asserts immediately. No `awaitility` in the pom yet
  → Cell 0 adds it test-scoped (or the polling helper alternative).
- **MockMvc only when the endpoint is the subject** (receipts/writeoffs, product queries mirroring
  the old GETs); otherwise repository/service assertions directly.
- **Auth:** no real Keycloak from Java — filters off. The frozen Postman suites remain the record
  of the real-auth path.

### Preserving Postman comments & assertions (asked 2026-11-02)

**Yes — both are preserved**, with one deliberate exception each side. JUnit 5 carries descriptions
natively (`@DisplayName`, class + method), and this repo already uses them.

| Postman artifact | Java form |
|---|---|
| Folder name (`2. Consumo de eventos / Sesiones`) | `@Nested` inner class per folder (optional for small scenarios) |
| Request/step name (`[Happy Path] Login exitoso / Evento Login`) | `@DisplayName("...")` on the method — **keep the original wording, incl. Spanish** |
| Collection/request `description` field | Javadoc on the class/method |
| `//` comments inside `pm.test` scripts | kept as real `//` comments (e.g. `// Pausa para procesamiento asíncrono` → note on the line where `await()`/sync assert replaced it) |
| Traceability to the frozen record | one header comment per test: `// postman: <collection> §<folder>/<request name>` |
| `pm.test("Status code is 200")` | `andExpect(status().isOk())` — **assertion kept**, its string → `@DisplayName` |
| `pm.expect(...).to.eql(...)` / `.to.exist` | `jsonPath(...).isEqualTo(...)` / AssertJ — **assertion kept**, same expectation, same wording in the display name |

**Deliberate exceptions — the three broker-specific artifacts that get translated, not dropped silently:**

| Postman assertion | Fate |
|---|---|
| `routed: true` (broker accepted the message) | **replaced** by the effect assertion (repository row / endpoint GET) — comment left behind: `// was: routed:true (broker ack)` |
| `setTimeout(..., 3000)` no-op pauses | **dropped** — sync listeners assert immediately; `@Async`/AFTER_COMMIT use `await()` |
| `responseTime < 2000` (broker HTTP latency) | **dropped** — no broker to talk to |
| "field missing" `pm.test`s | **dropped** per D9-5 |

Net effect: everything the Postman tests actually *verified* (statuses, JSON values, error-record
counts) carries over — often sharpened, since repository assertions catch what a `routed:true`
never could; only the four broker-specific line items above are translated away.

### Gotchas (will bite during the rewrite)

1. **`@TransactionalEventListener` + no active transaction = event never fires** (fallback is off
   by default). Every D1-AFTER_COMMIT flow test must publish inside
   `tx.executeWithoutResult(s -> events.publishEvent(...))`. This is the #1 confusing failure.
2. **JWT/tenant context:** the old tests passed `x-jwt-token`/`x-tenant-id` headers, which fed
   `JWTContextRabbitMqAspect`. In-JVM there is no HTTP thread — if a listener path reads
   `JwtTokenService` context, the test must seed it manually (check per listener when the cell
   lands; audit events carry everything in the payload and are likely unaffected).

---

## Cell protocol (applied identically to every cell)

1. **Create** event contract — if it crosses bounded contexts: a record in `commons/events/<bc>/<submodule>/` (D8, producer-owned); if internal to one module: a local event class. Reuse an existing DTO only if already immutable.
2. **Publisher**: inject `ApplicationEventPublisher`, replace `convertAndSend(exchange, "", event, headers)` with `publishEvent(evt)`.
3. **Listener**: replace `@RabbitListener(queues = X, containerFactory = ...)` with `@TransactionalEventListener`/`@EventListener`; drop `Message`, `Channel`, `deliveryTag` params; wrap body in the existing error-handling block (D3).
4. **REMOVE** — delete the queue + binding + DLX/DLQ/retry beans **owned only by this cell** (exchange deleted only on its last cell — flagged per cell); if the config file ends up empty, **delete the file**.
5. **Migrate this cell's tests** (map below): remove Rabbit mocks/assertions, assert `publishEvent`/listener invocation + error path — **and, if this cell owns a Postman scenario, create its Java flow test** (see [Postman → Java section](#postman--java-test-migration-rabbit-injecting-scenarios)).
6. **Verify**: compile + targeted tests + `grep` for leftover references + run app & exercise flow (listener log line).
7. **Update** `docs/RABBITMQ_MATRIX.md` marking the cell ✅ migrated.
8. **🛑 GATE — ask user:** *"Cell N migrated. Proceed to next cell?"*

---

## Phase 1 — `audit.exchange` (topic) — establishes the pattern

### Cell 1 — `keycloak` → `audit.session.queue` (rk `session.event`)
- **Modify**: `keycloak/audit/KeycloakSessionEventPublisher.java` (AmqpTemplate → ApplicationEventPublisher) · `audit/infrastructure/adapters/input/messageBroker/AuditSessionListener.java`
- **REMOVE**: `audit/infrastructure/config/rabbitConfig/RabbitSessionConfig.java` (queue + binding beans; exchange stays — 2 cells remain)
- ⚠ **Folder trap (verified):** the same `rabbitConfig/` folder also contains **`AuditExportAsyncConfig`** — a plain Spring `@EnableAsync` + `ThreadPoolTaskExecutor` (`exportTaskExecutor`) for audit export adapters, **NOT Rabbit**. Delete only `RabbitSessionConfig` (Cells 1–3 remove the other two); **never delete the folder wholesale**, and `AuditExportAsyncConfig` survives the teardown
- **Tests**:
  - `keycloak/unitAudit/unitAuditSession/AuditSessionEventPublisherImplTest.java` — replace `AmqpTemplate` mock + `convertAndSend(AUDIT_EXCHANGE, SESSION_EVENT_ROUTING_KEY, ...)` asserts with `publishEvent` asserts; remove imports of `RabbitAuditPublisherConfig`/`RabbitSessionConfig`
  - `audit/AuditApplicationTests.java` — context test must stay green (no broker)
  - **NEW `AuditSessionEventFlowTest`** — replaces `audit-rf` §2/Sesiones (8 Rabbit publishes); closes the `AuditSessionListener` test gap (happy path + error path, recipe above)
- **Gate →**

### Cell 2 — `commons` → `audit.operation.queue` (rk `operation.event`)
- **Modify**: `commons/audit/publisher/AuditEventPublisher.java` · `audit/.../AuditOperationListener.java`
- **REMOVE**: `audit/.../RabbitOperationConfig.java`
- **Tests** (3 duplicates of the same publisher test, all migrate):
  - `configuration/unitAudit/auditCommon/AuditEventPublisherTest.java`
  - `inventory/products/unitAudit/AuditEventPublisherTest.java`
  - `keycloak/unitAudit/common/AuditEventPublisherTest.java`
  - each: `@Mock AmqpTemplate` → `@Mock ApplicationEventPublisher`; `verify(amqpTemplate).convertAndSend(AUDIT_EXCHANGE, OPERATION_EVENT_ROUTING_KEY, dto)` → `verify(appEvents).publishEvent(isA(...))`; keep "business flow never breaks" test
  - **NEW `AuditOperationEventFlowTest`** — replaces `audit-rf` §2/Operaciones (12 Rabbit publishes); closes the `AuditOperationListener` test gap
- **Gate →**

### Cell 3 — `facture` → `audit.document.queue` (rk `document.event`)
- **Modify**: `accounting/facture/application/service/skeleton/audit/publisher/FactureAuditEventPublisher.java` — swap `AmqpTemplate` → `ApplicationEventPublisher` **and move it to `accounting/facture/infrastructure/adapters/output/audit/`** (✅ decided: put it where it belongs — matches every other module's publisher location; trivial move: fix `package` line + imports, no test to move) · `audit/.../AuditDocumentEventDtoListener.java`
- **REMOVE**: `audit/.../RabbitDocumentConfig.java`; **after this cell** also `commons/config/RabbitAuditPublisherConfig.java` (last binding to `audit.exchange`) + `configuration/unit/commons/config/aspect/JWTContextRabbitMqAspectUnitTest.java` (D2=🅰 decided — the Rabbit aspect dies with the last Rabbit listener)
- **Tests**: no dedicated test for this publisher → **add one** (assert `publishEvent` + swallow-on-failure); rerun the 3 `AuditEventPublisherTest`s from Cell 2 · **NEW `AuditDocumentEventFlowTest`** — replaces `audit-rf` §2/Documentos (13 Rabbit publishes); closes the `AuditDocumentEventDtoListener` test gap
- **Gate →**

---

## Phase 2 — `*.used` fanouts (local producer = `debt_payments`)

> **Scope note (decided):** Cells 4–7 are **transport-swap only** — no package moves, no feature
> changes. The `usageCount` ratchet (only ever increments, never decremented — pre-existing design
> flaw) is **out of scope**: keep behavior exactly as-is; a "real reference check instead of
> counters" refactor is future work, after this migration.

### Cell 4 — `debt_payments` → `catalogue/paymentMethods` (`paymentmethod.used.exchange`)
- **Modify**: `debt_payments/infraestructure/output/messageBroker/ResourceUsagePublisher.java` (remove paymentmethod block; port `IResourceUsageNotifierPort` unchanged) · `catalogue/paymentMethods/domain/messageBroker/PaymentMethodUsageListener.java` (**stays in its package — no layering moves**)
- **REMOVE**: `debt_payments/infraestructure/config/used/RabbitPaymentMethodConfig.java`
- **Tests**:
  - `catalogue/unit/paymentMethods/domain/messageBroker/PaymentMethodUsageListenerUnitTest.java` — remove `Channel`/`Message`/`DELIVERY_TAG` mocks and `verify(channel).basicAck(...)`; keep `incrementUsageCount` asserts; convert ACK test → error-path test
  - `debt_payments/.../ResourceUsagePublisherUnitTest.java` — partially: remove paymentmethod `convertAndSend` verify (leave third/costcenter intact)
- **Gate →**

### Cell 5 — `debt_payments` → `configuration/costCenters` (`costcenter.used.exchange`)
- **Modify**: `ResourceUsagePublisher.java` · `configuration/costCenters/domain/messageBroker/CostCenterUsageListener.java`
- **REMOVE**: `configuration/costCenters/domain/messageBroker/config/RabbitCostCenterUsedConfig.java`
- **Tests**: `configuration/unit/costCenters/domain/messageBroker/CostCenterUsageListenerUnitTest.java` (listener pattern) · `ResourceUsagePublisherUnitTest.java` (remove costcenter verify — **test imports the deleted config class; clean the import in the same commit**)
- **Gate →**

### Cell 6 — `debt_payments` → `configuration/thirds` (`third.used.exchange`)
- **Modify**: `ResourceUsagePublisher.java` (last block → publisher now 100% Spring events; `IJwtUtils` header plumbing droppable if unused elsewhere) · `configuration/thirds/.../ThirdUsageListener.java`
- **REMOVE**: `configuration/thirds/infrastructure/config/rabbitConfig/RabbitThirdUsedConfig.java`
- **Tests**: `ResourceUsagePublisherUnitTest.java` — **full rewrite**: `@Mock RabbitTemplate` → `@Mock ApplicationEventPublisher`, 3 asserts → `publishEvent` with event-type captors, drop all `Rabbit*Config` imports. (`ThirdUsageListener` has **no test** → add per D6)
- **Gate →**

### Cell 7 — `kardex` + `peps` → `inventory/products` (`product.used.exchange`, 2 producers, 1 queue — **one cell, atomic**)
- **Modify**: `inventory/kardex/.../messageBroker/ProductEventPublisher.java` · `inventory/peps/.../messageBroker/ProductEventPublisher.java` · `inventory/products/.../ProductUsageListener.java`
- **REMOVE**: `inventory/products/infraestructure/config/rabbitConfig/RabbitProductUsedConfig.java`
- **Tests**: `inventory/products/unit/infraestructure/output/messageBroker/ProductUsageListenerUnitTest.java` (channel/ack → error-path) · kardex/peps publisher tests don't exist → add per D6 · keep `kardex/.../recovery/ProductRecoveryActionPortUnitTest` + `peps/...` as-is (recovery logic survives)
- **Gate →**

---

## Phase 3 — `product.exchange` fanout (1 producer, 3 consumers — exchange deleted at Cell 10)

### Cell 8 — `products` → `kardex` (`product.kardex.queue`)
- **Modify**: `inventory/products/.../ProductEventPublisher.java` (block 1 of 3) · `inventory/kardex/.../ProductListener.java`
- Config `RabbitProductConfig` in products still declares peps/stock queues → **keep until Cells 9–10**
- **Tests**: `inventory/kardex/unit/infrastructure/adapters/output/messageBroker/ProductListenerUnitTest.java`
- **Gate →**

### Cell 9 — `products` → `peps` (`product.peps.queue`)
- **Modify**: `ProductEventPublisher.java` (block 2) · `inventory/peps/.../ProductListener.java`
- **REMOVE**: `inventory/peps/infrastructure/adapters/config/rabbitConfig/RabbitProductConfig.java`
- ⚠ **Naming trap (verified):** inside that file the constant is called `PRODUCT_KARDEX_QUEUE` but its value is **`"product.peps.queue"`** (line 33) — you are deleting *peps's* queue, not kardex's. The file declares **no exchange** (it binds to `productFanoutExchange` from products via `@Qualifier`), so `product.exchange` stays alive until Cell 10 ✅
- **Tests**: `inventory/peps/unit/infrastucture/output/messajeBroker/ProductListenerUnitTest.java`
- **Gate →**

### Cell 10 — `products` → `stock` (`product.stock.queue`)
- **Modify**: `ProductEventPublisher.java` (block 3) · `inventory/stock/.../StockListener.java`
- **REMOVE**: `inventory/products/infraestructure/config/rabbitConfig/RabbitProductConfig.java` (**last consumer of `product.exchange` — exchange bean goes with it**)
- **Tests**: no `StockListener` test exists → **add** `inventory/stock/.../StockListenerUnitTest` per D6 · **NEW `ProductEventFlowTest`** — replaces `inventory-weighted-average` §2 "Create product" (publishes `ProductCreated` once; asserts kardex + peps + stock all react in-process — complete only now, last consumer + exchange gone); closes the `StockListener` integration gap
- **Gate →**

---

## Phase 4 — facture → inventory

### Cell 11 — `facture` → `kardex` (`weighted.average.exchange` / `weighted.average.queue`)
- **Modify**: `accounting/facture/.../messageBroker/WeightedAverageEventPublisher.java` (port `IWeightedAverageEventPort` stays) · `inventory/kardex/.../FactureListener.java`
- **REMOVE**: `accounting/facture/.../rabbitConfig/RabbitWeightedAverageConfig.java`
- ⚠ **Misleading bean names (verified):** its beans are named `productExchange()` / `productQueue()` / `productBinding()` but they actually declare **`weighted.average.exchange` / `weighted.average.queue`** — delete the whole file; don't go looking for a "weightedAverage" bean name
- **Tests**: `inventory/kardex/unit/infrastructure/adapters/output/messageBroker/FactureListenerUnitTest.java` · **NEW `WeightedAverageFactureFlowTest`** — replaces `inventory-weighted-average` §3–6, §9–10 (31 Rabbit publishes; happy paths + kept null-field sad paths; **needs `await()` — this is the `@Async` listener**, D2) + creates its own product fixture
- **Gate →**

### Cell 12 — `facture` → `peps` (`peps.exchange` / `peps.queue`)
- **Modify**: `PepsEventPublisher.java` (port `IpepsEventPort` stays) · `inventory/peps/.../FactureListener.java`
- **REMOVE**: `accounting/facture/.../rabbitConfig/RabbitPEPSConfig.java`
- **Tests**: `inventory/peps/unit/infrastucture/output/messajeBroker/FactureListenerUnitTest.java`
- **Gate →**

---

## Phase 5 — notifications

### Cell 13 — `debt_payments` → `notifications` (`notifications.exchange` / `notifications.queue`)
- **Modify**: `debt_payments/.../adapter/InvoiceNotificationEventPublisherAdapter.java` (port `IInvoiceNotificationEventPublisher` stays) · `notifications/.../listener/NotificationEventListener.java`
- **REMOVE**: `accounting/debt_payments/infraestructure/config/RabbitNotificationsConfig.java` **and** `notifications/infrastructure/config/RabbitNotificationsConfig.java` (constants-only — both die together)
- **Tests**: no listener test → add per D6 · `notifications/MsNotificationsApplicationTests.java` must stay green · rerun `notifications/application/service/ProcessNotificationServiceTest.java`
- **Gate →**

### Cell 14 — `configuration/thirds` → `notifications` (`third.updated.exchange` / `third.updated.queue`)
- **Modify**: `configuration/thirds/.../ThirdEventPublisher.java` (port `IThirdEventPublisher` stays) · `notifications/.../ThirdEventListener.java`
- **REMOVE**: `configuration/thirds/.../rabbitConfig/RabbitThirdsEventsConfig.java` **and** `notifications/infrastructure/config/RabbitThirdsEventsConfig.java`
- **Tests**: add `ThirdEventListener` test per D6 · rerun `MsNotificationsApplicationTests`
- **Gate →**

---

## Phase 6 — invoice chain (DLX/retry chains disappear — flag explicitly in gate)

### Cell 15 — `facture` → `catalogue/accounting` (`invoice.exchange` → `invoice.accounting.queue` + `.dlx/.dlq/.retry`)
- **Modify**: `accounting/facture/.../messageBroker/ReceiptPublisher.java` (invoice block; port `IReceiptEventPort` stays) · `catalogue/accounting/.../listener/InvoiceEventListener.java` (keep `recovery/InvoiceRecoveryAction` logic inside new event handler)
- **REMOVE**: `catalogue/accounting/infraestructure/config/RabbitConfig.java` (all 5 beans: queue, dlx, dlq, retry, 3 bindings)
- **Retry semantics**: DLQ/retry chain → replaced by D3 error handler (save error + continue)
- **Tests**: no listener test → **add** covering success + port failure → `saveProcessingError` + recovery action invoked (replaces DLQ/retry test intent)
- **Gate →** — question explicitly: *"DLQ/retry chain for this cell is now gone — replaced by save-error-and-continue. OK?"*

### Cell 16 — `facture` → `debt_payments` (`invoice.exchange` → `invoice.payments.queue` + chain)
- **Modify**: `ReceiptPublisher.java` (second block) · `debt_payments/.../listener/InvoiceEventListener.java`
- **REMOVE**: `debt_payments/infraestructure/config/RabbitInvoiceConfig.java` — ⚠ **it declares the shared `rabbitTemplate()` bean**: move declaration temporarily to `commons/config/RabbitCommonConfig.java` while Cells 17–19 still publish via Rabbit (**REMOVE from `RabbitCommonConfig` in Phase 10**)
- **Tests**: add listener test per D6 · rerun `debt_payments` messageBroker tests · **NEW `InvoiceEventFlowTest`** (replaces `ms-debt-payments` §2 — 14 Rabbit publishes; closes the debt_payments `InvoiceEventListener` gap; the catalogue/accounting one stays a gap) + **NEW `ReceiptWriteOffRestFlowTest`** (moves §3 whole-scenario: inject invoice events as fixtures → `MockMvc` ReceiptController/PortfolioWriteOffController assertions)
- **Gate →**

### Cell 17 — `facture` → `treasury` (`purchase.invoice.exchange` → `invoice.treasury.queue`)
- **Two live publish paths hit this exchange — both migrate in this cell** (validated against code):
  1. **Outbox path**: `PurchaseInvoiceEventPublisher.java` (used by `PurchaseInvoiceOutboxService`) ← `SkeletonFactureService.java` (outbox call → direct publisher)
  2. **Direct path** ⚠ *previously unlisted*: `SkeletonFactureController` → `SkeletonEventService.publishPurchaseInvoiceEvent` (line 46 → line 63 `receiptEventPort.publishPurchaseInvoiceEvent`) → **`ReceiptPublisher.publishPurchaseInvoiceEvent` (lines 43–52, `convertAndSend(RabbitReceiptConfig.PURCHASE_INVOICE_EXCHANGE, ...)`)**
- **Modify**: `PurchaseInvoiceEventPublisher.java` (no new ports — transport swap only) · **`ReceiptPublisher.java` purchase block** (its invoice block was Cell 15; this 2nd `convertAndSend` had no cell owner — verified gap) · `SkeletonFactureService.java` (outbox call → direct publisher) · `SkeletonEventService.java` (port `IReceiptEventPort` stays; implementation switches to `publishEvent`) · `accounting/treasury/.../input/rabbit/TreasuryEventListener.java` PURCHASE method → `@TransactionalEventListener` (⚠ this listener does **not** extend the base; RESULT queue belongs to Cell 23)
- **REMOVE**:
  - `accounting/facture/.../rabbitConfig/RabbitReceiptConfig.java` (exchange)
  - treasury beans `treasuryInvoiceExchange`, `purchaseQueue`, `purchaseBinding` in `TreasuryRabbitConfig.java`
  - **[D7-A] `facture` outbox boilerplate entirely**: `PurchaseInvoiceOutboxService.java`, `model/PurchaseInvoiceOutboxEvent.java`, `repository/PurchaseInvoiceOutboxRepository.java` (+ table `purchase_invoice_outbox`) — consumer is same JVM, outbox pointless post-migration
- **Tests**:
  - `accounting/facture/.../SkeletonFactureServicePurchaseUnitTest.java` (mocks `PurchaseInvoiceOutboxService` → replace with direct publisher mock)
  - the **direct path** (`SkeletonEventService` → `ReceiptPublisher` purchase block) has **no test** → add a minimal one per D6 (asserts `publishEvent` + swallow-on-failure), or add it to the test-gaps list
  - `accounting/treasury/TreasuryApplicationTests.java` — **@SpringBootTest asserting "Rabbit topology resolves and wires"** → update assertion set (purchase beans gone; result/notification beans remain until Cells 21/23)
- **Gate →**

---

## Phase 7 — receipts & writeoffs

### Cell 18 — `debt_payments` → `catalogue/accounting` (`receipt.exchange` → `receipt.accounting.queue` + chain)
- **Modify**: `debt_payments/.../AccountingEventPublisher.java` (receipt blocks; port `IAccountingEventPublisher` stays) · `catalogue/accounting/.../ReceiptMessageListener.java`
- **REMOVE**: receipt beans from `debt_payments/infraestructure/config/RabbitAccountingConfig.java` (exchange, queue, dlx, dlq, retry, bindings)
- **Tests**: `debt_payments/.../AccountingEventPublisherUnitTest.java` — remove `RabbitAccountingConfig.RECEIPT_EXCHANGE` verifies (clean imports same commit); writeoff verifies stay until Cell 19 · add receipt listener test per D6
- **Gate →**

### Cell 19 — `debt_payments` → `catalogue/accounting` (`writeoff.exchange` → `writeoff.accounting.queue` + chain)
- **Modify**: `AccountingEventPublisher.java` (writeoff blocks — after this it's Rabbit-free; consider dropping `IJwtUtils` header plumbing) · `WriteOffMessageListener.java`
- **REMOVE**: writeoff beans from `RabbitAccountingConfig.java` → **file empty → delete file**
- **Tests**: `AccountingEventPublisherUnitTest.java` — **full rewrite** to `ApplicationEventPublisher` mocks, all `RabbitAccountingConfig` imports removed · add writeoff listener test per D6
- **Gate →**

---

## Phase 8 — treasury local check (informational)

### Cell 20 — no-op
- `grep -rn "convertAndSend" accounting/treasury` → confirms `TreasuryOutboxMessenger` only targets external exchanges → entire treasury publisher path belongs to Phase 9. Nothing local to migrate. **Gate →**

---

## Phase 9 — external flows — unfinished features, Rabbit removed, business code kept

> Spring events cannot cross JVM boundaries — but there's nothing to cross to: all 3 flows are
> **unfinished/unused** (no working counterpart, in development). Decision: **strip Rabbit + outbox,
> keep the non-Rabbit business code exactly as it is, and build the real transport later** when the
> feature is actually developed. No HTTP classes, no new endpoints, no REST clients now.

### Cell 21 — `treasury` → external (`treasury.accounting.exchange`, `payment.notification.exchange`)
- Only publisher in repo, **consumer is another service** (different JVM → Spring events impossible),
  and the whole outbound flow is an **unfinished, unused feature** (in development).
- **Decision: remove Rabbit + outbox; keep the business intent; no transport built now.**
- **Modify**:
  - Replace `OutboxEventAdapter` with a **tiny no-op adapter** implementing the same port
    `ITreasuryEventPublisher` — just logs the event (`log.info("treasury event [{}] (transport TBD — feature in development)", type)`)
    with a TODO comment. **No HTTP client, no REST call, nothing speculative.**
  - Port `ITreasuryEventPublisher` **kept as-is** → `PaymentVoucherService`, `PayableWriteOffService`,
    both `Transactional*UseCase` constructors **untouched** (they keep enqueuing their intent)
- **REMOVE [D7-B]**:
  - `accounting/treasury/.../messageBroker/support/TreasuryOutboxMessenger.java`
  - `accounting/treasury/.../messageBroker/OutboxEventAdapter.java` (superseded by the no-op adapter)
  - `accounting/treasury/.../jpa/entity/OutboxEventEntity.java` (table `outbox_events`)
  - `accounting/treasury/.../jpa/repository/IOutboxEventRepository.java` (incl. the dead queries)
  - `TreasuryRabbitConfig.java` remaining beans: `accountingExchange`, `notificationExchange`,
    `treasuryDlx`, `treasuryDlq` (+ `resultExchange`, `resultQueue`, `resultBinding`, `dlqBinding`
    if Cell 23 done first) — **file empty afterwards → delete file**
- **Tests**: `TreasuryApplicationTests` update (zero Rabbit beans) · service unit tests mocking
  `ITreasuryEventPublisher` keep working unchanged (port untouched) · no new test (nothing new built)
- **Gate →**

### Cell 22 — 4 unfinished usage flows (`tax.used`, `bankaccount.used`, `account.used`, `documenttype.used`)
- **Decision: dormant conversion — the flows STAY, only the transport changes.**
  Verified: **no publisher exists anywhere in this repo** (the sending half was never built —
  "left unfinished"). These listeners have never fired and cannot fire after the migration either;
  behavior is identical, feature code preserved for whoever completes it.
- **Modify** (transport swap):
  - `catalogue/taxes/domain/messageBroker/TaxUsageListener.java` — `@RabbitListener` → `@EventListener`,
    drop `Message`/`Channel`/deliveryTag params, keep D3 error block
  - `catalogue/bankAccounts/domain/messageBroker/BankAccountUsageListener.java` — same
  - `catalogue/catalogue/infraestructure/adapters/output/jpaAdapter/messageBroker/AccountUsageListener.java` — same
  - `configuration/typesOfDocuments/domain/messageBroker/DocumentTypeUsageListener.java` — same
  - **Contracts (D8)**: `TaxUsed`, `BankAccountUsed`, `AccountUsed`, `DocumentTypeUsed` records in
    `commons/events/<bc>/<submodule>/` — the missing piece a future publisher will call
    (`publishEvent(new TaxUsed(...))` — now a one-liner instead of exchange names + JSON)
- **REMOVE**: `RabbitTaxUsedConfig` · `RabbitBankAccountUsedConfig` ·
  `debt_payments/infraestructure/config/used/RabbitAccountConfig` · `RabbitDocumentTypeUsedConfig`
- **KEEP**: ports (`ITaxUsageInputPort`, `IBankAccountUsage`, `IAccountCatalogueUsagePort`,
  `IDocumentTypeUsage`), usage services, `usageCount` columns + delete guards — the feature surface
  is untouched; the counters stay permanently 0 exactly as in production today
- **Tests (all 4 exist — migrate, keep green)**:
  - `catalogue/unit/taxes/domain/messageBroker/TaxUsageListenerUnitTest.java`
  - `catalogue/unit/bankAccounts/domain/messageBroker/BankAccountUsageListenerUnitTest.java`
  - `catalogue/unit/catalogue/.../messageBroker/AccountUsageListenerUnitTest.java`
  - `configuration/unit/typesOfDocuments/domain/messageBroker/DocumentTypeUsageListenerUnitTest.java`
  - listener → error-path pattern: on success → `incrementUsageCount` invoked; on failure →
    `saveProcessingError` invoked + no exception thrown (no Channel/ACK assertions)
- **Gate →**

### Cell 23 — external → `treasury` (`accounting.result.exchange` → `accounting.treasury.result.queue`)
- **Decision: unfinished feature — Rabbit removed, business code kept, no endpoint built now.**
  Nothing publishes to `accounting.result.exchange` either (the accounting-side counterpart is the
  unfinished part); the receiving logic itself is real, non-Rabbit code.
- **Modify**:
  - `TreasuryEventListener.accounting(...)` (line 35, `@RabbitListener(RESULT_QUEUE)`) → **plain
    `@Component` method** taking `TreasuryCommands.AccountingResult` directly (drop `Message`,
    envelope DTO) — the three calls stay untouched: `vouchers.applyAccountingResult` ·
    `writeOffs.applyAccountingResult` · `schedules.applyAccountingResult`. A future REST
    controller (when the feature is developed) just builds the command and calls this method.
  - `TreasuryEventListener.invoice(...)` (PURCHASE) was migrated at Cell 17 — after both methods
    leave Rabbit, **delete the class** (logic lives on in the transactional listener from Cell 17 +
    this method) and `TreasuryRabbitDtos` envelopes die with it
- **REMOVE**: `resultQueue`, `resultBinding`, `resultExchange`, `dlqBinding` beans in
  `TreasuryRabbitConfig.java` (or the whole file if Cell 21 already emptied it) · Rabbit-only
  envelope DTOs in `TreasuryRabbitDtos`
- **Tests**: `TreasuryApplicationTests` final update (zero Rabbit beans) · no new test (no endpoint built)
- **Gate →**

---

## Phase 10 — Global teardown + full test migration

| Step | REMOVE |
|---|---|
| 1. Dependency | `pom.xml`: delete `spring-boot-starter-amqp` (line 148) |
| 2. Config | `src/main/resources/application-dev.yml` lines 63–99 (entire `spring.rabbitmq` + pool/retry/listener block) · `src/test/resources/application-test.yml` (`spring.rabbitmq.listener.simple.auto-startup: false`) |
| 3. Deployment | `docker-compose.yaml`: remove `rabbit-mq-secrets.env` line |
| 4. Listener bases (11 copies) | `AbstractMessageListener.java` in: `commons/config/base/`, `accounting/catalogue/commons/config/base/`, `catalogue/accounting/.../base/`, `debt_payments/.../base/`, `audit/.../base/`, `thirds/.../base/`, `kardex/.../base/`, `peps/.../base/`, `products/.../base/`, `stock/.../base/`, `notifications/.../base/` (or slim to non-Rabbit `AbstractEventListener` if D3 keeps the shared error template) |
| 5. Aspect/context | `commons/config/aspect/JWTContextRabbitMqAspect.java` (D2=🅰 decided — remove) · `JwtTokenService` rabbit methods (`setRabbitJwtToken`, `setRabbitTenantId`, `clearRabbitContext`, `isInRabbitContext`) |
| 6. Shared config | `commons/config/RabbitCommonConfig.java` (incl. temporary `rabbitTemplate` moved in Cell 16; its aspect test already removed at Cell 3) |
| 7. Dead ports | `debt_payments/application/output/{IAccountEventPublisher, ICostCenterEventPublisher, IThirdEventPublisher}` (never implemented; no tests reference — verified) |
| 8. Test sweep | rewrite `configuration/unit/commons/config/aspect/JwtTokenServiceUnitTest.java` rabbit-context tests to new context API · final scan: `grep -rln "org.springframework.amqp\|com.rabbitmq" src/main src/test` → **0** |
| 9. Verify | `./mvnw -q test` (full suite) · `./mvnw -q package` · boot app **with RabbitMQ stopped** and smoke: create invoice → audit + accounting listener logs fire; create product → kardex/peps/stock listeners fire |
| 10. Docs | `README.md` RabbitMQ sections · `docs/RABBITMQ_MATRIX.md` → mark all cells ✅ / "migrated to Spring Events" · `.postman/*.json` untouched (README index added at D9 — verify its cell refs say ✅) |

---

## Full test ↔ cell map (all Rabbit-touching test files)

| Test file | Cell |
|---|---|
| `keycloak/unitAudit/unitAuditSession/AuditSessionEventPublisherImplTest` | 1 |
| `configuration/unitAudit/auditCommon/AuditEventPublisherTest` | 2 |
| `inventory/products/unitAudit/AuditEventPublisherTest` | 2 |
| `keycloak/unitAudit/common/AuditEventPublisherTest` | 2 |
| `catalogue/unit/paymentMethods/domain/messageBroker/PaymentMethodUsageListenerUnitTest` | 4 |
| `configuration/unit/costCenters/domain/messageBroker/CostCenterUsageListenerUnitTest` | 5 |
| `debt_payments/.../ResourceUsagePublisherUnitTest` | 4 → 5 → **6 (rewrite)** |
| `inventory/products/.../ProductUsageListenerUnitTest` | 7 |
| `inventory/kardex/.../ProductListenerUnitTest` | 8 |
| `inventory/peps/.../ProductListenerUnitTest` | 9 |
| `inventory/kardex/.../FactureListenerUnitTest` | 11 |
| `inventory/peps/.../FactureListenerUnitTest` | 12 |
| `accounting/facture/.../SkeletonFactureServicePurchaseUnitTest` | 17 |
| `accounting/treasury/TreasuryApplicationTests` | 17 → 21 → 23 |
| `debt_payments/.../AccountingEventPublisherUnitTest` | 18 → **19 (rewrite)** |
| `catalogue/unit/taxes/domain/messageBroker/TaxUsageListenerUnitTest` | 22 |
| `catalogue/unit/bankAccounts/domain/messageBroker/BankAccountUsageListenerUnitTest` | 22 |
| `catalogue/unit/catalogue/.../messageBroker/AccountUsageListenerUnitTest` | 22 |
| `configuration/unit/typesOfDocuments/domain/messageBroker/DocumentTypeUsageListenerUnitTest` | 22 |
| `configuration/unit/commons/config/aspect/JWTContextRabbitMqAspectUnitTest` | 3 (D2=🅰 decided) |
| `configuration/unit/commons/config/aspect/JwtTokenServiceUnitTest` | 10 |
| `src/test/resources/application-test.yml` (rabbit block) | 10 |
| `audit/AuditApplicationTests` | 1 → 3 |
| `notifications/MsNotificationsApplicationTests` | 13 → 14 |

**New Java flow tests replacing Postman Rabbit scenarios** (created in-cell per D9 — full table in the [Postman → Java section](#postman--java-test-migration-rabbit-injecting-scenarios)): `AuditSessionEventFlowTest` (1) · `AuditOperationEventFlowTest` (2) · `AuditDocumentEventFlowTest` (3) · `ProductEventFlowTest` (10) · `WeightedAverageFactureFlowTest` (11) · `InvoiceEventFlowTest` + `ReceiptWriteOffRestFlowTest` (16).

**Test gaps (no test exists → add per D6):** `FactureAuditEventPublisher`, `ThirdUsageListener`, `NotificationEventListener`, `ThirdEventListener`, `InvoiceEventListener` (catalogue/accounting only — debt_payments covered by `InvoiceEventFlowTest`), `ReceiptMessageListener`, `WriteOffMessageListener`, kardex/peps `ProductEventPublisher`, `SkeletonEventService` direct purchase path (Cell 17). *(Already closed by the flow tests: `AuditSessionListener`, `AuditOperationListener`, `AuditDocumentEventDtoListener`, `StockListener`, debt_payments `InvoiceEventListener`.)*

---

## Summary

- **23 cells** in 10 phases: 3 audit → 4 used-fanout → 3 product fanout → 2 facture→inventory → 2 notifications → 3 invoice → 2 receipts → 1 no-op → 3 external Rabbit removals (unfinished flows, no new transport) → teardown.
- **Tests migrate in the same cell as their code** — never leave a cell with red tests.
- **D9 Postman → Java**: the 79 Rabbit-management-API publishes in 3 Postman collections become 7 `@SpringBootTest` flow tests built in their owning cells (1, 2, 3, 10, 11, 16); Postman collections stay frozen as historical record (+ `.postman/README.md` index of dead sections); "field missing" sad paths dropped (typed records make them compile errors), null-field + error-swallow (D3) tests kept.
- **D4 resolved (decided)**: no flows dropped, **no new transports built** — 4 unfinished `.used` listeners become **dormant Spring listeners** (Cell 22); 3 unfinished treasury flows get **Rabbit + outbox removed with business code kept** (no-op port adapter at Cell 21, plain component method at Cell 23 — real transport later, when the feature is developed).
- **D7 outbox**: **both outboxes deleted** — facture at Cell 17, treasury at Cell 21 (D7-B; no retry pump, no rethrow fix — D7-C dropped).
- **Scope guard**: transport swap only; no redesigns, no feature work, no ArchUnit; pre-existing flaws (usage ratchet, layering oddities outside Cell 3's publisher move) out of scope.
- **After every cell:** *"Cell N ✅ (code + tests) — proceed to next cell?"* → *yes / hold / skip / abort*.
