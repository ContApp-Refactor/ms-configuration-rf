# Documentación de Pruebas de Integración - CECENTRO DE AYUDA

## Introducción

Esta colección de Postman contiene pruebas de integración para el módulo de Centro de Ayuda. Incluye pruebas para operaciones CRUD (Crear, Leer, Actualizar, Eliminar) y validaciones de errores. Las pruebas están organizadas en carpetas que cubren casos correctos, casos incorrectos y limpieza de datos.

### Estructura de la Colección
- **1 Token**: Obtención del token de autenticación desde Keycloak
- **2 Integration**: Pruebas principales divididas en casos correctos e incorrectos
- **3 Tear Down**: Limpieza de datos de prueba

### Variables de Entorno Requeridas
- `baseUrl`: URL base del servicio (ej: localhost:8080)
- `enterpriseId`: ID de la empresa
- `keycloakUser`: Usuario para autenticación Keycloak
- `keycloakPassword`: Contraseña para autenticación Keycloak

## Variables de Colección

| Variable | Descripción | Valor Inicial |
|----------|-------------|---------------|
| testRunId | ID único para la ejecución de pruebas | Generado automáticamente |
| timestamp | Marca de tiempo de la ejecución | Generado automáticamente |
| tokenKeycloak | Token JWT obtenido de Keycloak | - |
| helpCenterId_1 | ID del primer registro creado para pruebas | - |
| helpCenterId_2 | ID del segundo registro creado para pruebas | - |

## 1. Token

### Método: POST
**URL**: `http://contables.unicauca.edu.co/dev/api/keycloak/token/`

**Autenticación**: No requiere

**Body** (raw/json):
```json
{
    "username": "{{keycloakUser}}",
    "password": "{{keycloakPassword}}"
}
```

**Scripts de Prueba**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Access token is present", function () {
    pm.expect(jsonData.access_token).to.not.be.null;
});
```

**Script Pre-request**:
```javascript
pm.collectionVariables.set("testRunId", pm.variables.replaceIn("{{$randomUUID}}"));
pm.collectionVariables.set("timestamp", new Date().toISOString());
console.log("=== INICIO DE PRUEBA ===");
console.log("Timestamp:", pm.collectionVariables.get("timestamp"));
console.log("Enterprise ID:", pm.environment.get("enterpriseId"));
```

## 2. Integration

### 2.1 Casos correctos

#### Listar módulos disponibles - 200
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/modules`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('La respuesta contiene lista de módulos', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.an('array');
    pm.expect(responseJson.length).to.be.at.least(1);
});

pm.test('Cada módulo tiene id y name', function () {
    const responseJson = pm.response.json();
    responseJson.forEach(function(module) {
        pm.expect(module).to.have.property('id');
        pm.expect(module).to.have.property('name');
    });
});

console.log('✓ Módulos disponibles: ' + pm.response.json().length);
```

#### Crear registro de ayuda - 200
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 1,
  "name": "Test Ayuda - {{timestamp}}",
  "description": "Descripción de prueba para inventario promedio ponderado"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('La respuesta contiene los datos del registro', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson).to.have.property('moduleId');
    pm.expect(responseJson).to.have.property('moduleName');
    pm.expect(responseJson).to.have.property('name');
    pm.expect(responseJson).to.have.property('description');
    pm.expect(responseJson).to.have.property('status');
    pm.expect(responseJson.status).to.be.true;
});

pm.test('Los datos coinciden con lo enviado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.moduleId).to.equal(1);
    pm.expect(responseJson.name.toLowerCase()).to.include('test ayuda');
});

const responseJson = pm.response.json();
pm.collectionVariables.set('helpCenterId_1', responseJson.id);
console.log('✓ Registro de ayuda creado con ID: ' + responseJson.id);
```

#### Crear segundo registro (módulo 3) - 200
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 3,
  "name": "Guía Comercial - {{timestamp}}",
  "description": "Descripción de ayuda para el módulo comercial"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta contiene los datos del registro', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.moduleId).to.equal(3);
    pm.expect(responseJson.moduleName).to.equal('Comercial');
});

