# Colecciones de Postman - Microservicios Productos e Inventario

Este directorio contiene las colecciones de Postman para probar los microservicios de Productos e Inventario.

## 📋 Tabla de Contenidos

1. [Requisitos previos](#requisitos-previos)
2. [Importar colecciones](#importar-colecciones)
3. [Configurar environments](#configurar-environments)
4. [Estructura de colecciones](#estructura-de-colecciones)
5. [Endpoints disponibles](#endpoints-disponibles)

## 🔧 Requisitos previos

- **Postman** instalado (versión 10.0 o superior)
- Los microservicios deben estar ejecutándose
- Docker ( si usas docker-compose)

## 📥 Importar colecciones

1. Abre Postman
2. Haz clic en el botón **Import** en la esquina superior izquierda
3. Selecciona los archivos de la carpeta `collections/`:
   - `inventario-service.postman_collection.json`
   - `productos-service.postman_collection.json`
4. Las colecciones aparecerán en tu panel de Collections

## 🌍 Configurar environments

1. Haz clic en el ícono de engranaje (⚙️) en la esquina superior derecha
2. Selecciona **Import**
3. Importa los archivos de la carpeta `environments/`

## 📂 Estructura de colecciones

postman/
├── collections/
│ ├── inventario-service.postman_collection.json
│ └── productos-service.postman_collection.json
└── README.md

## 🚀 Endpoints disponibles

### Microservicio de Inventario

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/inventario/{id}` | Obtener inventario por ID |
| GET | `/api/inventario/{id}/stock` | Consultar stock disponible |
| PUT | `/api/inventario/{id}` | Actualizar inventario completo |
| POST | `/api/inventario` | Crear nuevo registro de inventario |
| PATCH | `/api/inventario/{id}/incrementar` | Incrementar stock |
| PATCH | `/api/inventario/{id}/decrementar` | Decrementar stock |

### Microservicio de Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/productos/{id}` | Obtener producto por ID |
| GET | `/api/productos` | Listar todos los productos |
| POST | `/api/productos` | Crear nuevo producto |
| PUT | `/api/productos/{id}` | Actualizar producto existente |
| DELETE | `/api/productos/{id}` | Eliminar producto por ID |

| `productos_base_url` | `https://api.tudominio.com/productos` | URL base del servicio de productos (prod) |

## 🏃 Ejecutar los microservicios localmente

### Con Docker Compose

docker-compose up -d

## 📊 Flujo de prueba recomendado

1. **Crear productos** (POST en productos-service)
2. **Listar todos los productos** (GET en productos-service)
3. **Crear registros de inventario** (POST en inventario-service)
4. **Consultar stock** (GET en inventario-service)
5. **Incrementar/Decrementar stock** (PATCH en inventario-service)
6. **Actualizar productos** (PUT en productos-service)
7. **Eliminar productos** (DELETE en productos-service)

## 🐛 Solución de problemas

### Error de conexión

Si obtienes errores de conexión, verifica:
- Los microservicios están ejecutándose
- Los puertos 8080 y 8081 están disponibles
- El environment seleccionado es el correcto

### Error 404

- Verifica que la URL base en las variables de entorno sea correcta
- Asegúrate de que el ID del recurso existe

### Error 500

- Revisa los logs de los microservicios
- Verifica que la base de datos esté configurada correctamente

**Última actualización:** Octubre 2025
**Versión de Postman recomendada:** 10.0+
