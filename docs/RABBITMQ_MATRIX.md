# RabbitMQ Topology Matrix — `ms-configuration-rf`

> Cross-module matrix of RabbitMQ exchanges, queues and bindings in this service,
> with the supporting infrastructure files per module and the abstraction level
> of each listener/publisher (hexagonal ports vs. concrete classes).
>
> Base package: `co.unicauca.edu.co.contables`

---

## 1. Modules that use RabbitMQ

| # | Module | Role | Rabbit support |
|---|--------|------|----------------|
| M1 | `accounting/catalogue/accounting` | P + C | config, 3 listeners |
| M2 | `accounting/catalogue/catalogue` | C | 1 listener |
| M3 | `accounting/catalogue/paymentMethods` | C | 1 listener |
| M4 | `accounting/catalogue/taxes` | C | config + 1 listener |
| M5 | `accounting/catalogue/bankAccounts` | C | config + 1 listener |
| M6 | `accounting/catalogue/commons` | support | `AbstractMessageListener` base only |
| M7 | `accounting/debt_payments` | **hub** P + C | 5 configs, 3 publishers, 1 listener |
| M8 | `accounting/facture` | P | 3 configs, 4 publishers (1 in `application` layer) |
| M9 | `accounting/treasury` | P + C | config, outbox, 1 listener |
| M10 | `audit` | C | 3 configs, 3 listeners |
| M11 | `commons` | P + support | audit TopicExchange, publisher, listener container factory, JWT aspect |
| M12 | `configuration/costCenters` | C | config + listener (in `domain` layer ⚠) |
| M13 | `configuration/thirds` | P + C | 2 configs, 1 publisher, 1 listener |
| M14 | `configuration/typesOfDocuments` | C | config + listener (in `domain` layer ⚠) |
| M15 | `inventory/products` | P + C | 2 configs, 1 publisher, 1 listener |
| M16 | `inventory/kardex` | P + C | 2 listeners, 1 publisher (no own config) |
| M17 | `inventory/peps` | P + C | config, 2 listeners, 1 publisher |
| M18 | `inventory/stock` | C | 1 listener (no own config) |
| M19 | `notifications` | C | 2 constant-only configs, 2 listeners |
| M20 | `keycloak/audit` | P | 1 publisher |

**Excluded (no RabbitMQ):** `accounting/facturemanagement` (empty rabbit dirs), `accounting/catalogue/banks`, `auxiliary`, `financial`, `configuration/accountingCalendar`, `configuration/classesOfDocuments`, `configuration/enterprise`, `configuration/helpCenter`, `configuration/noCommercialTags`, `keycloak` (non-audit).

---

## 2. Matrix — rows = producer module, columns = consumer module

Cell format: `exchange (type) → queue`.
⚠ = topology declared locally but published from another module or an external service.