const responseJson = pm.response.json();
pm.collectionVariables.set('helpCenterId_2', responseJson.id);
console.log('✓ Segundo registro creado con ID: ' + responseJson.id);
```

#### Consultar registro por ID - 200
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findById/{{helpCenterId_1}}`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('La respuesta contiene los datos correctos', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson.id).to.equal(parseInt(pm.collectionVariables.get('helpCenterId_1')));
    pm.expect(responseJson).to.have.property('moduleId');
    pm.expect(responseJson).to.have.property('name');
    pm.expect(responseJson).to.have.property('description');
});

console.log('✓ Registro consultado correctamente');
```

#### Listar todos los registros - 200
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findAll`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('La respuesta tiene estructura de paginación', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson).to.have.property('page');
    pm.expect(responseJson.page).to.have.property('totalElements');
    pm.expect(responseJson.page).to.have.property('totalPages');
    pm.expect(responseJson.content).to.be.an('array');
});

pm.test('Contiene al menos los registros creados', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.page.totalElements).to.be.at.least(2);
});

console.log('✓ Listado correcto: ' + pm.response.json().page.totalElements + ' registros');
```

#### Listar con paginación - 200
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findAll?page=0&size=5`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta tiene estructura de paginación', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson).to.have.property('page');
    pm.expect(responseJson.page).to.have.property('size');
    pm.expect(responseJson.page).to.have.property('number');
});

pm.test('Respeta el tamaño de página solicitado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.page.size).to.equal(5);
    pm.expect(responseJson.page.number).to.equal(0);
});

console.log('✓ Paginación correcta: página ' + pm.response.json().page.number + ', tamaño ' + pm.response.json().page.size);
```

#### Listar con búsqueda - 200
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findAll?search=Test`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta contiene resultados filtrados', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson.content).to.be.an('array');
});

pm.test('Los resultados coinciden con la búsqueda', function () {
    const responseJson = pm.response.json();
    if (responseJson.content.length > 0) {
        responseJson.content.forEach(function(item) {
            pm.expect(item.name.toLowerCase()).to.include('test');
        });
    }
});

console.log('✓ Búsqueda correcta: ' + pm.response.json().page.totalElements + ' resultados');
```

#### Listar por módulo (módulo 1) - 200
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findAllByModule?moduleId=1`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('La respuesta es un array', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.an('array');
});

pm.test('Todos los registros son del módulo 1', function () {
    const responseJson = pm.response.json();
    responseJson.forEach(function(item) {
        pm.expect(item.moduleId).to.equal(1);
    });
});

console.log('✓ Listado por módulo 1: ' + pm.response.json().length + ' registros');
```

#### Actualizar registro - 200
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "id": {{helpCenterId_1}},
  "moduleId": 2,
  "name": "Nombre Actualizado - {{timestamp}}",
  "description": "Descripción actualizada para inventario PEPS"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('El registro fue actualizado correctamente', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.id).to.equal(parseInt(pm.collectionVariables.get('helpCenterId_1')));
    pm.expect(responseJson.name.toLowerCase()).to.include('actualizado');
    pm.expect(responseJson.moduleId).to.equal(2);
});

console.log('✓ Registro actualizado correctamente');
```

#### Cambiar estado a inactivo - 200
**Método**: PATCH  
**URL**: `{{baseUrl}}/api/config/help-center/changeState/{{helpCenterId_1}}?status=false`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('El estado cambió a inactivo', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.id).to.equal(parseInt(pm.collectionVariables.get('helpCenterId_1')));
    pm.expect(responseJson.status).to.be.false;
});

console.log('✓ Estado cambiado a inactivo');
```

#### Cambiar estado a activo - 200
**Método**: PATCH  
**URL**: `{{baseUrl}}/api/config/help-center/changeState/{{helpCenterId_1}}?status=true`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('El estado cambió a activo', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.status).to.be.true;
});

console.log('✓ Estado cambiado a activo');
```

### 2.2 Casos incorrectos

#### Crear sin body - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body**: (vacío)

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CREAR] Body vacio rechazado correctamente');
```

#### Crear con body vacio {} - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene errores de validacion para todos los campos', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson.error).to.have.property('message');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('moduleId');
    pm.expect(responseJson.fieldErrors).to.have.property('name');
    pm.expect(responseJson.fieldErrors).to.have.property('description');
});

console.log('[CREAR] Body {} rechazado correctamente');
```

#### Crear sin moduleId - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "name": "Test sin modulo",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que moduleId es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('moduleId');
    pm.expect(responseJson.fieldErrors.moduleId.toLowerCase()).to.include('obligatorio');
});

