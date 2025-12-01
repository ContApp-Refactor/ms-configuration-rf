# Documentación de Pruebas de Integración - TIPOS DE DOCUMENTOS

## Introducción

Esta colección de Postman contiene pruebas de integración para el módulo de tipos de documentos de la aplicación ContApp. Las pruebas están diseñadas para validar las funcionalidades de creación, consulta, actualización y eliminación de clases de documento y tipos de documento, incluyendo casos de éxito y error.

La colección utiliza autenticación basada en tokens de Keycloak y está organizada en tres secciones principales:
- **1 Token**: Obtención del token de autenticación
- **2 Integration**: Pruebas de integración (2.1 Clases de Documento, 2.2 Tipos de Documento)
- **3 Tear Down**: Limpieza de datos creados durante las pruebas

## Variables de Colección

La colección utiliza las siguientes variables:

- `testRunId`: Identificador único de la ejecución de pruebas
- `timestamp`: Marca de tiempo de la ejecución
- `tokenKeycloak`: Token de autenticación JWT obtenido de Keycloak
- `documentClassId_1`: ID de la primera clase de documento creada
- `documentClassId_2`: ID de la segunda clase de documento creada
- `documentTypeId_1`: ID del primer tipo de documento creado
- `documentTypeId_2`: ID del segundo tipo de documento creado

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

### 2.1 Clases de Documento

#### 2.1.1 Casos correctos

##### Crear clase de documento 1 - 201

**Método:** POST  
**URL:** {{baseUrl}}/api/config/document-classes/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "name": "Clase Test 1 - {{$randomUUID}}"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta contiene la clase de documento creada', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson).to.have.property('name');
    pm.expect(responseJson).to.have.property('idEnterprise');
    pm.expect(responseJson).to.have.property('status');
    pm.expect(responseJson.status).to.be.true;
    pm.collectionVariables.set('documentClassId_1', responseJson.id);
});

console.log('[CREAR] Clase de documento 1 creada correctamente');
```

##### Crear clase de documento 2 - 201

**Método:** POST  
**URL:** {{baseUrl}}/api/config/document-classes/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "name": "Clase Test 2 - {{$randomUUID}}"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta contiene la clase de documento creada', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson).to.have.property('name');
    pm.expect(responseJson).to.have.property('idEnterprise');
    pm.expect(responseJson).to.have.property('status');
    pm.expect(responseJson.status).to.be.true;
    pm.collectionVariables.set('documentClassId_2', responseJson.id);
});

console.log('[CREAR] Clase de documento 2 creada correctamente');
```

##### Consultar clase por ID - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findById/{{documentClassId_1}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta contiene la clase de documento solicitada', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentClassId_1'));
    pm.expect(responseJson).to.have.property('name');
    pm.expect(responseJson).to.have.property('idEnterprise');
    pm.expect(responseJson).to.have.property('status');
});

console.log('[CONSULTAR] Clase de documento consultada por ID correctamente');
```

##### Listar todas las clases - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findAll/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es una pagina con contenido', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson.content).to.be.an('array');
    pm.expect(responseJson).to.have.property('page');
    pm.expect(responseJson.page).to.have.property('totalElements');
    pm.expect(responseJson.page).to.have.property('totalPages');
});

console.log('[LISTAR] Clases de documento listadas correctamente');
```

##### Listar clases con paginacion - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findAll/{{enterpriseId}}?page=0&size=5  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es una pagina con contenido paginado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson.content).to.be.an('array');
    pm.expect(responseJson.page).to.have.property('size');
    pm.expect(responseJson.page.size).to.eql(5);
    pm.expect(responseJson.page).to.have.property('number');
    pm.expect(responseJson.page.number).to.eql(0);
});

console.log('[LISTAR] Clases de documento paginadas correctamente');
```

##### Listar clases con busqueda - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findAll/{{enterpriseId}}?search=Test  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es una pagina con resultados de busqueda', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson.content).to.be.an('array');
});

console.log('[BUSCAR] Clases de documento buscadas correctamente');
```

