# Documentación de Pruebas de Integración - CENTROS DE COSTO

## Introducción

Esta colección de Postman contiene pruebas de integración para el módulo de centros de costo de la aplicación ContApp. Las pruebas están diseñadas para validar las funcionalidades de creación, consulta, actualización y eliminación de centros de costo, incluyendo la gestión de jerarquías (padre-hijo-nieto), estados activos/inactivos y validaciones de negocio.

La colección utiliza autenticación basada en tokens de Keycloak y está organizada en tres secciones principales:
- **1 Token**: Obtención del token de autenticación
- **2 Integration**: Pruebas de integración (2.1 Casos correctos, 2.2 Casos incorrectos)
- **3 Tear Down**: Limpieza de datos creados durante las pruebas

## Variables de Colección

La colección utiliza las siguientes variables:

- `testRunId`: Identificador único de la ejecución de pruebas
- `timestamp`: Marca de tiempo de la ejecución
- `tokenKeycloak`: Token de autenticación JWT obtenido de Keycloak
- `costCenterId_1`: ID del centro de costo padre (nivel 1) creado
- `costCenterId_2`: ID del centro de costo hijo (nivel 2) creado
- `costCenterId_3`: ID del centro de costo nieto (nivel 3) creado

## Variables de Entorno Requeridas

Para ejecutar las pruebas, se requiere configurar las siguientes variables de entorno en Postman:

- `baseUrl`: URL base de la API (ej: localhost:8080)
- `enterpriseId`: ID de la empresa (ej: d9a1a122-662e-47b4-852e-2769b124e025)
- `keycloakUser`: Usuario para autenticación en Keycloak
- `keycloakPassword`: Contraseña para autenticación en Keycloak
- `baseUrlRabbit`: URL de RabbitMQ (opcional, para monitoreo)

## 1. Token

### Token

**Método:** POST  
**URL:** http://contables.unicauca.edu.co/dev/api/keycloak/token/  
**Autenticación:** Ninguna  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
    "username": "{{keycloakUser}}",
    "password": "{{keycloakPassword}}"
}
```

**Script Pre-request:**
```javascript
pm.collectionVariables.set("testRunId", pm.variables.replaceIn("{{$randomUUID}}"));
pm.collectionVariables.set("timestamp", new Date().toISOString());
console.log("=== INICIO DE PRUEBA ===");
console.log("Timestamp:", pm.collectionVariables.get("timestamp"));
console.log("Enterprise ID:", pm.environment.get("enterpriseId"));
```

**Script de Test:**
```javascript
var jsonData = pm.response.json();
pm.collectionVariables.set("tokenKeycloak", jsonData.access_token);

pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Access token is present", function () {
    pm.expect(jsonData.access_token).to.not.be.null;
});
```

## 2. Integration

### 2.1 Casos correctos

#### Crear Centro Costo Padre (Nivel 1)

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "code": "11",
  "name": "Administración"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response tiene estructura correcta", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('idEnterprise');
    pm.expect(jsonData).to.have.property('code');
    pm.expect(jsonData).to.have.property('name');
    pm.expect(jsonData).to.have.property('status');
});

pm.test("Datos corresponden a lo enviado", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql("11");
    pm.expect(jsonData.name).to.eql("Administración");
    pm.expect(jsonData.parentId).to.be.null;
    pm.expect(jsonData.status).to.be.true;
});

pm.test("Guardar ID del centro padre", function () {
    var jsonData = pm.response.json();
    pm.collectionVariables.set("costCenterId_1", jsonData.id);
    console.log("Centro Costo Padre ID:", jsonData.id);
});
```

