# Documentación de Pruebas de Integración - CALENDARIO CONTABLE

## Introducción

Esta colección de Postman contiene pruebas de integración para el módulo de calendario contable de la aplicación ContApp. Las pruebas están diseñadas para validar las funcionalidades de creación, consulta y eliminación de fechas en el calendario contable, incluyendo casos de éxito y error.

La colección utiliza autenticación basada en tokens de Keycloak y está organizada en tres secciones principales:
- **1 Token**: Obtención del token de autenticación
- **2 Integration**: Pruebas de integración (casos correctos e incorrectos)
- **3 Tear Down**: Limpieza de datos creados durante las pruebas

## Variables de Colección

La colección utiliza las siguientes variables:

- `testRunId`: Identificador único de la ejecución de pruebas
- `timestamp`: Marca de tiempo de la ejecución
- `tokenKeycloak`: Token de autenticación JWT obtenido de Keycloak
- `calendarId_1`: ID del primer calendario creado durante las pruebas

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

#### Crear fecha día - 200

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "date": "2025-06-15"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('La respuesta contiene los datos del calendario', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson).to.have.property('idEnterprise');
    pm.expect(responseJson).to.have.property('date');
    pm.expect(responseJson.date).to.equal('2025-06-15');
});

const responseJson = pm.response.json();
pm.collectionVariables.set('calendarId_1', responseJson.id);
console.log('✓ Fecha individual creada con ID: ' + responseJson.id);
```

#### Abrir mes completo (Febrero 2025) - 200

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-month  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 2025,
  "month": 2
}
```

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('La respuesta contiene los días del mes', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.an('array');
    pm.expect(responseJson.length).to.equal(28);
});

pm.test('Todas las fechas son de febrero 2025', function () {
    const responseJson = pm.response.json();
    responseJson.forEach(function(item) {
        pm.expect(item.date).to.include('2025-02');
    });
});

console.log('✓ Mes de febrero 2025 abierto con ' + pm.response.json().length + ' días');
```

#### Abrir año completo (2024) - 200

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-year  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 2024
}
```

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('La respuesta contiene los días del año', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.an('array');
    pm.expect(responseJson.length).to.equal(366);
});

pm.test('Todas las fechas son de 2024', function () {
    const responseJson = pm.response.json();
    responseJson.forEach(function(item) {
        pm.expect(item.date).to.include('2024');
    });
});

console.log('✓ Año 2024 abierto con ' + pm.response.json().length + ' días (bisiesto)');
```

#### Consultar fecha por ID - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/findById/{{calendarId_1}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
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
    pm.expect(responseJson).to.have.property('idEnterprise');
    pm.expect(responseJson).to.have.property('date');
    pm.expect(responseJson.id).to.equal(parseInt(pm.collectionVariables.get('calendarId_1')));
});

console.log('✓ Fecha consultada correctamente');
```

#### Listar fechas por año (2024) - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/year/{{enterpriseId}}?year=2024  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('La respuesta es un array con fechas del año', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.an('array');
    pm.expect(responseJson.length).to.be.at.least(1);
});

pm.test('Todas las fechas son de 2024', function () {
    const responseJson = pm.response.json();
    responseJson.forEach(function(item) {
        pm.expect(item.date).to.include('2024');
    });
});

console.log('✓ Listado de fechas 2024 correcto: ' + pm.response.json().length + ' registros');
```

#### Listar años disponibles - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/years/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es JSON válido', function () {
    pm.response.to.be.json;
});

pm.test('La respuesta es un array de años', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.an('array');
    pm.expect(responseJson.length).to.be.at.least(1);
});

pm.test('Contiene los años creados (2024, 2025)', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.include(2024);
    pm.expect(responseJson).to.include(2025);
});

console.log('✓ Años disponibles: ' + pm.response.json().join(', '));
```

#### Verificar fecha existente (existe) - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/exists/{{enterpriseId}}?date=2025-06-15  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta indica que la fecha existe', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.true;
});

console.log('✓ Verificación correcta: fecha 2025-06-15 existe');
```

#### Verificar fecha no existente - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/exists/{{enterpriseId}}?date=2030-01-01  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta indica que la fecha no existe', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.false;
});

console.log('✓ Verificación correcta: fecha 2030-01-01 no existe');
```

### 2.2 Casos incorrectos

#### Crear

##### Crear fecha - Sin token - 401

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/create  
**Autenticación:** Ninguna  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "date": "2025-07-01"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 401 (Unauthorized)', function () {
    pm.response.to.have.status(401);
});

console.log('✓ Rechazado correctamente: sin token de autenticación');
```

##### Crear fecha - Sin idEnterprise - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "date": "2025-07-01"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El mensaje indica campo requerido', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.include('identerprise');
});

console.log('✓ Rechazado correctamente: idEnterprise es requerido');
```

