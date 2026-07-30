# Microservicio de Configuración (Configuration)

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.7-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-orange)](https://www.rabbitmq.com/)
[![Apache POI](https://img.shields.io/badge/Apache%20POI-5.2.5-green)](https://poi.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)

Microservicio centralizado de configuración para el sistema contable CONTAPP, que administra 6 módulos esenciales: calendario contable, centros de costo, tipos y clases de documentos, centro de ayuda y etiquetas no comerciales. Construido con arquitectura modular por capas y soporte completo para multi-tenancy.

## 📋 Tabla de Contenidos

- [Características Principales](#-características-principales)
- [Arquitectura](#-arquitectura)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Configuración](#-instalación-y-configuración)
- [Ejecución](#-ejecución)
- [API Documentation](#-api-documentation)
- [Módulos](#-módulos)
- [Testing](#-testing)
- [CI/CD](#-cicd)
- [Contribución](#-contribución)

## 🚀 Características Principales

### Gestión Integral de Configuraciones
- **6 Módulos Principales**: Calendario contable, centros de costo, clases y tipos de documentos, ayuda, etiquetas
- **Multi-tenancy Completo**: Aislamiento total por empresa (enterpriseId)
- **Arquitectura Modular**: Cada módulo con responsabilidades claras y separadas
- **Operaciones CRUD**: Crear, leer, actualizar y eliminar configuraciones
- **Validaciones de Negocio**: Reglas específicas por módulo con manejo de errores robusto

### Calendario Contable Avanzado
- **Gestión por Períodos**: Meses y años contables con apertura y cierre
- **Validaciones de Fechas**: Control de solapamiento y consistencia temporal
- **Operaciones por Lotes**: Apertura/cierre masivo de períodos
- **Estado de Períodos**: Control de períodos abiertos/cerrados

### Centros de Costo Jerárquicos
- **Estructura Arbórea**: Soporte completo para jerarquía padre-hijo
- **Códigos Estructurados**: Validación de prefijos y formato de códigos
- **Contadores de Uso**: Tracking de utilización para eliminación segura
- **Exportación Excel**: Reportes jerárquicos en formato profesional

### Sistema de Documentos Completo
- **Clases de Documentos**: Categorización principal de documentos
- **Tipos de Documentos**: Subclasificación con módulos específicos
- **Validaciones Cruzadas**: Consistencia entre clases y tipos
- **Estados Activos/Inactivos**: Control de vigencia de documentos

### Centro de Ayuda Interactivo
- **Preguntas por Módulo**: Organización por áreas del sistema
- **Búsqueda Avanzada**: Filtros por módulo y estado
- **Gestión de Contenido**: CRUD completo de preguntas y respuestas
- **Enumeraciones de Módulos**: Catálogo controlado de áreas

### Etiquetas No Comerciales
- **Clasificación Especial**: Etiquetas para casos específicos no comerciales
- **Validaciones Únicas**: Reglas específicas para etiquetas no comerciales
- **Gestión Simplificada**: Operaciones CRUD básicas con validaciones

### Arquitectura Empresarial
- **Arquitectura por Capas**: Separación clara (DataAccess → Domain → Presentation)
- **Eventos Asíncronos**: Integración vía RabbitMQ para operaciones complejas
- **Service Discovery**: Registro automático en Eureka
- **Seguridad JWT**: Autenticación con Keycloak y OAuth2
- **Monitoreo**: Health checks y métricas con Spring Boot Actuator

## 🏗️ Arquitectura

El proyecto implementa una **arquitectura modular por capas** altamente organizada:

### Estructura General
```
configuration/
├── accountingCalendar/        # 📅 Calendario Contable
│   ├── dataAccess/           # Capa de acceso a datos
│   ├── domain/              # Lógica de negocio y modelos
│   └── presentation/        # APIs REST y DTOs
├── costCenters/             # 🏢 Centros de Costo
├── classesOfDocuments/      # 📄 Clases de Documentos
├── typesOfDocuments/        # 📋 Tipos de Documentos
├── helpCenter/              # ❓ Centro de Ayuda
├── noCommercialTags/        # 🏷️ Etiquetas No Comerciales
└── commons/                 # 🔧 Utilidades Compartidas
    ├── config/              # Configuraciones globales
    ├── exceptions/          # Excepciones comunes
    ├── multitenancy/        # Multi-tenancy
    ├── security/            # Seguridad JWT
    └── utils/               # Utilidades comunes
```

### Arquitectura por Capas
Cada módulo sigue el patrón de **arquitectura en capas**:

```
Módulo/
├── dataAccess/              # 🗄️ Capa de Infraestructura
│   ├── entity/             # Entidades JPA
│   ├── repository/         # Repositorios de datos
│   └── mapper/             # Mapeo entidad-DTO
├── domain/                  # 🎯 Dominio (Reglas de Negocio)
│   ├── models/             # Modelos de dominio
│   ├── services/           # Servicios de dominio
│   ├── mapper/             # Mapeo dominio-DTO
│   └── messageBroker/      # Eventos (opcional)
└── presentation/            # 🌐 Capa de Presentación
    ├── controller/         # Controladores REST
    └── DTO/                # Request/Response DTOs
```

### Características Arquitecturales
- **SOLID Principles**: Diseño orientado a objetos con responsabilidades claras
- **Dependency Injection**: Inyección de dependencias con Spring
- **Clean Architecture**: Separación de concerns por capas
- **Domain-Driven Design**: Modelos ricos con lógica de negocio
- **Repository Pattern**: Abstracción de acceso a datos

## 🛠️ Tecnologías

### Framework & Runtime
- **Java 17**: Lenguaje de programación con últimas características LTS
- **Spring Boot 3.4.7**: Framework principal con Spring Framework 6.x
- **Spring Cloud 2024.0.1**: Microservicios y nube

### Persistencia de Datos
- **Spring Data JPA**: Abstracción de datos con Hibernate
- **PostgreSQL**: Base de datos relacional principal
- **H2 Database**: Base de datos en memoria para testing
- **Connection Pooling**: HikariCP para optimización de conexiones

### Mensajería y Comunicación
- **RabbitMQ**: Message broker para eventos asíncronos
- **Spring AMQP**: Cliente RabbitMQ con configuración avanzada
- **Eureka Client**: Service discovery y registro automático

### Seguridad y Autenticación
- **Spring Security 6.x**: Framework de seguridad completo
- **OAuth2/OpenID Connect**: Protocolo de autenticación estándar
- **JWT (JJWT 0.9.1)**: Tokens de acceso y refresh
- **Keycloak**: Proveedor de identidad (opcional)

### Procesamiento de Documentos
- **Apache POI 5.2.5**: Generación de archivos Excel
- **File Upload**: Configuración avanzada para exportaciones

### Testing y Calidad
- **JUnit 5**: Framework de testing moderno
- **Mockito**: Mocks y stubs para testing
- **JaCoCo**: Cobertura de código con reportes detallados
- **Spring Boot Test**: Testing integrado de aplicaciones

### DevOps y Despliegue
- **Docker**: Contenedorización completa
- **Maven Wrapper**: Build consistente sin instalación
- **GitHub Actions**: CI/CD automatizado

### Utilidades y Herramientas
- **MapStruct**: Mapeo objeto-objeto type-safe
- **Lombok**: Reducción de boilerplate code
- **Spring Boot Actuator**: Monitoreo y health checks
- **PaginationHelper**: Utilidad de paginación común
- **StringStandardizationUtils**: Normalización de textos

## 📋 Requisitos Previos

### Sistema Operativo
- **Windows 10/11**, **macOS**, o **Linux**
- **Arquitectura**: x64/AMD64

### Software Base
- **Java JDK**: 17 o superior (recomendado JDK 17 LTS)
- **Maven**: 3.8+ (viene incluido el wrapper `mvnw`)
- **Git**: 2.30+ para control de versiones

### Servicios Externos (para desarrollo completo)
- **PostgreSQL**: 15+ (base de datos principal)
- **RabbitMQ**: 3.12+ (message broker)
- **Keycloak**: 20+ (proveedor de identidad)
- **Eureka Server**: Para service discovery

### Recursos del Sistema
- **RAM**: Mínimo 2GB, recomendado 4GB+
- **Disco**: 500MB libres para código y dependencias
- **Red**: Acceso a internet para dependencias Maven

## ⚙️ Instalación y Configuración

### 1. Clonación del Repositorio
```bash
git clone <repository-url>
cd configuration
```

### 2. Configuración de Variables de Entorno

#### Variables Esenciales
```bash
# Base de datos PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/general_config
DB_USER=postgres
DB_PASSWORD=your_secure_password
DB_DRIVER=org.postgresql.Driver
DB_HIBERNATE_DDL_AUTO=create-drop

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# Seguridad (Keycloak)
JWT_ISSUER_URI=http://localhost:8090/auth/realms/oauth2-realm
JWT_JWK_SET_URI=http://localhost:8090/auth/realms/oauth2-realm/protocol/openid-connect/certs
JWT_PRINCIPAL_ATTR=preferred_username
JWT_RESOURCE_ID=microservices_client

# Eureka Service Discovery
EUREKA_URL=http://localhost:8761/eureka/

# Aplicación
PORT=8080
PROFILE=dev
INSTANCE_HOSTNAME=localhost
```

#### Variables Avanzadas de RabbitMQ
```bash
# Connection pool
RABBITMQ_CONNECTION_POOL_SIZE=5
RABBITMQ_CHANNEL_POOL_SIZE=25
RABBITMQ_CHANNEL_CHECKOUT_TIMEOUT=5000

# Listener container
RABBITMQ_PREFETCH=10
RABBITMQ_CONCURRENCY=1
RABBITMQ_MAX_CONCURRENCY=5
```

### 3. Configuración de Base de Datos

#### Esquema Automático
El esquema se crea automáticamente con Hibernate:
- **Desarrollo**: `ddl-auto: create-drop` (recrea esquema en cada startup)
- **Producción**: `ddl-auto: validate` (solo validación)

#### Datos Iniciales
Cada módulo maneja sus propios datos maestros:
- **Calendario Contable**: Períodos contables por empresa
- **Centros de Costo**: Jerarquía de costos por empresa
- **Documentos**: Clases y tipos de documentos
- **Centro de Ayuda**: Preguntas y respuestas por módulo

### 4. Configuración de RabbitMQ

#### Exchanges y Queues por Módulo
```javascript
// Cost Centers - Tracking de uso
costcenter.used.exchange     // FanoutExchange para eventos de centros de costo
costcenter.used.queue        // Queue durable para mensajes de uso de centros de costo

// Document Types - Tracking de uso
documenttype.used.exchange   // FanoutExchange para eventos de tipos de documento
documenttype.used.queue      // Queue durable para mensajes de uso de tipos de documento
```

#### Configuración de Listener
```yaml
rabbitmq:
  listener:
    simple:
      acknowledge-mode: manual
      prefetch: 10
      concurrency: 1
      max-concurrency: 5

# Listeners activos:
# - CostCenterUsageListener: @RabbitListener(queues = "costcenter.used.queue")
# - DocumentTypeUsageListener: @RabbitListener(queues = "documenttype.used.queue")

```

## 🚀 Ejecución

### Desarrollo Local

#### Opción 1: Maven Directo
```bash
# Compilar y ejecutar
./mvnw spring-boot:run

# Ejecutar con perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Ejecutar tests
./mvnw test

# Build del proyecto
./mvnw clean package -DskipTests
```

#### Opción 2: Docker Standalone
```bash
# Build de imagen
docker build -t configuration .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/general_config \
  -e DB_USER=postgres \
  -e DB_PASSWORD=password \
  configuration
```

#### Opción 3: IDE (IntelliJ/Eclipse/VS Code)
- Importar como proyecto Maven
- Ejecutar `ConfigurationApplication.java`
- Configurar variables de entorno en IDE

### Producción

#### Variables de Producción
```bash
PROFILE=prod
DB_HIBERNATE_DDL_AUTO=validate
LOGGING_LEVEL_ROOT=INFO
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
```

#### Health Checks
```bash
# Health endpoint
curl http://localhost:8080/actuator/health

# Información de la aplicación
curl http://localhost:8080/actuator/info

# Métricas detalladas
curl http://localhost:8080/actuator/metrics
```

## 📚 API Documentation

### OpenAPI Specification (JSON)
```
http://localhost:8080/api-docs
```

### Postman Collections
Archivos completos en `.postman/`:
- `configuration - collection.json`: Suite completa de pruebas API
- `configuration - environment.json`: Variables de entorno

### Base URLs por Módulo
```bash
# Calendario Contable
/api/config/accounting-calendar

# Centros de Costo
/api/config/cost-centers

# Clases de Documentos
/api/config/document-classes

# Tipos de Documentos
/api/config/document-types

# Centro de Ayuda
/api/config/help-center

# Etiquetas No Comerciales
/api/config/tag
```

## 🎯 Módulos

### 1. 📅 Calendario Contable (Accounting Calendar)

**Funcionalidades:**
- Gestión de períodos contables (meses/años)
- Apertura y cierre de períodos
- Operaciones por lotes
- Validaciones de fechas y solapamiento

**Endpoints Principales:**
```http
POST   /api/config/accounting-calendar/create
POST   /api/config/accounting-calendar/open-month
POST   /api/config/accounting-calendar/open-year
GET    /api/config/accounting-calendar/year/{enterpriseId}
DELETE /api/config/accounting-calendar/delete-month
```

### 2. 🏢 Centros de Costo (Cost Centers)

**Funcionalidades:**
- Jerarquía padre-hijo completa
- Códigos estructurados con prefijos
- Exportación Excel jerárquica
- Contadores de uso y eliminación segura

**Endpoints Principales:**
```http
POST   /api/config/cost-centers/create
PUT    /api/config/cost-centers/update
GET    /api/config/cost-centers/findAll/{enterpriseId}
GET    /api/config/cost-centers/export/excel/{enterpriseId}
DELETE /api/config/cost-centers/delete/{id}/{enterpriseId}
```

### 3. 📄 Clases de Documentos (Document Classes)

**Funcionalidades:**
- Categorización principal de documentos
- Estados activo/inactivo
- Validaciones de unicidad por empresa
- Listados filtrados por estado

**Endpoints Principales:**
```http
POST   /api/config/document-classes/create
PUT    /api/config/document-classes/update
GET    /api/config/document-classes/findAll/{enterpriseId}
GET    /api/config/document-classes/findAllActive/{enterpriseId}
```

### 4. 📋 Tipos de Documentos (Document Types)

**Funcionalidades:**
- Subclasificación por módulo del sistema
- Asociación con clases de documentos
- Enumeraciones de módulos del sistema
- Búsqueda por módulo específico

**Endpoints Principales:**
```http
POST   /api/config/document-types/create
PUT    /api/config/document-types/update
GET    /api/config/document-types/findAll/{enterpriseId}
GET    /api/config/document-types/findAllByModule/{enterpriseId}
GET    /api/config/document-types/modules
```

### 5. ❓ Centro de Ayuda (Help Center)

**Funcionalidades:**
- Preguntas y respuestas por módulo
- Enumeración controlada de módulos
- Búsqueda y filtrado avanzado
- Gestión completa de contenido de ayuda

**Endpoints Principales:**
```http
POST   /api/config/help-center/create
PUT    /api/config/help-center/update
GET    /api/config/help-center/findAll
GET    /api/config/help-center/findAllByModule
GET    /api/config/help-center/modules
```

### 6. 🏷️ Etiquetas No Comerciales (No Commercial Tags)

**Funcionalidades:**
- Clasificación especial para casos no comerciales
- Validaciones específicas para etiquetas
- Gestión simplificada con operaciones CRUD básicas

**Endpoints Principales:**
```http
POST   /api/config/tag/create
PUT    /api/config/tag/update/{idTag}
GET    /api/config/tag/findAll/{enterpriseId}
DELETE /api/config/tag/deletetag/{idTag}
```

### Características Comunes de Todos los Módulos

#### Multi-tenancy
- **Aislamiento por Empresa**: Todos los datos separados por `enterpriseId`
- **Contexto Automático**: Tenant resolver integrado
- **Validaciones Cruzadas**: Consistencia dentro de cada tenant

#### Operaciones CRUD Estándar
- **Create**: Creación con validaciones de negocio
- **Read**: Consultas paginadas y filtradas
- **Update**: Actualización con control de cambios
- **Delete**: Eliminación segura con verificación de uso

#### Manejo de Errores
- **Excepciones Específicas**: Más de 25 tipos de excepciones por módulo
- **Códigos de Error**: Error codes únicos por tipo de error
- **Mensajes Descriptivos**: Información detallada para debugging
- **Global Exception Handler**: Manejo centralizado de errores

## 🧪 Testing

### Cobertura de Código
```bash
# Ejecutar tests con cobertura
./mvnw clean test jacoco:report

# Ver reporte HTML
open target/site/jacoco/index.html

# Cobertura mínima configurada: 0% (para desarrollo flexible)
```

### Estructura de Tests
```
src/test/java/
├── unit/
│   ├── domain/              # Tests de lógica de negocio
│   ├── infrastructure/      # Tests de infraestructura
│   └── application/         # Tests de servicios
└── integration/             # Tests de integración (futuro)
```

### Tests por Módulo
- **36+ archivos de test** distribuidos en los 6 módulos
- **Cobertura completa**: Modelos, servicios, controladores, mappers
- **Tests unitarios**: Con mocks para dependencias externas
- **Tests de infraestructura**: Base de datos, RabbitMQ, security

### Testing con Postman
```bash
# Ejecutar colección completa
newman run .postman/configuration\ -\ collection.json \
  -e .postman/configuration\ -\ environment.json
```

### Tests de Integración
- **API Testing**: Endpoints REST completos
- **Database Testing**: Validación de persistencia
- **Security Testing**: Autenticación y autorización
- **Performance**: Carga y estrés en operaciones críticas

## 🔄 CI/CD

### GitHub Actions Workflows

#### Pipeline de Desarrollo
```yaml
# on-push-to-dev.yaml
- Build automático en push a develop
- Tests completos con JaCoCo
- Build de imagen Docker
- Push a Docker Hub
- Notificación de resultados
```

#### Pipeline de Integración
```yaml
# on-pull-request-to-dev.yaml
- Validación de PRs
- Tests unitarios e integración
- Code quality checks
- Testing con Postman/Newman
- Merge automático tras aprobación
```

### Estrategia de Branches
```
main (producción)
├── develop (desarrollo)
│   ├── feature/calendar-management
│   ├── feature/cost-centers-hierarchy
│   ├── feature/document-types
│   └── feature/help-center
└── hotfix/config-validation
```

### Despliegue Continuo
- **Build Automático**: En cada push/merge a develop
- **Testing Automatizado**: Suite completa de tests
- **Docker Images**: Generación automática de contenedores
- **Environment Promotion**: develop → staging → production

## 🤝 Contribución

### Estándares de Desarrollo
1. **Arquitectura por Capas**: Mantener separación clara en cada módulo
2. **Multi-tenancy**: Siempre considerar el contexto de empresa
3. **Testing**: Tests unitarios para toda nueva funcionalidad
4. **Documentación**: README actualizado con nuevas características

### Proceso de Desarrollo
1. **Crear rama**: `git checkout -b feature/nombre-modulo-funcionalidad`
2. **Implementar**: Seguir arquitectura por capas y principios SOLID
3. **Testing**: Cobertura completa con tests unitarios
4. **Pull Request**: Descripción detallada y testing con Postman
5. **Code Review**: Aprobación requerida antes del merge
6. **Merge**: Automático tras CI/CD exitoso

### Convenciones de Código
- **Lenguaje**: Español para comentarios, inglés para código
- **Nombres**: camelCase para variables/métodos, PascalCase para clases
- **Imports**: Organizados automáticamente
- **Formato**: Google Java Style Guide

### Documentación
- **JavaDoc**: Comentarios completos en clases públicas
- **README**: Actualización obligatoria en cambios significativos
- **Postman**: Mantenimiento de colecciones de testing

### Módulos y Responsabilidades
Cada módulo debe mantener su **responsabilidad única**:
- **AccountingCalendar**: Solo gestión de períodos contables
- **CostCenters**: Solo jerarquía y gestión de centros de costo
- **DocumentClasses/Types**: Solo categorización de documentos
- **HelpCenter**: Solo contenido de ayuda
- **NoCommercialTags**: Solo etiquetas especiales


---

**Nota**: Este microservicio forma parte integral del ecosistema CONTAPP, proporcionando la configuración centralizada para todos los módulos del sistema contable. Su arquitectura modular permite escalabilidad independiente por módulo y facilita el mantenimiento de configuraciones críticas del negocio.