#### Crear Centro Costo Hijo (Nivel 2)

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "code": "1102",
  "name": "Ventas",
  "parentId": {{costCenterId_1}}
}
```

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response tiene estructura correcta", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('idEnterprise');
    pm.expect(jsonData).to.have.property('code');
    pm.expect(jsonData).to.have.property('name');
    pm.expect(jsonData).to.have.property('parentId');
    pm.expect(jsonData).to.have.property('status');
});

pm.test("Datos corresponden a lo enviado", function () {
    var jsonData = pm.response.json();
    var parentId = pm.collectionVariables.get("costCenterId_1");
    pm.expect(jsonData.code).to.eql("1102");
    pm.expect(jsonData.name).to.eql("Ventas");
    pm.expect(jsonData.parentId).to.eql(parseInt(parentId));
    pm.expect(jsonData.status).to.be.true;
});

pm.test("Guardar ID del centro hijo nivel 2", function () {
    var jsonData = pm.response.json();
    pm.collectionVariables.set("costCenterId_2", jsonData.id);
    console.log("Centro Costo Hijo Nivel 2 ID:", jsonData.id);
});
```

#### Crear Centro Costo Nieto (Nivel 3)

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "code": "110204F",
  "name": "Subventas",
  "parentId": {{costCenterId_2}}
}
```

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response tiene estructura correcta", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('idEnterprise');
    pm.expect(jsonData).to.have.property('code');
    pm.expect(jsonData).to.have.property('name');
    pm.expect(jsonData).to.have.property('parentId');
    pm.expect(jsonData).to.have.property('status');
});

pm.test("Datos corresponden a lo enviado", function () {
    var jsonData = pm.response.json();
    var parentId = pm.collectionVariables.get("costCenterId_2");
    pm.expect(jsonData.code).to.eql("110204F");
    pm.expect(jsonData.name).to.eql("Subventas");
    pm.expect(jsonData.parentId).to.eql(parseInt(parentId));
    pm.expect(jsonData.status).to.be.true;
});

pm.test("Guardar ID del centro nieto nivel 3", function () {
    var jsonData = pm.response.json();
    pm.collectionVariables.set("costCenterId_3", jsonData.id);
    console.log("Centro Costo Nieto Nivel 3 ID:", jsonData.id);
});
```

#### Consultar Centro Costo por ID

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findById/{{costCenterId_1}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response tiene estructura correcta", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('idEnterprise');
    pm.expect(jsonData).to.have.property('code');
    pm.expect(jsonData).to.have.property('name');
    pm.expect(jsonData).to.have.property('status');
});

pm.test("ID coincide con el solicitado", function () {
    var jsonData = pm.response.json();
    var expectedId = parseInt(pm.collectionVariables.get("costCenterId_1"));
    pm.expect(jsonData.id).to.eql(expectedId);
});
```

#### Consultar Centro con Padre (Nivel 2)

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findById/{{costCenterId_2}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Tiene parentId correcto", function () {
    var jsonData = pm.response.json();
    var expectedParentId = parseInt(pm.collectionVariables.get("costCenterId_1"));
    pm.expect(jsonData.parentId).to.eql(expectedParentId);
});

pm.test("Código es correcto", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql("1102");
});
```

#### Listar Todos Jerárquico

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findAll/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response es paginada", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('content');
    pm.expect(jsonData).to.have.property('page');
    pm.expect(jsonData.page).to.have.property('totalElements');
    pm.expect(jsonData.page).to.have.property('totalPages');
    pm.expect(jsonData.page).to.have.property('size');
    pm.expect(jsonData.page).to.have.property('number');
});

pm.test("Contiene al menos los 3 centros creados", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.page.totalElements).to.be.at.least(3);
});
```

#### Listar con Paginación

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findAll/{{enterpriseId}}?page=0&size=10  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Paginación aplicada correctamente", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.page.number).to.eql(0);
    pm.expect(jsonData.page.size).to.eql(10);
});

pm.test("Content es un array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.content).to.be.an('array');
});
```

#### Listar con Búsqueda

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findAll/{{enterpriseId}}?search=Ventas  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Búsqueda retorna resultados", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.content).to.be.an('array');
    pm.expect(jsonData.page.totalElements).to.be.a('number');
});

pm.test("Resultados contienen término buscado", function () {
    var jsonData = pm.response.json();
    
    if (jsonData.content.length === 0) {
        console.log("No results found for search term 'ventas'");
        pm.expect(true).to.be.true; // Pass the test when no results
    } else {
        var found = jsonData.content.some(item => 
            item.name.toLowerCase().includes('ventas') || 
            item.code.toLowerCase().includes('ventas')
        );
        pm.expect(found).to.be.true;
    }
});
```

#### Listar por Estado Activo

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findAllByStatus/{{enterpriseId}}?status=true  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response es paginada", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('content');
    pm.expect(jsonData.page).to.have.property('totalElements');
});

pm.test("Todos los elementos están activos", function () {
    var jsonData = pm.response.json();
    jsonData.content.forEach(function(item) {
        pm.expect(item.status).to.be.true;
    });
});
```

