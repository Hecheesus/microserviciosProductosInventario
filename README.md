# Microservicios: Productos e Inventario

Sistema de microservicios basado en Spring Boot que implementa el estándar JSON API para gestionar productos e inventarios.

## 📋 Descripción de la Arquitectura

El sistema está compuesto por dos microservicios independientes:

### Microservicio 1: Productos
- **Responsabilidad**: Gestión completa de productos (CRUD)
- **Base de datos**: MySQL (productos_db)
- **Puerto**: 8081
- **Endpoints principales**:
  - `POST /api/productos` - Crear producto
  - `GET /api/productos/{id}` - Obtener producto por ID
  - `PUT /api/productos/{id}` - Actualizar producto
  - `DELETE /api/productos/{id}` - Eliminar producto
  - `GET /api/productos` - Listar productos con paginación

### Microservicio 2: Inventario
- **Responsabilidad**: Gestión de inventarios y comunicación con servicio de productos
- **Base de datos**: MySQL (inventario_db)
- **Puerto**: 8082
- **Endpoints principales**:
  - `GET /api/inventarios/{productoId}` - Consultar inventario de un producto
  - `PUT /api/inventarios/{productoId}` - Actualizar cantidad tras compra
  - Emite eventos cuando el inventario cambia

### Comunicación entre Microservicios
- **Protocolo**: HTTP/REST
- **Formato**: JSON API (https://jsonapi.org/)
- **Autenticación**: API Key en header `X-API-Key`
- **Manejo de errores**: Timeout y reintentos configurables

## 🎯 Decisiones Técnicas

### ¿Por qué MySQL?

Se eligió MySQL como base de datos por las siguientes razones:

1. **Madurez y estabilidad**: MySQL es una base de datos probada con más de 25 años de desarrollo
2. **Simplicidad**: Para este caso de uso (gestión de productos e inventarios), MySQL ofrece toda la funcionalidad necesaria sin complejidad adicional
3. **Rendimiento para lecturas**: MySQL tiene excelente rendimiento en operaciones de lectura frecuentes, típicas en sistemas de productos
4. **Transacciones ACID**: Con InnoDB, MySQL garantiza consistencia de datos (crítico para inventarios)
5. **Integración con Spring Boot**: Spring Data JPA funciona perfectamente con MySQL
6. **Recursos moderados**: MySQL consume menos recursos que PostgreSQL para casos de uso simples
7. **Familiaridad**: Ampliamente conocido por equipos de desarrollo

### Comparación MySQL vs PostgreSQL

| Aspecto | MySQL | PostgreSQL |
|---------|-------|------------|
| Simplicidad | ✅ Más simple de configurar | ⚠️ Más complejo |
| Rendimiento lectura | ✅ Excelente | ✅ Muy bueno |
| Rendimiento escritura | ✅ Muy bueno | ✅ Excelente |
| Características avanzadas | ⚠️ Básicas | ✅ Avanzadas (JSONB, full-text) |
| Cumplimiento SQL estándar | ⚠️ Parcial | ✅ Completo |
| Uso de recursos | ✅ Moderado | ⚠️ Mayor |
| Casos de uso | Aplicaciones web, e-commerce | Sistemas complejos, analítica |

**Conclusión**: Para este proyecto de microservicios con operaciones CRUD estándar, MySQL es la elección más pragmática.

## 🛠️ Requisitos

- Java 21
- Gradle 8.5+
- Docker y Docker Compose
- MySQL 8.0+ (incluido en Docker Compose)

## 📁 Estructura del Proyecto

```
microservices-productos-inventario/
├── README.md
├── docker-compose.yml
├── .gitignore
├── postman/
│   ├── collections/
│   │   ├── inventario-service.postman_collection.json
│   │   └── productos-service.postman_collection.json
│   └── README.md
├── productos-service/
│   ├── build.gradle
│   ├── settings.gradle
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/microservices/productos/
│       │   │   ├── ProductosApplication.java
│       │   │   ├── config/
│       │   │   ├── controller/
│       │   │   ├── model/
│       │   │   ├── repository/
│       │   │   ├── service/
│       │   │   └── exception/
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│            └── java/
│               └── com/microservices/productos/
│                    ├── controller/
│                    └── service/
│
└── inventario-service/
    ├── build.gradle
    ├── settings.gradle
    ├── Dockerfile
    └── src/
        ├── main/
        │   ├── java/com/microservices/inventario/       
        │   │   ├── config/
        │   │   ├── controller/
        │   │   ├── exception/
        │   │   ├── model/
        │   │   ├── repository/
        │   │   ├── service/
        │   │   └── InventarioApplication.java
        │   └── resources/
        │       └── application.properties
        └── test/
            └── java/
                └── com/microservices/inventario/
                    ├── controller/
                    └── service/

```

## 🚀 Instalación y Ejecución

### Opción 1: Usando Docker Compose (Recomendado)

1. **Clonar el repositorio**:
```bash
cd microservices-productos-inventario
```

2. **Construir y ejecutar los servicios**:
```bash
docker-compose up --build
```

Esto iniciará:
- MySQL Productos (puerto 3307)
- MySQL Inventario (puerto 3308)
- Servicio Productos (puerto 8081)
- Servicio Inventario (puerto 8082)

3. **Verificar que los servicios están funcionando**:
```bash
# Productos
curl http://localhost:8081/actuator/health

# Inventario
curl http://localhost:8082/actuator/health
```

4. **Detener los servicios**:
```bash
docker-compose down
```

5. **Detener y eliminar volúmenes** (elimina datos):
```bash
docker-compose down -v
```

### Opción 2: Ejecución Local (Sin Docker)

#### Prerequisitos:
- MySQL instalado y ejecutándose
- Java 21 instalado
- Gradle instalado

#### Pasos:

1. **Configurar bases de datos MySQL**:
```sql
CREATE DATABASE productos_db;
CREATE DATABASE inventario_db;
```

2. **Construir el servicio de productos**:
```bash
cd productos-service
./gradlew clean build
```

3. **Ejecutar el servicio de productos**:
```bash
./gradlew bootRun
```

4. **En otra terminal, construir el servicio de inventario**:
```bash
cd inventario-service
./gradlew clean build
```

5. **Ejecutar el servicio de inventario**:
```bash
./gradlew bootRun
```

## 📚 Documentación de API

Una vez que los servicios estén ejecutándose, la documentación Swagger estará disponible en:

- **Productos**: http://localhost:8081/swagger-ui.html
- **Inventario**: http://localhost:8082/swagger-ui.html

Documentación OpenAPI (JSON):
- **Productos**: http://localhost:8081/v3/api-docs
- **Inventario**: http://localhost:8082/v3/api-docs

## 📝 Ejemplos de Uso

### Autenticación

Todos los endpoints requieren el header `X-API-Key`:

```bash
-H "X-API-Key: productos-api-key-2024"
```

Para comunicación entre servicios (Inventario → Productos):
```bash
-H "X-API-Key: inventario-api-key-2024"
```

### Crear un Producto

```bash
curl -X POST http://localhost:8081/api/productos \
  -H "Content-Type: application/vnd.api+json" \
  -H "X-API-Key: productos-api-key-2024" \
  -d '{
    "data": {
      "type": "productos",
      "attributes": {
        "nombre": "Laptop Dell XPS 15",
        "precio": 1299.99
      }
    }
  }'
```

### Obtener un Producto

```bash
curl -X GET http://localhost:8081/api/productos/1 \
  -H "Accept: application/vnd.api+json" \
  -H "X-API-Key: productos-api-key-2024"
```

### Listar Productos con Paginación

```bash
curl -X GET "http://localhost:8081/api/productos?page=0&size=10" \
  -H "Accept: application/vnd.api+json" \
  -H "X-API-Key: productos-api-key-2024"
```

### Consultar Inventario de un Producto

```bash
curl -X GET http://localhost:8082/api/inventarios/1 \
  -H "Accept: application/vnd.api+json" \
  -H "X-API-Key: inventario-api-key-2024"
```

### Actualizar Cantidad de Inventario

```bash
curl -X PUT http://localhost:8082/api/inventarios/1 \
  -H "Content-Type: application/vnd.api+json" \
  -H "X-API-Key: inventario-api-key-2024" \
  -d '{
    "data": {
      "type": "inventarios",
      "id": "1",
      "attributes": {
        "cantidad": 45
      }
    }
  }'
```

## 🧪 Testing

### Ejecutar Tests Unitarios

```bash
# Productos
cd productos-service
./gradlew test

# Inventario
cd inventario-service
./gradlew test
```

### Generar Reporte de Cobertura (JaCoCo)

```bash
./gradlew test jacocoTestReport
```

Ver reporte en: `build/reports/jacoco/test/html/index.html`

### Verificar Cobertura Mínima (60%)

```bash
./gradlew test jacocoTestCoverageVerification
```

## 📊 Características Implementadas

✅ **Cumplimiento JSON API**
- Formato de respuesta estándar
- Links HATEOAS
- Metadata en respuestas
- Manejo de errores según spec

✅ **Seguridad**
- Autenticación por API Key
- Validación de headers
- Manejo seguro de credenciales

✅ **Comunicación entre Microservicios**
- RestTemplate con retry
- Timeout configurado (5 segundos)
- Hasta 3 reintentos con backoff

✅ **Documentación**
- Swagger UI integrado
- OpenAPI 3.0
- Endpoints documentados

✅ **Testing**
- Tests unitarios
- Tests de integración
- Cobertura mínima 60%
- JaCoCo reports

✅ **Containerización**
- Dockerfile por servicio
- Docker Compose orchestration
- Health checks
- Variables de entorno

✅ **Manejo de Errores**
- Exception handlers globales
- Respuestas JSON API compliant
- Mensajes descriptivos