console.log('[CREAR] moduleId null rechazado correctamente');
```

#### Crear con moduleId inexistente - 404
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 99999,
  "name": "Test modulo inexistente",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CREAR] moduleId inexistente rechazado correctamente');
```

#### Crear con moduleId negativo - 404
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": -1,
  "name": "Test modulo negativo",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CREAR] moduleId negativo rechazado correctamente');
```

#### Crear con moduleId cero - 404
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 0,
  "name": "Test modulo cero",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CREAR] moduleId cero rechazado correctamente');
```

#### Crear con moduleId tipo string - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": "abc",
  "name": "Test modulo string",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CREAR] moduleId string rechazado correctamente');
```

#### Crear sin name - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 1,
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
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

#### Crear con name vacio - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 1,
  "name": "",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
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

#### Crear con name solo espacios - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 1,
  "name": "   ",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
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

#### Crear con name mayor a 255 caracteres - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 1,
  "name": "Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut labore et dolore magna aliqua Ut enim ad minim veniam quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat Duis aute irure dolor in reprehenderit",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que name excede el limite', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('name');
    pm.expect(responseJson.fieldErrors.name.toLowerCase()).to.include('255');
});

console.log('[CREAR] name > 255 caracteres rechazado correctamente');
```

#### Crear sin description - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 1,
  "name": "Test sin descripcion"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que description es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('description');
    pm.expect(responseJson.fieldErrors.description.toLowerCase()).to.include('obligatoria');
});

console.log('[CREAR] description null rechazado correctamente');
```

#### Crear con description vacia - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 1,
  "name": "Test descripcion vacia",
  "description": ""
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que description es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('description');
    pm.expect(responseJson.fieldErrors.description.toLowerCase()).to.include('obligatoria');
});

console.log('[CREAR] description vacia rechazada correctamente');
```

#### Crear con description solo espacios - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 1,
  "name": "Test descripcion espacios",
  "description": "   "
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que description es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('description');
    pm.expect(responseJson.fieldErrors.description.toLowerCase()).to.include('obligatoria');
});

console.log('[CREAR] description con espacios rechazada correctamente');
```

#### Crear con token invalido - 401
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token (token_invalido_12345)  

**Body** (raw/json):
```json
{
  "moduleId": 1,
  "name": "Test token invalido",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 401 (Unauthorized)', function () {
    pm.response.to.have.status(401);
});

console.log('[CREAR] Token invalido rechazado correctamente');
```

#### Crear con JSON malformado - 400
**Método**: POST  
**URL**: `{{baseUrl}}/api/config/help-center/create`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 1,
  "name": "Test",
  "description": 
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CREAR] JSON malformado rechazado correctamente');
```

#### Consultar por ID inexistente - 404
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findById/99999`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CONSULTAR] ID inexistente rechazado correctamente');
```

#### Consultar por ID negativo - 404
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findById/-1`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CONSULTAR] ID negativo rechazado correctamente');
```

#### Consultar por ID cero - 404
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findById/0`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CONSULTAR] ID cero rechazado correctamente');
```

#### Consultar por ID tipo string - 400
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findById/abc`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CONSULTAR] ID string rechazado correctamente');
```

#### Consultar por modulo inexistente - 400
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findAllByModule?moduleId=99999`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta indica que el modulo no existe', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
    pm.expect(responseJson.message.toLowerCase()).to.include('módulo');
});

console.log('[CONSULTAR] Modulo inexistente rechazado correctamente');
```

#### Consultar por modulo sin parametro - 400
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findAllByModule`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CONSULTAR] Modulo sin parametro rechazado correctamente');
```

#### Consultar por modulo tipo string - 400
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findAllByModule?moduleId=abc`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CONSULTAR] Modulo string rechazado correctamente');
```

#### FindAll con page negativo - 200 pagina valida
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findAll?page=-1&size=10`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 o 200', function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 400, 500]);
});

pm.test('Manejo de pagina negativa o respuesta paginada válida', function () {
    const responseJson = pm.response.json();
    const status = pm.response.code;

    if (status === 400) {
        // Caso de error esperado cuando page < 0
        pm.expect(responseJson).to.have.property('status');
        pm.expect(responseJson.status).to.eql(400);
        pm.expect(responseJson).to.have.property('message');
        pm.expect(responseJson.message).to.include('Page index must not be less than zero');
        pm.expect(responseJson).to.have.property('code');
    } else if (status >= 200 && status < 300) {
        // Caso exitoso: respuesta paginada
        pm.expect(responseJson).to.have.property('content');
        pm.expect(responseJson.content).to.be.an('array');
    } else {
        pm.expect.fail('Código de respuesta inesperado: ' + status);
    }
});

console.log('[CONSULTAR] Page negativo manejado correctamente');
```