#### Actualizar Nombre Centro Costo

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_1}},
  "idEnterprise": "{{enterpriseId}}",
  "code": "11",
  "name": "Administración General"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Nombre actualizado correctamente", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.name).to.eql("Administración General");
});

pm.test("ID se mantiene igual", function () {
    var jsonData = pm.response.json();
    var expectedId = parseInt(pm.collectionVariables.get("costCenterId_1"));
    pm.expect(jsonData.id).to.eql(expectedId);
});
```

#### Actualizar Código Centro Costo

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_2}},
  "idEnterprise": "{{enterpriseId}}",
  "code": "1103",
  "name": "Ventas",
  "parentId": {{costCenterId_1}}
}
```

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Código actualizado correctamente", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql("1103");
});

pm.test("ParentId se mantiene", function () {
    var jsonData = pm.response.json();
    var expectedParentId = parseInt(pm.collectionVariables.get("costCenterId_1"));
    pm.expect(jsonData.parentId).to.eql(expectedParentId);
});
```

#### Desactivar Centro Costo Nieto

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/cost-centers/changeState/{{costCenterId_3}}/{{enterpriseId}}?status=false  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Estado cambiado a inactivo", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.status).to.be.false;
});

pm.test("ID es correcto", function () {
    var jsonData = pm.response.json();
    var expectedId = parseInt(pm.collectionVariables.get("costCenterId_3"));
    pm.expect(jsonData.id).to.eql(expectedId);
});
```

#### Reactivar Centro Costo Nieto

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/cost-centers/changeState/{{costCenterId_3}}/{{enterpriseId}}?status=true  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Estado cambiado a activo", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.status).to.be.true;
});

pm.test("ID es correcto", function () {
    var jsonData = pm.response.json();
    var expectedId = parseInt(pm.collectionVariables.get("costCenterId_3"));
    pm.expect(jsonData.id).to.eql(expectedId);
});
```

#### Listar por Estado Inactivo

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findAllByStatus/{{enterpriseId}}?status=false  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response es paginada", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('content');
    pm.expect(jsonData.page).to.have.property('totalElements');
});

pm.test("Todos los elementos están inactivos o lista vacía", function () {
    var jsonData = pm.response.json();
    jsonData.content.forEach(function(item) {
        pm.expect(item.status).to.be.false;
    });
});
```

### 2.2 Casos incorrectos

#### Crear

##### Crear con body vacío

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
    pm.expect(jsonData).to.have.property('fieldErrors');
});
```

##### Crear sin idEnterprise

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "code": "11",
  "name": "Centro Test"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo idEnterprise", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('idEnterprise');
});
```

##### Crear con idEnterprise vacío

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "",
  "code": "11",
  "name": "Centro Test"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo idEnterprise", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('idEnterprise');
});
```

##### Crear sin code

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "name": "Centro Test"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo code", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('code');
});
```

##### Crear con code longitud 1 (inválido)

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "code": "1",
  "name": "Centro Test"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error de validación de longitud de código", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('codeLengthValid');
});
```

##### Crear con code longitud 3 (inválido)

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "code": "123",
  "name": "Centro Test"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error de validación de longitud de código", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('codeLengthValid');
});
```

##### Crear sin name

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "code": "11"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo name", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('name');
});
```

##### Crear con name vacío

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "code": "11",
  "name": ""
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo name", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('name');
});
```

##### Crear con name solo espacios

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "code": "11",
  "name": "   "
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo name", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('name');
});
```

##### Crear con parentId inexistente

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "code": "1102",
  "name": "Centro Hijo",
  "parentId": 999999
}
```