##### Listar clases activas - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findAllActive/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta contiene solo clases activas', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson.content).to.be.an('array');
    responseJson.content.forEach(function(item) {
        pm.expect(item.status).to.be.true;
    });
});

console.log('[LISTAR ACTIVAS] Clases de documento activas listadas correctamente');
```

##### Actualizar clase de documento - 200

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/document-classes/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{documentClassId_1}},
  "idEnterprise": "{{enterpriseId}}",
  "name": "Clase Actualizada - {{$randomUUID}}"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta contiene la clase actualizada', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentClassId_1'));
    pm.expect(responseJson).to.have.property('name');
    pm.expect(responseJson.name.toLowerCase()).to.include('actualizada');
});

console.log('[ACTUALIZAR] Clase de documento actualizada correctamente');
```

##### Desactivar clase de documento - 200

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/document-classes/changeState/{{documentClassId_1}}/{{enterpriseId}}?status=false  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La clase de documento fue desactivada', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentClassId_1'));
    pm.expect(responseJson.status).to.be.false;
});

console.log('[CAMBIAR ESTADO] Clase de documento desactivada correctamente');
```

##### Activar clase de documento - 200

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/document-classes/changeState/{{documentClassId_1}}/{{enterpriseId}}?status=true  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La clase de documento fue activada', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentClassId_1'));
    pm.expect(responseJson.status).to.be.true;
});

console.log('[CAMBIAR ESTADO] Clase de documento activada correctamente');
```

#### 2.1.2 Casos incorrectos

##### Crear

###### Crear con body vacio {} - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/document-classes/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene errores de validacion para todos los campos', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('idEnterprise');
    pm.expect(responseJson.fieldErrors).to.have.property('name');
});

console.log('[CREAR] Body {} rechazado correctamente');
```

###### Crear sin idEnterprise - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/document-classes/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "name": "Clase sin empresa"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que idEnterprise es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('idEnterprise');
    pm.expect(responseJson.fieldErrors.idEnterprise.toLowerCase()).to.include('obligatori');
});

console.log('[CREAR] idEnterprise null rechazado correctamente');
```

###### Crear con idEnterprise vacio - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/document-classes/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "",
  "name": "Clase con empresa vacia"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que idEnterprise es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('idEnterprise');
    pm.expect(responseJson.fieldErrors.idEnterprise.toLowerCase()).to.include('obligatori');
});

console.log('[CREAR] idEnterprise vacio rechazado correctamente');
```

###### Crear sin name - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/document-classes/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que name es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('name');
    pm.expect(responseJson.fieldErrors.name.toLowerCase()).to.include('obligatorio');
});

console.log('[CREAR] name null rechazado correctamente');
```

###### Crear con name vacio - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/document-classes/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "name": ""
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que name es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('name');
    pm.expect(responseJson.fieldErrors.name.toLowerCase()).to.include('obligatorio');
});

console.log('[CREAR] name vacio rechazado correctamente');
```

###### Crear con name solo espacios - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/document-classes/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "name": "   "
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que name es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('name');
    pm.expect(responseJson.fieldErrors.name.toLowerCase()).to.include('obligatorio');
});

console.log('[CREAR] name con espacios rechazado correctamente');
```

##### Consultar

###### Consultar con ID inexistente - 404

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findById/999999/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
    pm.expect(responseJson.message.toLowerCase()).to.include('no encontrad');
});

console.log('[CONSULTAR] ID inexistente rechazado correctamente');
```

###### Consultar con enterpriseId inexistente - 404

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findById/{{documentClassId_1}}/empresa-inexistente  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

console.log('[CONSULTAR] enterpriseId inexistente rechazado correctamente');
```

###### Listar con paginacion negativa - 400

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findAll/{{enterpriseId}}?page=-1&size=10  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[LISTAR] Paginacion negativa rechazada correctamente');
```

###### Listar con size negativo - 400

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findAll/{{enterpriseId}}?page=0&size=-5  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[LISTAR] Size negativo rechazado correctamente');
```

