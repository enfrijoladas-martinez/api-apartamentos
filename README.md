# API Apartamentos

API REST para alquilar pisos y habitaciones. Gestiona **clientes** (propietarios y huéspedes), **propiedades** con sus imágenes, **unidades rentables** (habitaciones o el piso completo), **disponibilidad**, **reservas**, **pagos**, **reseñas** y **notificaciones**.

Además de la API, incluye una **interfaz gráfica web** (Thymeleaf) para administrar clientes, propiedades, unidades, reservas y pagos desde el navegador.

## Tecnologías

- Java 21 (compila con JDK 21 o superior)
- Spring Boot 4.1.1: Spring Web MVC, Spring Data JPA, Validation, Thymeleaf y Actuator
- Spring Security Crypto (las contraseñas se guardan cifradas con BCrypt)
- MySQL 8 o superior (driver `mysql-connector-j`)
- Maven (incluido con `mvnw`)
- JUnit 5 y Mockito para las pruebas unitarias

## Estructura

```
src/main/java/com/api/apartamentos/
├── models/         Entidades JPA (9)
├── repositories/   Interfaces que extienden JpaRepository
├── services/       Lógica de negocio y validaciones (BaseCrudService + uno por entidad)
└── controllers/    Controladores REST (/api/...), WebController (interfaz) y ApiExceptionHandler
src/main/resources/
├── templates/      Páginas de la interfaz gráfica
└── static/css/     Estilos
src/test/java/      Pruebas unitarias de pagos y unidades
postman/Apartamentos.postman_collection.json   Colección de Postman
```

## Instalación y ejecución

1. Clona el repositorio:
   ```bash
   git clone https://github.com/enfrijoladas-martinez/api-apartamentos.git
   cd api-apartamentos
   ```
2. Configura las variables de entorno: copia `.env.example` como `.env` y pon tu contraseña de MySQL.
   ```bash
   cp .env.example .env
   ```
3. Ejecuta la aplicación (la base `apartamentos` y sus tablas se crean solas):
   ```bash
   ./mvnw spring-boot:run
   ```
   En Windows: `mvnw.cmd spring-boot:run`. También puedes abrir la carpeta en VS Code y ejecutar `ApartamentosApplication.java`.
4. Abre:
   - Interfaz gráfica: `http://localhost:8000`
   - API: `http://localhost:8000/api/...` (por ejemplo `GET http://localhost:8000/api/propiedades`)
5. Pruebas unitarias:
   ```bash
   ./mvnw test
   ```

## Variables de entorno