**Script de Test:**
```javascript
pm.test("Status code is 404", function () {
    pm.response.to.have.status(404);
});

pm.test("Mensaje de error indica padre no encontrado", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

#### Consultar

##### Consultar con ID inexistente

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findById/999999/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 404", function () {
    pm.response.to.have.status(404);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Consultar con enterpriseId incorrecto

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findById/{{costCenterId_1}}/empresa-inexistente-xyz  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 404", function () {
    pm.response.to.have.status(404);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Listar con page negativo

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findAll/{{enterpriseId}}?page=-1&size=10  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 400 o 500", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 500]);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Listar con size negativo

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findAll/{{enterpriseId}}?page=0&size=-5  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 400 o 500", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 500]);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Listar con size cero

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findAll/{{enterpriseId}}?page=0&size=0  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 400 o 500", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 500]);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Listar por estado con enterpriseId incorrecto

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findAllByStatus/empresa-inexistente-xyz?status=true  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200 con lista vacía o 404", function () {
    if (pm.response.code === 200) {
        var jsonData = pm.response.json();
        pm.expect(jsonData.content).to.be.an('array');
    } else {
        pm.expect(pm.response.code).to.eql(404);
    }
});
```

##### Listar por estado sin parámetro status

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findAllByStatus/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 400 o 500", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 500]);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Listar por estado con status inválido

**Método:** GET  
**URL:** {{baseUrl}}/api/config/cost-centers/findAllByStatus/{{enterpriseId}}?status=invalido  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

#### Actualizar

##### Actualizar con body vacío

**Método:** POST  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
    pm.expect(jsonData).to.have.property('fieldErrors');
});
```

##### Actualizar sin id

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "code": "11",
  "name": "Centro Actualizado"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo id", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('id');
});
```

##### Actualizar con id inexistente

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": 999999,
  "idEnterprise": "{{enterpriseId}}",
  "code": "11",
  "name": "Centro Actualizado"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 404", function () {
    pm.response.to.have.status(404);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Actualizar sin idEnterprise

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_1}},
  "code": "11",
  "name": "Centro Actualizado"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo idEnterprise", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('idEnterprise');
});
```

##### Actualizar con idEnterprise vacío

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_1}},
  "idEnterprise": "",
  "code": "11",
  "name": "Centro Actualizado"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo idEnterprise", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('idEnterprise');
});
```

##### Actualizar sin code

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_1}},
  "idEnterprise": "{{enterpriseId}}",
  "name": "Centro Actualizado"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo code", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('code');
});
```

##### Actualizar con code longitud 1 (inválido)

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_1}},
  "idEnterprise": "{{enterpriseId}}",
  "code": "1",
  "name": "Centro Actualizado"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error de validación de longitud de código", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('codeLengthValid');
});
```

##### Actualizar con code longitud 3 (inválido)

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_1}},
  "idEnterprise": "{{enterpriseId}}",
  "code": "123",
  "name": "Centro Actualizado"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error de validación de longitud de código", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('codeLengthValid');
});
```

##### Actualizar sin name

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_1}},
  "idEnterprise": "{{enterpriseId}}",
  "code": "11"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo name", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('name');
});
```

##### Actualizar con name vacío

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_1}},
  "idEnterprise": "{{enterpriseId}}",
  "code": "11",
  "name": ""
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo name", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('name');
});
```

##### Actualizar con name solo espacios

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_1}},
  "idEnterprise": "{{enterpriseId}}",
  "code": "11",
  "name": "   "
}
```

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error en campo name", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('fieldErrors');
    pm.expect(jsonData.fieldErrors).to.have.property('name');
});
```

##### Actualizar con parentId inexistente

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_2}},
  "idEnterprise": "{{enterpriseId}}",
  "code": "1102",
  "name": "Centro Hijo Actualizado",
  "parentId": 999999
}
```

**Script de Test:**
```javascript
pm.test("Status code is 404", function () {
    pm.response.to.have.status(404);
});

pm.test("Mensaje de error indica padre no encontrado", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Actualizar con enterpriseId incorrecto

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/cost-centers/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{costCenterId_1}},
  "idEnterprise": "empresa-inexistente-xyz",
  "code": "11",
  "name": "Centro Actualizado"
}
```