###### Listar con size cero - 400

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findAll/{{enterpriseId}}?page=0&size=0  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[LISTAR] Size cero rechazado correctamente');
```

###### Listar activas con enterpriseId inexistente - 200 vacio

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findAllActive/empresa-inexistente  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es una pagina vacia', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson.content).to.be.an('array');
    pm.expect(responseJson.content.length).to.eql(0);
});

console.log('[LISTAR ACTIVAS] enterpriseId inexistente retorna lista vacia');
```

###### Listar todas con enterpriseId inexistente - 200 vacio

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-classes/findAll/empresa-inexistente  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es una pagina vacia', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson.content).to.be.an('array');
    pm.expect(responseJson.content.length).to.eql(0);
});

console.log('[LISTAR] enterpriseId inexistente retorna lista vacia');
```

##### Actualizar

###### Actualizar sin id - 400

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/document-classes/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "name": "Clase sin ID"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que id es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('id');
    pm.expect(responseJson.fieldErrors.id.toLowerCase()).to.include('obligatorio');
});

console.log('[ACTUALIZAR] id null rechazado correctamente');
```

###### Actualizar sin idEnterprise - 400

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/document-classes/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{documentClassId_1}},
  "name": "Clase sin empresa"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que idEnterprise es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('idEnterprise');
    pm.expect(responseJson.fieldErrors.idEnterprise.toLowerCase()).to.include('obligatori');
});

console.log('[ACTUALIZAR] idEnterprise null rechazado correctamente');
```

###### Actualizar con idEnterprise vacio - 400

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/document-classes/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{documentClassId_1}},
  "idEnterprise": "",
  "name": "Clase con empresa vacia"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que idEnterprise es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('idEnterprise');
    pm.expect(responseJson.fieldErrors.idEnterprise.toLowerCase()).to.include('obligatori');
});

console.log('[ACTUALIZAR] idEnterprise vacio rechazado correctamente');
```

###### Actualizar sin name - 400

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/document-classes/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{documentClassId_1}},
  "idEnterprise": "{{enterpriseId}}"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que name es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('name');
    pm.expect(responseJson.fieldErrors.name.toLowerCase()).to.include('obligatorio');
});

console.log('[ACTUALIZAR] name null rechazado correctamente');
```

###### Actualizar con name vacio - 400

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/document-classes/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{documentClassId_1}},
  "idEnterprise": "{{enterpriseId}}",
  "name": ""
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que name es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('name');
    pm.expect(responseJson.fieldErrors.name.toLowerCase()).to.include('obligatorio');
});

console.log('[ACTUALIZAR] name vacio rechazado correctamente');
```

###### Actualizar con ID inexistente - 404

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/document-classes/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": 999999,
  "idEnterprise": "{{enterpriseId}}",
  "name": "Clase con ID inexistente"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
    pm.expect(responseJson.message.toLowerCase()).to.include('no encontrad');
});

console.log('[ACTUALIZAR] ID inexistente rechazado correctamente');
```

##### Cambiar Estado

###### Cambiar estado con ID inexistente - 404

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/document-classes/changeState/999999/{{enterpriseId}}?status=false  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
    pm.expect(responseJson.message.toLowerCase()).to.include('no encontrad');
});

console.log('[CAMBIAR ESTADO] ID inexistente rechazado correctamente');
```

###### Cambiar estado con enterpriseId inexistente - 404

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/document-classes/changeState/{{documentClassId_1}}/empresa-inexistente?status=false  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
    pm.expect(responseJson.message.toLowerCase()).to.include('no encontrad');
});

console.log('[CAMBIAR ESTADO] enterpriseId inexistente rechazado correctamente');
```

###### Cambiar estado sin parametro status - 400

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/document-classes/changeState/{{documentClassId_1}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta indica error por parametro faltante', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CAMBIAR ESTADO] Sin parametro status rechazado correctamente');
```

### 2.2 Tipos de Documento

#### 2.2.1 Casos correctos

##### Crear tipo de documento 1 - 200