Se leen del sistema o del archivo `.env` en la raíz del proyecto. El archivo `.env` no se sube al repositorio.

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_HOST` | Servidor de MySQL | `localhost` |
| `DB_PORT` | Puerto de MySQL | `3306` |
| `DB_NAME` | Nombre de la base de datos | `apartamentos` |
| `DB_USERNAME` | Usuario de MySQL | `root` |
| `DB_PASSWORD` | Contraseña de MySQL | *(obligatoria)* |
| `SERVER_PORT` | Puerto de la aplicación | `8000` |

## Interfaz gráfica

| Ruta | Pantalla |
|---|---|
| `/` | Panel con totales de clientes, propiedades, reservas y pagos |
| `/clientes` | Lista, alta, edición y baja de clientes |
| `/propiedades` | Lista, alta, edición y baja de propiedades |
| `/unidades` | Unidades rentables de cada propiedad |
| `/reservas` | Reservas, con opción de cancelar |
| `/pagos` | Pagos, con opciones de confirmar y reembolsar |

## Endpoints de la API

Todos los recursos tienen `GET` (lista), `GET /{id}`, `POST`, `PUT /{id}` y `DELETE /{id}`.

| Recurso | Ruta base | Extras |
|---|---|---|
| Clientes | `/api/clientes` | La contraseña se recibe pero nunca se devuelve |
| Propiedades | `/api/propiedades` | `GET /api/propiedades/tipo/{tipo}`: propiedades por tipo |
| Imágenes de propiedad | `/api/propiedad-imagenes` | |
| Unidades rentables | `/api/unidades` | |
| Disponibilidad | `/api/disponibilidades` | |
| Reservas | `/api/reservas` | `importeTotal` se calcula con el precio de la unidad × noches |
| Pagos | `/api/pagos` | `POST /api/pagos/{id}/confirmar`, `POST /api/pagos/{id}/reembolsar` |
| Reseñas | `/api/resenas` | |
| Notificaciones | `/api/notificaciones` | |

### Campos que asigna el servidor

- Los ids de todas las entidades.
- Las fechas de registro, creación, envío y reseña.
- `importeTotal` de la reserva, cuando no se envía: precio por noche × noches (por fecha de calendario).
- `referenciaPago` y el cliente del pago (se toma de la reserva). `fechaPago` se asigna al confirmar.

## Reglas y validaciones

- Email válido y único; nombre, apellidos y contraseña obligatorios.
- Una unidad necesita una propiedad existente que no esté en mantenimiento, capacidad ≥ 1 y precio > 0.
- Una reserva necesita unidad y cliente existentes, salida posterior a la entrada y que la unidad no esté reservada en esas fechas.
- La suma de los pagos no puede superar el importe de la reserva, y no se paga una reserva cancelada.
- Un pago solo se confirma si está `PENDIENTE` o `EN_PROCESO`, y solo se reembolsa si está `PAGADO`. Los pagos confirmados no se eliminan.

| Código | Cuándo |
|---|---|
| 200 OK | Consulta, actualización o acción correcta |
| 201 Created | Recurso creado |
| 204 No Content | Recurso eliminado |
| 400 Bad Request | Dato inválido o regla de negocio incumplida |
| 404 Not Found | El recurso no existe |
| 409 Conflict | Email o INE repetido, o registro en uso por otro |

## Ejemplos

### Crear un cliente

`POST /api/clientes`

```json
{
  "nombres": "Jorge",
  "apellidos": "Ruiz Peña",
  "email": "jorge@correo.com",
  "password": "demo1234",
  "telefono": "5533334444"
}
```

Respuesta `201 Created` (sin la contraseña):

```json
{
  "id": 2,
  "tipo": "CLIENTE",
  "nombres": "Jorge",
  "apellidos": "Ruiz Peña",
  "email": "jorge@correo.com",
  "telefono": "5533334444",
  "estatus": "ACTIVO",
  "fechaRegistro": "2026-10-13T11:48:22.1548848"
}
```

### Crear una unidad rentable

`POST /api/unidades`

```json
{
  "propiedad": { "idPropiedad": 1 },
  "nombre": "Habitación principal",
  "tipo": "Habitación",
  "capacidad": 2,
  "precioNoche": 650
}
```

Respuesta `201 Created`: la unidad con `"idUnidad": 1`, `"estado": "DISPONIBLE"` y los datos de su propiedad.

### Crear una reserva

`POST /api/reservas`

```json
{
  "unidad":  { "idUnidad": 1 },
  "cliente": { "id": 2 },
  "fechaEntrada": "2026-11-01T15:00:00",
  "fechaSalida":  "2026-11-04T11:00:00",
  "comentario": "Llegamos por la tarde"
}
```

Respuesta `201 Created` (resumida): 3 noches × 650 = 1950.

```json
{
  "idReservacion": 1,
  "estado": "PENDIENTE",
  "fechaEntrada": "2026-11-01T15:00:00",
  "fechaSalida": "2026-11-04T11:00:00",
  "importeTotal": 1950,
  "unidad":  { "idUnidad": 1, "nombre": "Habitación principal", "precioNoche": 650 },
  "cliente": { "id": 2, "nombres": "Jorge", "apellidos": "Ruiz Peña" }
}
```

### Registrar y confirmar un pago

`POST /api/pagos`

```json
{
  "reserva": { "idReservacion": 1 },
  "importe": 1950,
  "fechaVencimiento": "2026-10-30",
  "metodo": "TARJETA_CREDITO"
}
```

`POST /api/pagos/1/confirmar` → `200 OK` (resumida):

```json
{
  "idPago": 1,
  "estado": "PAGADO",
  "fechaPago": "2026-10-13",
  "importe": 1950,
  "metodo": "TARJETA_CREDITO",
  "referenciaPago": "PAG-72C69B55AF284869"
}
```

### Errores

Reserva en fechas ya ocupadas → `400 Bad Request`:

```json
{ "status": 400, "error": "La unidad ya está reservada en ese rango", "timestamp": "2026-10-13T11:48:22.8380362" }
```

Pago que no existe (`POST /api/pagos/999/confirmar`) → `404 Not Found`:

```json
{ "status": 404, "error": "El pago no existe", "timestamp": "2026-10-13T11:48:22.9605113" }
```

## Pruebas con Postman

Importa `postman/Apartamentos.postman_collection.json` y ejecuta las carpetas en orden sobre una base vacía. Las peticiones de creación guardan los ids en variables de la colección, y los casos de error están marcados con su código esperado.