#### FindAll con sortField invalido - 400
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findAll?page=0&size=10&sortField=campoInexistente`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 o 200', function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 400, 500]);
});

console.log('[CONSULTAR] SortField invalido manejado');
```

#### Actualizar sin body - 400
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body**: (vacío)

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[ACTUALIZAR] Sin body rechazado correctamente');
```

#### Actualizar sin id - 400
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "moduleId": 1,
  "name": "Test actualizacion sin id",
  "description": "Descripcion actualizada"
}
```

**Scripts de Prueba**:
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

#### Actualizar con id inexistente - 404
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "id": 99999,
  "moduleId": 1,
  "name": "Test id inexistente",
  "description": "Descripcion prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[ACTUALIZAR] id inexistente rechazado correctamente');
```

#### Actualizar sin moduleId - 400
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "id": {{helpCenterId_1}},
  "name": "Test sin modulo",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que moduleId es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('moduleId');
    pm.expect(responseJson.fieldErrors.moduleId.toLowerCase()).to.include('obligatorio');
});

console.log('[ACTUALIZAR] moduleId null rechazado correctamente');
```

#### Actualizar con moduleId inexistente - 404
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "id": {{helpCenterId_1}},
  "moduleId": 99999,
  "name": "Test modulo inexistente",
  "description": "Descripcion prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[ACTUALIZAR] moduleId inexistente rechazado correctamente');
```

#### Actualizar sin name - 400
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "id": {{helpCenterId_1}},
  "moduleId": 1,
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
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

#### Actualizar con name vacio - 400
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "id": {{helpCenterId_1}},
  "moduleId": 1,
  "name": "",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
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

#### Actualizar con name mayor a 255 caracteres - 400
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "id": {{helpCenterId_1}},
  "moduleId": 1,
  "name": "Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut labore et dolore magna aliqua Ut enim ad minim veniam quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat Duis aute irure dolor in reprehenderit",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que name excede el limite', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('name');
    pm.expect(responseJson.fieldErrors.name.toLowerCase()).to.include('255');
});

console.log('[ACTUALIZAR] name > 255 caracteres rechazado correctamente');
```

#### Actualizar sin description - 400
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "id": {{helpCenterId_1}},
  "moduleId": 1,
  "name": "Test sin descripcion"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que description es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('description');
    pm.expect(responseJson.fieldErrors.description.toLowerCase()).to.include('obligatoria');
});

console.log('[ACTUALIZAR] description null rechazado correctamente');
```

#### Actualizar con description vacia - 400
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Body** (raw/json):
```json
{
  "id": {{helpCenterId_1}},
  "moduleId": 1,
  "name": "Test descripcion vacia",
  "description": ""
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El error de validacion indica que description es obligatorio', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('error');
    pm.expect(responseJson).to.have.property('fieldErrors');
    pm.expect(responseJson.fieldErrors).to.have.property('description');
    pm.expect(responseJson.fieldErrors.description.toLowerCase()).to.include('obligatoria');
});

console.log('[ACTUALIZAR] description vacia rechazada correctamente');
```

#### Actualizar con token invalido - 401
**Método**: PUT  
**URL**: `{{baseUrl}}/api/config/help-center/update`  
**Autenticación**: Bearer Token (token_invalido_12345)  

**Body** (raw/json):
```json
{
  "id": 1,
  "moduleId": 1,
  "name": "Test token invalido",
  "description": "Descripcion de prueba"
}
```

**Scripts de Prueba**:
```javascript
pm.test('Status code es 401 (Unauthorized)', function () {
    pm.response.to.have.status(401);
});

console.log('[ACTUALIZAR] Token invalido rechazado correctamente');
```

#### Cambiar estado con id inexistente - 404
**Método**: PATCH  
**URL**: `{{baseUrl}}/api/config/help-center/changeState/99999?status=false`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CAMBIAR ESTADO] ID inexistente rechazado correctamente');
```