| Producer \ Consumer | M1 cat.acctg | M2 cat.cat. | M3 payMeth | M4 taxes | M5 bankAcc | M7 debt_pay | M9 treasury | M10 audit | M12 costC | M13 thirds | M14 docTypes | M16 kardex | M17 peps | M15 prod | M18 stock | M19 notif |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **M11 commons** | | | | | | | | `audit.exchange (topic, rk operation.event) → audit.operation.queue` | | | | | | | | |
| **M20 keycloak** | | | | | | | | `audit.exchange (topic, rk session.event) → audit.session.queue` | | | | | | | | |
| **M8 facture** | `invoice.exchange (fanout) → invoice.accounting.queue` | | | | | `invoice.exchange (fanout) → invoice.payments.queue` | `purchase.invoice.exchange (fanout) → invoice.treasury.queue` | `audit.exchange (topic, rk document.event) → audit.document.queue` | | | | `weighted.average.exchange (fanout) → weighted.average.queue` | `peps.exchange (fanout) → peps.queue` | | | |
| **M7 debt_payments** | `receipt.exchange (fanout) → receipt.accounting.queue`<br>`writeoff.exchange (fanout) → writeoff.accounting.queue` | ⚠`account.used.exchange (fanout) → account.used.queue` | `paymentmethod.used.exchange (fanout) → paymentmethod.used.queue` | | | | | | `costcenter.used.exchange (fanout) → costcenter.used.queue` | `third.used.exchange (fanout) → third.used.queue` | | | | | | `notifications.exchange (fanout) → notifications.queue` |
| **M9 treasury** | | | | | | | *(self)* `accounting.result.exchange → accounting.treasury.result.queue` ⚠ published externally | | | | | | | | | *(self)* `payment.notification.exchange`, `treasury.accounting.exchange` → **no local consumer** |
| **M13 thirds** | | | | | | | | | | *(self)* `third.updated.exchange (fanout) → third.updated.queue` | | | | | | `third.updated.exchange (fanout) → third.updated.queue` |
| **M15 products** | | | | | | | | | | | | `product.exchange (fanout) → product.kardex.queue` | `product.exchange (fanout) → product.peps.queue` | `product.exchange (fanout) → product.kardex.queue` (declared here) | `product.exchange (fanout) → product.stock.queue` | |
| **M16 kardex** | | | | | | | | | | | | | | `product.used.exchange (fanout) → product.used.queue` | | |
| **M17 peps** | | | | | | | | | | | | | | `product.used.exchange (fanout) → product.used.queue` | | |
| **M1 cat.acctg** | *(consumer only)* | | | | | | | | | | | | | | | |
| **⚠ external / not in repo** | | | | `tax.used.exchange → tax.used.queue` | `bankaccount.used.exchange → bankaccount.used.queue` | `account.used.exchange → account.used.queue` | | | | | `documenttype.used.exchange → documenttype.used.queue` | | | | | |

### Topology summary

- **21 exchanges**: 1 `topic` (`audit.exchange`) + 19 `fanout` + 1 DLX-per-chain (`treasury.dlx` and the 4 retry DLXs are fanout too).
- **24 queues**, all **durable**; no exclusive/auto-delete queues.
- **4 retry/DLX chains** (each: work queue with `x-dead-letter-exchange` → `.dlx` → `.dlq` + `.retry.queue` with `x-message-ttl`):
  - `invoice.accounting.queue` / `.dlx` / `.dlq` / `.retry.queue`
  - `invoice.payments.queue` / `.dlx` / `.dlq` / `.retry.queue`
  - `receipt.accounting.queue` / `.dlx` / `.dlq` / `.retry.queue`
  - `writeoff.accounting.queue` / `.dlx` / `.dlq` / `.retry.queue`
- Treasury: `invoice.treasury.queue` and `accounting.treasury.result.queue` dead-letter into `treasury.dlx` → `treasury.dlq`.

### Exchange catalog

| Exchange | Type | Declared by | Consumed by |
|---|---|---|---|
| `audit.exchange` | topic | `commons` | `audit` (3 queues, 3 routing keys) |
| `invoice.exchange` | fanout | `debt_payments` | `catalogue/accounting`, `debt_payments` |
| `purchase.invoice.exchange` | fanout | `facture` (also redeclared in `treasury`) | `treasury` |
| `weighted.average.exchange` | fanout | `facture` | `kardex` |
| `peps.exchange` | fanout | `facture` | `peps` |
| `receipt.exchange` | fanout | `debt_payments` | `catalogue/accounting` |
| `writeoff.exchange` | fanout | `debt_payments` | `catalogue/accounting` |
| `notifications.exchange` | fanout | `debt_payments` | `notifications` |
| `account.used.exchange` | fanout | `debt_payments` | `catalogue/catalogue` |
| `paymentmethod.used.exchange` | fanout | `debt_payments` | `catalogue/paymentMethods` |
| `third.used.exchange` | fanout | `configuration/thirds` | `configuration/thirds` |
| `costcenter.used.exchange` | fanout | `configuration/costCenters` | `configuration/costCenters` |
| `third.updated.exchange` | fanout | `configuration/thirds` | `notifications` |
| `documenttype.used.exchange` | fanout | `configuration/typesOfDocuments` | `configuration/typesOfDocuments` |
| `tax.used.exchange` | fanout | `catalogue/taxes` | `catalogue/taxes` |
| `bankaccount.used.exchange` | fanout | `catalogue/bankAccounts` | `catalogue/bankAccounts` |
| `product.exchange` | fanout | `inventory/products` | `kardex`, `peps`, `stock` |
| `product.used.exchange` | fanout | `inventory/products` | `inventory/products` |
| `accounting.result.exchange` | fanout | `treasury` | `treasury` (producer external) |
| `treasury.accounting.exchange` | fanout | `treasury` | external |
| `payment.notification.exchange` | fanout | `treasury` | external |