**Método:** POST  
**URL:** {{baseUrl}}/api/config/document-types/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "name": "Factura Venta Test - {{$randomUUID}}",
  "prefix": "FV",
  "documentClassId": {{documentClassId_1}},
  "moduleId": 3,
  "currentCounter": 1
}
```

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta contiene el tipo de documento creado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson).to.have.property('name');
    pm.expect(responseJson).to.have.property('prefix');
    pm.expect(responseJson).to.have.property('idEnterprise');
    pm.expect(responseJson).to.have.property('documentClassId');
    pm.expect(responseJson).to.have.property('moduleId');
    pm.expect(responseJson).to.have.property('status');
    pm.expect(responseJson.status).to.be.true;
    pm.collectionVariables.set('documentTypeId_1', responseJson.id);
});

console.log('[CREAR] Tipo de documento 1 creado correctamente');
```

##### Crear tipo de documento 2 - 200

**Método:** POST  
**URL:** {{baseUrl}}/api/config/document-types/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "name": "Factura Compra Test - {{$randomUUID}}",
  "prefix": "FC",
  "documentClassId": {{documentClassId_1}},
  "moduleId": 4,
  "currentCounter": 100
}
```

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta contiene el tipo de documento creado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson).to.have.property('name');
    pm.expect(responseJson).to.have.property('prefix');
    pm.expect(responseJson).to.have.property('idEnterprise');
    pm.expect(responseJson).to.have.property('documentClassId');
    pm.expect(responseJson).to.have.property('moduleId');
    pm.expect(responseJson).to.have.property('status');
    pm.expect(responseJson.status).to.be.true;
    pm.collectionVariables.set('documentTypeId_2', responseJson.id);
});

console.log('[CREAR] Tipo de documento 2 creado correctamente');
```

##### Consultar tipo por ID - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-types/findById/{{documentTypeId_1}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta contiene el tipo de documento solicitado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentTypeId_1'));
    pm.expect(responseJson).to.have.property('name');
    pm.expect(responseJson).to.have.property('prefix');
    pm.expect(responseJson).to.have.property('idEnterprise');
    pm.expect(responseJson).to.have.property('documentClassId');
    pm.expect(responseJson).to.have.property('moduleId');
    pm.expect(responseJson).to.have.property('status');
});

console.log('[CONSULTAR] Tipo de documento consultado por ID correctamente');
```

##### Listar todos los tipos - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-types/findAll/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es una pagina con contenido', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson.content).to.be.an('array');
    pm.expect(responseJson).to.have.property('page');
    pm.expect(responseJson.page).to.have.property('totalElements');
    pm.expect(responseJson.page).to.have.property('totalPages');
});

console.log('[LISTAR] Tipos de documento listados correctamente');
```

##### Listar tipos con paginacion - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-types/findAll/{{enterpriseId}}?page=0&size=5  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es una pagina con contenido paginado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson.content).to.be.an('array');
    pm.expect(responseJson.page).to.have.property('size');
    pm.expect(responseJson.page.size).to.eql(5);
    pm.expect(responseJson.page).to.have.property('number');
    pm.expect(responseJson.page.number).to.eql(0);
});

console.log('[LISTAR] Tipos de documento paginados correctamente');
```

##### Listar tipos con ordenamiento - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-types/findAll/{{enterpriseId}}?sortBy=name&order=desc  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es una pagina con contenido ordenado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson.content).to.be.an('array');
});

console.log('[LISTAR] Tipos de documento ordenados correctamente');
```

##### Listar tipos con busqueda - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-types/findAll/{{enterpriseId}}?search=Factura  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es una pagina con resultados de busqueda', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson.content).to.be.an('array');
});

console.log('[BUSCAR] Tipos de documento buscados correctamente');
```

##### Listar tipos por modulo - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-types/findAllByModule/{{enterpriseId}}?moduleId=2  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es un array de tipos por modulo', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.an('array');
});

console.log('[LISTAR POR MODULO] Tipos de documento por modulo listados correctamente');
```

##### Obtener todos los modulos - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/document-types/modules  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es un array de modulos', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.an('array');
    pm.expect(responseJson.length).to.be.above(0);
});