##### Crear fecha - idEnterprise vacío - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "",
  "date": "2025-07-01"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

console.log('✓ Rechazado correctamente: idEnterprise no puede estar vacío');
```

##### Crear fecha - Sin date - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/create  
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

pm.test('El mensaje indica campo requerido', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.include('date');
});

console.log('✓ Rechazado correctamente: date es requerido');
```

##### Crear fecha - Formato fecha inválido (DD-MM-YYYY) - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "date": "15-07-2025"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El mensaje indica formato incorrecto', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.include('fecha');
});

console.log('✓ Rechazado correctamente: formato DD-MM-YYYY no es válido');
```

##### Crear fecha - Formato fecha inválido (texto) - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/create  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "date": "fecha-invalida"
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

console.log('✓ Rechazado correctamente: texto no es formato de fecha válido');
```

##### Crear fecha - Body vacío - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/create  
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

console.log('✓ Rechazado correctamente: body vacío no es válido');
```

##### Abrir mes - Sin token - 401

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-month  
**Autenticación:** Ninguna  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 2025,
  "month": 7
}
```

**Script de Test:**
```javascript
pm.test('Status code es 401 (Unauthorized)', function () {
    pm.response.to.have.status(401);
});

console.log('✓ Rechazado correctamente: sin token de autenticación');
```

##### Abrir mes - Sin idEnterprise - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-month  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "year": 2025,
  "month": 7
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El mensaje indica campo requerido', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.include('identerprise');
});

console.log('✓ Rechazado correctamente: idEnterprise es requerido');
```

##### Abrir mes - Año menor a 2000 - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-month  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 1999,
  "month": 7
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El mensaje indica año inválido', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.include('year');
});

console.log('✓ Rechazado correctamente: año menor a 2000 no es válido');
```

##### Abrir mes - Año mayor a 9999 - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-month  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 10000,
  "month": 7
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El mensaje indica año inválido', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.include('year');
});

console.log('✓ Rechazado correctamente: año mayor a 9999 no es válido');
```

##### Abrir mes - Mes menor a 1 - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-month  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 2025,
  "month": 0
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El mensaje indica mes inválido', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.include('month');
});

console.log('✓ Rechazado correctamente: mes 0 no es válido');
```

##### Abrir mes - Mes mayor a 12 - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-month  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 2025,
  "month": 13
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El mensaje indica mes inválido', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.include('month');
});

console.log('✓ Rechazado correctamente: mes 13 no es válido');
```

##### Abrir mes - Body vacío - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-month  
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

console.log('✓ Rechazado correctamente: body vacío no es válido');
```

##### Abrir año - Sin token - 401

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-year  
**Autenticación:** Ninguna  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 2026
}
```

**Script de Test:**
```javascript
pm.test('Status code es 401 (Unauthorized)', function () {
    pm.response.to.have.status(401);
});

console.log('✓ Rechazado correctamente: sin token de autenticación');
```

##### Abrir año - Sin idEnterprise - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-year  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "year": 2026
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El mensaje indica campo requerido', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.include('identerprise');
});

console.log('✓ Rechazado correctamente: idEnterprise es requerido');
```

##### Abrir año - Año menor a 2000 - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-year  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 1999
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El mensaje indica año inválido', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.include('year');
});

console.log('✓ Rechazado correctamente: año menor a 2000 no es válido');
```

##### Abrir año - Año mayor a 9999 - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-year  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 10000
}
```

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

pm.test('El mensaje indica año inválido', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.include('year');
});

console.log('✓ Rechazado correctamente: año mayor a 9999 no es válido');
```

##### Abrir año - Body vacío - 400

**Método:** POST  
**URL:** {{baseUrl}}/api/config/accounting-calendar/open-year  
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

console.log('✓ Rechazado correctamente: body vacío no es válido');
console.log('=== FIN DE CASOS INCORRECTOS DE CREAR ===');
```

#### Consultar

##### Consultar por ID - Sin token - 401

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/findById/1/{{enterpriseId}}  
**Autenticación:** Ninguna  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 401 (Unauthorized)', function () {
    pm.response.to.have.status(401);
});

console.log('✓ Rechazado correctamente: sin token de autenticación');
```

##### Consultar por ID - ID inexistente - 404

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/findById/999999/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

pm.test('El mensaje indica que no se encontró', function () {
    const responseJson = pm.response.json();
    pm.expect(JSON.stringify(responseJson).toLowerCase()).to.satisfy(function(msg) {
        return msg.includes('no') || msg.includes('not') || msg.includes('encontr');
    });
});

console.log('✓ Rechazado correctamente: ID 999999 no existe');
```

##### Consultar por ID - enterpriseId inexistente - 404

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/findById/1/empresa-inexistente-12345  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 404 (Not Found)', function () {
    pm.response.to.have.status(404);
});