---

## 3. Per-module RabbitMQ infrastructure files + abstraction

Legend: ✅ abstracted via interface/hexagonal port · ❌ concrete class, direct AMQP · ⚠ layering or ownership issue

### M11 `commons` (shared)

| File | Purpose | Abstraction |
|---|---|---|
| `commons/config/RabbitAuditPublisherConfig.java` | Declares `TopicExchange audit.exchange` + routing keys | — |
| `commons/config/RabbitCommonConfig.java` | `rabbitListenerContainerFactory`, JSON converter | — |
| `commons/audit/publisher/AuditEventPublisher.java` | Publishes operation audit events | ❌ concrete `@Component`, direct `AmqpTemplate` |
| `commons/config/aspect/JWTContextRabbitMqAspect.java` | AOP: injects tenant/JWT into every `@RabbitListener` | — |
| `commons/config/base/AbstractMessageListener.java` | Base listener template (error/recovery ports) | — |

### M1 `accounting/catalogue/accounting`

| File | Purpose | Abstraction |
|---|---|---|
| `infraestructure/config/RabbitConfig.java` | `invoice.accounting.queue` + DLX/DLQ/retry | — |
| `output/messageBroker/listener/InvoiceEventListener.java` | Consumes `invoice.accounting.queue` | ✅ `extends AbstractMessageListener`, delegates to `IInvoiceProcessInputPort` |
| `output/messageBroker/listener/WriteOffMessageListener.java` | Consumes `writeoff.accounting.queue` | ✅ → `IWriteOffProcessInputPort` |
| `output/messageBroker/ReceiptMessageListener.java` | Consumes `receipt.accounting.queue` | ✅ → `IReceiptProcessInputPort` |
| `output/messageBroker/base/AbstractMessageListener.java` | Local copy of base | — |
| `output/messageBroker/recovery/InvoiceRecoveryAction.java` | Event recovery | ✅ implements `IEventRecoveryActionPort` |
| **Publisher** | none | — |

### M2–M5 catalogue usage listeners

| Module | Config | Listener | Abstraction |
|---|---|---|---|
| M2 `catalogue` | *(uses `debt_payments` config)* | `adapters/output/jpaAdapter/messageBroker/AccountUsageListener.java` | ✅ `extends AbstractMessageListener` → `IAccountCatalogueUsagePort` |
| M3 `paymentMethods` | *(uses `debt_payments` config)* | `domain/messageBroker/PaymentMethodUsageListener.java` ⚠ *in `domain` layer* | ✅ → `IPaymentMethodUsage` |
| M4 `taxes` | `domain/messageBroker/config/RabbitTaxUsedConfig.java` | `domain/messageBroker/TaxUsageListener.java` ⚠ | ✅ → `ITaxUsageInputPort` |
| M5 `bankAccounts` | `domain/messageBroker/config/RabbitBankAccountUsedConfig.java` | `domain/messageBroker/BankAccountUsageListener.java` ⚠ | ✅ → `IBankAccountUsage` |

### M6 `accounting/catalogue/commons`

| File | Purpose | Abstraction |
|---|---|---|
| `config/base/AbstractMessageListener.java` | Another copy of the shared base listener | — |

### M7 `accounting/debt_payments` (integration hub)