console.log('[MODULOS] Modulos obtenidos correctamente');
```

##### Actualizar tipo de documento - 200

**Método:** PUT  
**URL:** {{baseUrl}}/api/config/document-types/update  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "id": {{documentTypeId_1}},
  "idEnterprise": "{{enterpriseId}}",
  "name": "Tipo Actualizado - {{$randomUUID}}",
  "prefix": "TA",
  "documentClassId": {{documentClassId_1}},
  "moduleId": 3,
  "currentCounter": 50
}
```

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta contiene el tipo actualizado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentTypeId_1'));
    pm.expect(responseJson).to.have.property('name');
    pm.expect(responseJson.name.toLowerCase()).to.include('actualizado');
});

console.log('[ACTUALIZAR] Tipo de documento actualizado correctamente');
```

##### Desactivar tipo de documento - 200

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/document-types/changeState/{{documentTypeId_1}}/{{enterpriseId}}?status=false  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('El tipo de documento fue desactivado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentTypeId_1'));
    pm.expect(responseJson.status).to.be.false;
});

console.log('[CAMBIAR ESTADO] Tipo de documento desactivado correctamente');
```

##### Activar tipo de documento - 200

**Método:** PATCH  
**URL:** {{baseUrl}}/api/config/document-types/changeState/{{documentTypeId_1}}/{{enterpriseId}}?status=true  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('El tipo de documento fue activado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentTypeId_1'));
    pm.expect(responseJson.status).to.be.true;
});

console.log('[CAMBIAR ESTADO] Tipo de documento activado correctamente');
```

#### 2.2.2 Casos incorrectos

*(Los casos incorrectos para tipos de documento siguen un patrón similar a los de clases de documento, validando campos obligatorios, formatos, rangos y existencia de recursos. Incluyen validaciones para prefix, moduleId, documentClassId y otros campos específicos de tipos de documento.)*

## 3. Tear Down

### Eliminar tipo de documento 1 - 200

**Método:** DELETE  
**URL:** {{baseUrl}}/api/config/document-types/delete/{{documentTypeId_1}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('El tipo de documento fue eliminado correctamente', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentTypeId_1'));
});

console.log('[TEAR DOWN] Tipo de documento 1 eliminado correctamente');
```

### Eliminar tipo de documento 2 - 200

**Método:** DELETE  
**URL:** {{baseUrl}}/api/config/document-types/delete/{{documentTypeId_2}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('El tipo de documento fue eliminado correctamente', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentTypeId_2'));
});

console.log('[TEAR DOWN] Tipo de documento 2 eliminado correctamente');
```

### Eliminar clase de documento 1 - 200

**Método:** DELETE  
**URL:** {{baseUrl}}/api/config/document-classes/delete/{{documentClassId_1}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La clase de documento fue eliminada correctamente', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentClassId_1'));
});

console.log('[TEAR DOWN] Clase de documento 1 eliminada correctamente');
```

### Eliminar clase de documento 2 - 200

**Método:** DELETE  
**URL:** {{baseUrl}}/api/config/document-classes/delete/{{documentClassId_2}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La clase de documento fue eliminada correctamente', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.eql(pm.collectionVariables.get('documentClassId_2'));
});

console.log('[TEAR DOWN] Clase de documento 2 eliminada correctamente');
console.log('=== FIN DE PRUEBAS ===');
```

## Notas de Ejecución

1. **Orden de Ejecución**: Los tests deben ejecutarse en el orden definido en la colección para asegurar que las dependencias (como el token y IDs creados) estén disponibles.

2. **Dependencias**: 
   - El token de Keycloak debe obtenerse primero
   - Las clases de documento deben crearse antes que los tipos de documento
   - Los IDs se almacenan en variables de colección durante la ejecución
   - Las pruebas de eliminación dependen de que los datos existan previamente

3. **Entorno**: Asegurarse de que los servicios de Keycloak, la API de configuración y la base de datos estén ejecutándose antes de ejecutar las pruebas.

4. **Limpieza**: La sección "Tear Down" elimina todos los datos creados durante las pruebas para mantener el entorno limpio.