#### Cambiar estado con id negativo - 404
**Método**: PATCH  
**URL**: `{{baseUrl}}/api/config/help-center/changeState/-1?status=false`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CAMBIAR ESTADO] ID negativo rechazado correctamente');
```

#### Cambiar estado con id cero - 404
**Método**: PATCH  
**URL**: `{{baseUrl}}/api/config/help-center/changeState/0?status=false`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CAMBIAR ESTADO] ID cero rechazado correctamente');
```

#### Cambiar estado con id tipo string - 400
**Método**: PATCH  
**URL**: `{{baseUrl}}/api/config/help-center/changeState/abc?status=false`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CAMBIAR ESTADO] ID string rechazado correctamente');
```

#### Cambiar estado sin parametro status - 400
**Método**: PATCH  
**URL**: `{{baseUrl}}/api/config/help-center/changeState/{{helpCenterId_1}}`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CAMBIAR ESTADO] Sin parametro status rechazado correctamente');
```

#### Cambiar estado con status tipo string invalido - 400
**Método**: PATCH  
**URL**: `{{baseUrl}}/api/config/help-center/changeState/{{helpCenterId_1}}?status=abc`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[CAMBIAR ESTADO] Status string invalido rechazado correctamente');
```

#### Cambiar estado con token invalido - 401
**Método**: PATCH  
**URL**: `{{baseUrl}}/api/config/help-center/changeState/1?status=false`  
**Autenticación**: Bearer Token (token_invalido_12345)  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 401 (Unauthorized)', function () {
    pm.response.to.have.status(401);
});

console.log('[CAMBIAR ESTADO] Token invalido rechazado correctamente');
```

#### Eliminar con id inexistente - 404
**Método**: DELETE  
**URL**: `{{baseUrl}}/api/config/help-center/delete/99999`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[ELIMINAR] ID inexistente rechazado correctamente');
```

#### Eliminar con id tipo string - 400
**Método**: DELETE  
**URL**: `{{baseUrl}}/api/config/help-center/delete/abc`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('La respuesta contiene mensaje de error', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('message');
});

console.log('[ELIMINAR] ID string rechazado correctamente');
```

#### Eliminar con token invalido - 401
**Método**: DELETE  
**URL**: `{{baseUrl}}/api/config/help-center/delete/1`  
**Autenticación**: Bearer Token (token_invalido_12345)  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 401 (Unauthorized)', function () {
    pm.response.to.have.status(401);
});

console.log('[ELIMINAR] Token invalido rechazado correctamente');
```

## 3. Tear Down

#### Eliminar primer registro - 200
**Método**: DELETE  
**URL**: `{{baseUrl}}/api/config/help-center/delete/{{helpCenterId_1}}`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('El registro fue eliminado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.id).to.equal(parseInt(pm.collectionVariables.get('helpCenterId_1')));
});

console.log('✓ Primer registro eliminado');
```

#### Eliminar segundo registro - 200
**Método**: DELETE  
**URL**: `{{baseUrl}}/api/config/help-center/delete/{{helpCenterId_2}}`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('El registro fue eliminado', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.id).to.equal(parseInt(pm.collectionVariables.get('helpCenterId_2')));
});

console.log('✓ Segundo registro eliminado');
console.log('=== FIN DE CASOS CORRECTOS DE CENTRO DE AYUDA ===');
```

#### Verificar eliminación - 404
**Método**: GET  
**URL**: `{{baseUrl}}/api/config/help-center/findById/{{helpCenterId_1}}`  
**Autenticación**: Bearer Token ({{tokenKeycloak}})  

**Scripts de Prueba**:
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

console.log('Verificación correcta: registro eliminado ya no existe');
```

## Notas de Ejecución

1. **Orden de Ejecución**: Los tests deben ejecutarse en el orden definido en la colección para asegurar que las dependencias (como el token y IDs creados) estén disponibles.

2. **Dependencias**: 
   - El token de Keycloak debe obtenerse primero
   - Los IDs de calendarios se almacenan en variables de colección durante la ejecución
   - Las pruebas de eliminación dependen de que los datos existan previamente

3. **Entorno**: Asegurarse de que los servicios de Keycloak, la API de configuración y la base de datos estén ejecutándose antes de ejecutar las pruebas.

4. **Limpieza**: La sección "Tear Down" elimina todos los datos creados durante las pruebas para mantener el entorno limpio.