| File | Purpose | Abstraction |
|---|---|---|
| `infraestructure/config/RabbitAccountingConfig.java` | `receipt.exchange`, `writeoff.exchange` + queues, DLX, DLQ, retry | — |
| `infraestructure/config/RabbitInvoiceConfig.java` | Declares **`invoice.exchange`**, `invoice.payments.queue` + DLX chain, `RabbitTemplate` | — |
| `infraestructure/config/RabbitNotificationsConfig.java` | `notifications.exchange` + `notifications.queue` | — |
| `infraestructure/config/used/RabbitAccountConfig.java` | `account.used.exchange` + queue | — |
| `infraestructure/config/used/RabbitPaymentMethodConfig.java` | `paymentmethod.used.exchange` + queue | — |
| `output/messageBroker/AccountingEventPublisher.java` | Publishes receipt/writeoff events | ✅ implements `IAccountingEventPublisher` (port in `application/input`) |
| `output/messageBroker/ResourceUsagePublisher.java` | Publishes third/costcenter/paymentmethod "used" | ✅ implements `IResourceUsageNotifierPort` (`application/output`) |
| `output/messageBroker/adapter/InvoiceNotificationEventPublisherAdapter.java` | Publishes to notifications | ✅ implements `IInvoiceNotificationEventPublisher` |
| `output/messageBroker/listener/InvoiceEventListener.java` | Consumes `invoice.payments.queue` | ✅ `extends AbstractMessageListener` → error/recovery ports |
| `output/messageBroker/adapter/MessageErrorHandlingAdapter.java` | Persists processing errors (used by ~10 modules) | ✅ implements `IMessageErrorHandlingPort` |
| `domain/ports/IMessageErrorHandlingPort.java`, `IEventRecoveryActionPort.java` | Shared ports | ✅ |
| `application/output/{IAccountEventPublisher,ICostCenterEventPublisher,IThirdEventPublisher}.java` | ⚠ **dead ports** — no implementation, no usage | — |

### M8 `accounting/facture`

| File | Purpose | Abstraction |
|---|---|---|
| `adapters/config/rabbitConfig/RabbitReceiptConfig.java` | `purchase.invoice.exchange` only (avoids redeclaring others) | — |
| `adapters/config/rabbitConfig/RabbitPEPSConfig.java` | `peps.exchange` + `peps.queue` | — |
| `adapters/config/rabbitConfig/RabbitWeightedAverageConfig.java` | `weighted.average.exchange` + queue | — |
| `adapters/output/messageBroker/ReceiptPublisher.java` | Publishes invoice + purchase invoice events | ✅ implements `IReceiptEventPort` (⚠ port in `application/ports/input`) |
| `adapters/output/messageBroker/PepsEventPublisher.java` | Publishes PEPS events | ✅ implements `IpepsEventPort` |
| `adapters/output/messageBroker/WeightedAverageEventPublisher.java` | Publishes weighted average events | ✅ implements `IWeightedAverageEventPort` |
| `adapters/output/messageBroker/PurchaseInvoiceEventPublisher.java` | Publishes purchase invoice events | ❌ concrete class |
| `application/service/skeleton/audit/publisher/FactureAuditEventPublisher.java` | Publishes document audit events | ❌ concrete **and** in `application` layer (hexagonal violation) |
| `application/ports/output/FactureEventPublisher.java` + `adapters/output/eventpublisher/FactureEventPublisherAdapter.java` | Spring `ApplicationEventPublisher`, **not RabbitMQ** | ⚠ misleading name |
| **Listener** | none | — |

### M9 `accounting/treasury` (only outbox-pattern user)

| File | Purpose | Abstraction |
|---|---|---|
| `adapters/config/TreasuryRabbitConfig.java` | 5 fanout exchanges, 3 queues, DLX/DLQ, custom container factory | — |
| `adapters/output/messageBroker/OutboxEventAdapter.java` | Transactional outbox | ✅ implements `ITreasuryEventPublisher` (`application/output`) |
| `adapters/output/messageBroker/support/TreasuryOutboxMessenger.java` | Raw `RabbitTemplate` + publisher confirms | ❌ concrete class |
| `adapters/input/rabbit/TreasuryEventListener.java` | Consumes `invoice.treasury.queue`, `accounting.treasury.result.queue` | ⚠ `@RabbitListener` directly, does **not** extend `AbstractMessageListener` |
| `jpa/entity/OutboxEventEntity`, `jpa/repository/IOutboxEventRepository` | Outbox persistence | — |

### M10 `audit`