console.log('✓ Rechazado correctamente: enterpriseId inexistente');
```

##### Listar por año - Sin token - 401

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/year/{{enterpriseId}}?year=2024  
**Autenticación:** Ninguna  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 401 (Unauthorized)', function () {
    pm.response.to.have.status(401);
});

console.log('✓ Rechazado correctamente: sin token de autenticación');
```

##### Listar por año - Sin parámetro year - 400

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/year/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

console.log('✓ Rechazado correctamente: parámetro year es requerido');
```

##### Listar por año - Año como texto inválido - 400

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/year/{{enterpriseId}}?year=invalido  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

console.log('✓ Rechazado correctamente: año debe ser un número válido');
```

##### Listar años disponibles - Sin token - 401

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/years/{{enterpriseId}}  
**Autenticación:** Ninguna  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 401 (Unauthorized)', function () {
    pm.response.to.have.status(401);
});

console.log('✓ Rechazado correctamente: sin token de autenticación');
```

##### Verificar existencia - Sin token - 401

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/exists/{{enterpriseId}}?date=2025-01-01  
**Autenticación:** Ninguna  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 401 (Unauthorized)', function () {
    pm.response.to.have.status(401);
});

console.log('✓ Rechazado correctamente: sin token de autenticación');
```

##### Verificar existencia - Sin parámetro date - 400

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/exists/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

console.log('✓ Rechazado correctamente: parámetro date es requerido');
```

##### Verificar existencia - Formato fecha inválido - 400

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/exists/{{enterpriseId}}?date=01-01-2025  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

console.log('✓ Rechazado correctamente: formato de fecha inválido');
```

##### Verificar existencia - Fecha como texto inválido - 400

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/exists/{{enterpriseId}}?date=fecha-invalida  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 400 (Bad Request)', function () {
    pm.response.to.have.status(400);
});

console.log('✓ Rechazado correctamente: texto no es una fecha válida');
console.log('=== FIN DE CASOS INCORRECTOS DE CONSULTAR ===');
```

## 3. Tear Down

### Eliminar fecha individual por ID - 204

**Método:** DELETE  
**URL:** {{baseUrl}}/api/config/accounting-calendar/delete/{{calendarId_1}}/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 204 (No Content)', function () {
    pm.response.to.have.status(204);
});

console.log('✓ Fecha individual eliminada correctamente');
```

### Verificar eliminación de fecha - 200 false

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/exists/{{enterpriseId}}?date=2025-06-15  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La fecha ya no existe', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.false;
});

console.log('✓ Verificación correcta: fecha eliminada ya no existe');
```

### Eliminar mes completo (Febrero 2025) - 204

**Método:** DELETE  
**URL:** {{baseUrl}}/api/config/accounting-calendar/delete-month  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 2025,
  "month": 2
}
```

**Script de Test:**
```javascript
pm.test('Status code es 204 (No Content)', function () {
    pm.response.to.have.status(204);
});

console.log('✓ Mes de febrero 2025 eliminado correctamente');
```

### Eliminar año completo (2024) - 204

**Método:** DELETE  
**URL:** {{baseUrl}}/api/config/accounting-calendar/delete-year  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body (raw JSON):**
```json
{
  "idEnterprise": "{{enterpriseId}}",
  "year": 2024
}
```

**Script de Test:**
```javascript
pm.test('Status code es 204 (No Content)', function () {
    pm.response.to.have.status(204);
});

console.log('✓ Año 2024 eliminado correctamente');
```

### Verificar listado vacío tras eliminaciones - 200

**Método:** GET  
**URL:** {{baseUrl}}/api/config/accounting-calendar/years/{{enterpriseId}}  
**Autenticación:** Bearer Token ({{tokenKeycloak}})  
**Headers:** Ninguno  
**Body:** Ninguno  

**Script de Test:**
```javascript
pm.test('Status code es 200 (OK)', function () {
    pm.response.to.have.status(200);
});

pm.test('La respuesta es un array vacío o sin 2024', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.an('array');
    pm.expect(responseJson).to.not.include(2024);
});

console.log('✓ Verificación correcta: año 2024 ya no aparece en la lista');
console.log('=== FIN DE CASOS CORRECTOS DE CALENDARIO ===');
```

## Notas de Ejecución

1. **Orden de Ejecución**: Los tests deben ejecutarse en el orden definido en la colección para asegurar que las dependencias (como el token y IDs creados) estén disponibles.

2. **Dependencias**: 
   - El token de Keycloak debe obtenerse primero
   - Los IDs de calendarios se almacenan en variables de colección durante la ejecución
   - Las pruebas de eliminación dependen de que los datos existan previamente

3. **Entorno**: Asegurarse de que los servicios de Keycloak, la API de configuración y la base de datos estén ejecutándose antes de ejecutar las pruebas.

4. **Limpieza**: La sección "Tear Down" elimina todos los datos creados durante las pruebas para mantener el entorno limpio.