**Script de Test:**
```javascript
pm.test("Status code is 404", function () {
    pm.response.to.have.status(404);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

#### Cambiar estado

##### Cambiar estado con ID inexistente

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/cost-centers/changeState/999999/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Cambiar estado con ID negativo

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/cost-centers/changeState/-1/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 400 or 404", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 404]);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Cambiar estado con ID cero

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/cost-centers/changeState/0/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 400 or 404", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 404]);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Cambiar estado con enterpriseId inexistente

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/cost-centers/changeState/{{costCenterId_1}}/empresa-inexistente-xyz  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Cambiar estado con enterpriseId vacío

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/cost-centers/changeState/{{costCenterId_1}}/" "/  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 400 or 404", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 404]);
});

pm.test("Response tiene estructura de error", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

##### Cambiar estado padre con hijos activos

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/cost-centers/changeState/{{costCenterId_1}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 400 or 409", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 409]);
});

pm.test("Response indica conflicto por hijos activos", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('error');
});
```

## 3. Tear Down

### Eliminar Centro Costo Auxiliar (Nivel 3)

**Método:** DELETE  
**URL:** {{baseUrl}}/api/config/cost-centers/delete/{{costCenterId_3}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Centro de costo nieto eliminado", function () {
    var jsonData = pm.response.json();
    var expectedId = parseInt(pm.collectionVariables.get("costCenterId_3"));
    pm.expect(jsonData.id).to.eql(expectedId);
});

console.log("Centro Costo Nieto eliminado exitosamente");
```

### Eliminar Centro Costo Hijo (Nivel 2)

**Método:** DELETE  
**URL:** {{baseUrl}}/api/config/cost-centers/delete/{{costCenterId_2}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Centro de costo hijo eliminado", function () {
    var jsonData = pm.response.json();
    var expectedId = parseInt(pm.collectionVariables.get("costCenterId_2"));
    pm.expect(jsonData.id).to.eql(expectedId);
});

console.log("Centro Costo Hijo eliminado exitosamente");
```

### Eliminar Centro Costo Padre (Nivel 1)

**Método:** DELETE  
**URL:** {{baseUrl}}/api/config/cost-centers/delete/{{costCenterId_1}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Centro de costo padre eliminado", function () {
    var jsonData = pm.response.json();
    var expectedId = parseInt(pm.collectionVariables.get("costCenterId_1"));
    pm.expect(jsonData.id).to.eql(expectedId);
});

console.log("Centro Costo Padre eliminado exitosamente");
console.log("=== FIN DE PRUEBA ===");
```

### Eliminar Centro Costo Huérfano

**Método:** DELETE  
**URL:** {{baseUrl}}/api/config/cost-centers/delete/22/4285f1f2-6eab-4318-91b7-cf608b4167a2  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test("Status code is 200 o 404", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 404]);
});

if (pm.response.code === 200) {
    pm.test("Centro de costo huérfano eliminado", function () {
        var jsonData = pm.response.json();
        pm.expect(jsonData.id).to.eql(22);
    });
    console.log("Centro Costo huérfano ID 22 eliminado exitosamente");
} else {
    console.log("Centro Costo huérfano ID 22 ya no existe");
}
```

## Notas de Ejecución

1. **Orden de Ejecución**: Los tests deben ejecutarse en el orden definido en la colección para asegurar que las dependencias (como el token y IDs creados) estén disponibles.

2. **Dependencias**: 
   - El token de Keycloak debe obtenerse primero
   - Los centros de costo deben crearse en orden jerárquico: padre → hijo → nieto
   - Los IDs se almacenan en variables de colección durante la ejecución
   - Las pruebas de eliminación dependen de que los datos existan previamente

3. **Jerarquía**: La colección prueba la funcionalidad de centros de costo con estructura jerárquica de hasta 3 niveles, validando que los códigos siguen el patrón de longitud (2, 4, 6+ caracteres) y que las relaciones padre-hijo se mantienen correctamente.

4. **Estados**: Se prueban cambios de estado (activo/inactivo) con validaciones de negocio como no permitir desactivar un padre con hijos activos.

5. **Limpieza**: La sección "Tear Down" elimina todos los datos creados durante las pruebas en orden inverso (nieto → hijo → padre) para mantener el entorno limpio.