| File | Purpose | Abstraction |
|---|---|---|
| `infrastructure/config/rabbitConfig/RabbitSessionConfig.java` | `audit.session.queue` + topic binding (rk `session.event`) | — |
| `infrastructure/config/rabbitConfig/RabbitOperationConfig.java` | `audit.operation.queue` (rk `operation.event`) | — |
| `infrastructure/config/rabbitConfig/RabbitDocumentConfig.java` | `audit.document.queue` (rk `document.event`) | — |
| `adapters/input/messageBroker/AuditSessionListener.java` | Session audit consumer | ✅ `extends AbstractMessageListener` → `LogAuditSessionCommand` |
| `adapters/input/messageBroker/AuditOperationListener.java` | Operation audit consumer | ✅ → `LogAuditOperationCommand` |
| `adapters/input/messageBroker/AuditDocumentEventDtoListener.java` | Document audit consumer | ✅ → `LogAuditDocumentEventCommand` |
| `adapters/input/messageBroker/base/AbstractMessageListener.java` | Local base copy | — |
| `application/port/output/IMessageErrorHandlingPort.java` | Own error port | ✅ |
| **Publisher** | none (published by `commons`, `keycloak`, `facture`) | — |

### M12 / M14 configuration usage listeners

| Module | Config | Listener | Abstraction |
|---|---|---|---|
| M12 `costCenters` | `domain/messageBroker/config/RabbitCostCenterUsedConfig.java` | `domain/messageBroker/CostCenterUsageListener.java` ⚠ **in `domain` layer** | ✅ `extends AbstractMessageListener` → `IUsageCostCenter` |
| M14 `typesOfDocuments` | `domain/messageBroker/config/RabbitDocumentTypeUsedConfig.java` | `domain/messageBroker/DocumentTypeUsageListener.java` ⚠ | ✅ → port |

### M13 `configuration/thirds`

| File | Purpose | Abstraction |
|---|---|---|
| `infrastructure/config/rabbitConfig/RabbitThirdsEventsConfig.java` | `third.updated.exchange` + queue | — |
| `infrastructure/config/rabbitConfig/RabbitThirdUsedConfig.java` | `third.used.exchange` + queue | — |
| `adapters/output/persistence/messageBroker/ThirdEventPublisher.java` | Publishes third updated | ✅ implements `IThirdEventPublisher` (`application/ports/output`) |
| `adapters/output/persistence/messageBroker/ThirdUsageListener.java` | Consumes `third.used.queue` | ✅ `extends AbstractMessageListener` → `IThirdUsagePort` |
| `adapters/output/persistence/messageBroker/base/AbstractMessageListener.java` | Local base copy | — |

### M15 `inventory/products`

| File | Purpose | Abstraction |
|---|---|---|
| `infraestructure/config/rabbitConfig/RabbitProductConfig.java` | `product.exchange`, `product.kardex.queue`, `product.stock.queue` + bindings | — |
| `infraestructure/config/rabbitConfig/RabbitProductUsedConfig.java` | `product.used.exchange` + queue | — |
| `output/messageBroker/ProductEventPublisher.java` | Publishes product events | ✅ implements `IProductEventPort` (`application/ports/input`) |
| `output/messageBroker/ProductUsageListener.java` | Consumes `product.used.queue` | ✅ `extends AbstractMessageListener` → `IProductUsagePort` |
| `output/messageBroker/base/AbstractMessageListener.java` | Local base copy | — |

### M16 `inventory/kardex`

| File | Purpose | Abstraction |
|---|---|---|
| `adapters/output/messageBroker/FactureListener.java` | Consumes `weighted.average.queue` (imports **facture's** config) | ✅ `extends AbstractMessageListener` → `IKardexCommandPort` |
| `adapters/output/messageBroker/ProductListener.java` | Consumes `product.kardex.queue` (imports **products'** config) | ✅ → `IProductCommandRepositoryPort` |
| `adapters/output/messageBroker/ProductEventPublisher.java` | Publishes product "used" | ✅ implements `IProductEventPort` (`domain/port/product`) |
| `adapters/output/messageBroker/recovery/ProductRecoveryActionPort.java` | Event recovery | ✅ implements `IEventRecoveryActionPort` |
| `adapters/output/messageBroker/base/AbstractMessageListener.java` | Local base copy | — |
| **Own config** | none — reuses other modules' configs ⚠ | — |

### M17 `inventory/peps`

| File | Purpose | Abstraction |
|---|---|---|
| `adapters/config/rabbitConfig/RabbitProductConfig.java` | `product.peps.queue` bound to `productFanoutExchange` (bean from `products`) | — |
| `adapters/output/messageBroker/FactureListener.java` | Consumes `peps.queue` (facture's config) | ✅ `extends AbstractMessageListener` → `IKardexCommandPort` |
| `adapters/output/messageBroker/ProductListener.java` | Consumes `product.peps.queue` | ✅ → `IProductCommandOutPutPort` |
| `adapters/output/messageBroker/ProductEventPublisher.java` | Publishes product "used" | ✅ implements `IProductEventPort` (`domain/port/output/external`) |
| `adapters/output/messageBroker/recovery/ProductRecoveryActionPort.java` | Event recovery | ✅ implements `IEventRecoveryActionPort` |
| `adapters/output/messageBroker/base/AbstractMessageListener.java` | Local base copy | — |

### M18 `inventory/stock`

| File | Purpose | Abstraction |
|---|---|---|
| `adapters/output/messageBroker/StockListener.java` | Consumes `product.stock.queue` (products' config) | ✅ `extends AbstractMessageListener` → `IStockCommandRepositoryPort` |
| `adapters/output/messageBroker/base/AbstractMessageListener.java` | Local base copy | — |
| **Own config / publisher** | none | — |

### M19 `notifications`

| File | Purpose | Abstraction |
|---|---|---|
| `infrastructure/config/RabbitNotificationsConfig.java` | **Constants only** (topology owned by `debt_payments`) | — |
| `infrastructure/config/RabbitThirdsEventsConfig.java` | **Constants only** (topology owned by `thirds`) | — |
| `output/messageBroker/listener/NotificationEventListener.java` | Consumes `notifications.queue` | ✅ `extends AbstractMessageListener` → `IProcessNotificationUseCase` |
| `output/messageBroker/listener/ThirdEventListener.java` | Consumes `third.updated.queue` | ✅ → use case |
| `output/messageBroker/base/AbstractMessageListener.java` | Local base copy | — |

### M20 `keycloak/audit`

| File | Purpose | Abstraction |
|---|---|---|
| `KeycloakSessionEventPublisher.java` | Publishes session audit events to `audit.exchange` | ❌ concrete class, direct `AmqpTemplate` |

---

## 4. Key findings

1. **Publishers**: 11 of 15 are hexagonal (implement an application/domain port). **4 are not**:
   - `commons/audit/publisher/AuditEventPublisher`
   - `keycloak/audit/KeycloakSessionEventPublisher`
   - `accounting/facture/.../output/messageBroker/PurchaseInvoiceEventPublisher`
   - `accounting/facture/application/service/skeleton/audit/publisher/FactureAuditEventPublisher`
2. **Listeners**: none implement an interface — all use **inheritance** (`extends AbstractMessageListener`) plus delegation to an input port. The base class is **copy-pasted in 10 places** (`commons`, `catalogue/commons`, `debt_payments`, `audit`, `thirds`, `kardex`, `peps`, `products`, `stock`, `notifications`).
3. **Layering violations**: usage listeners live in `domain/messageBroker` (M3, M4, M5, M12, M14); `FactureAuditEventPublisher` publishes from the `application` layer, inverting the hexagonal dependency rule.
4. **Dead code**: 3 publisher interfaces in `debt_payments/application/output` (`IAccountEventPublisher`, `ICostCenterEventPublisher`, `IThirdEventPublisher`) have no implementations; `facture/FactureEventPublisher` is a Spring-events port, not RabbitMQ.
5. **Topology ownership is centralized**: `debt_payments` declares topology consumed by other modules (invoice, account, paymentmethod, notifications); `products` declares queues consumed by kardex/peps/stock; `notifications` deliberately declares nothing to avoid bean-name clashes.
6. **Only `treasury`** uses the transactional-outbox + publisher-confirms pattern; all other publishers publish directly and swallow exceptions.
7. **Inconsistent spelling**: `infraestructure` (typo) in `accounting/*` and `inventory/products`, `infrastructure` elsewhere.
