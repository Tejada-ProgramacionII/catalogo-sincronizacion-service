<!-- calidad:inicio -->
![Calidad](https://img.shields.io/badge/Calidad-sin%20c%C3%B3digo-lightgrey) ![Aprobar](https://img.shields.io/badge/Aprobar-no-red) ![Destacar](https://img.shields.io/badge/Destacar-no-lightgrey)

**Calidad (7 dimensiones):** sin código en `main` (no evaluable).
<!-- calidad:fin -->

# catalogo-sincronizacion-service
  Servicio de catálogo y sincronización — Spring Boot. Réplica local del catálogo de la cátedra con sincronización completa e incremental (REST + Redis + Kafka).

## Base de datos y migraciones

- Base local: PostgreSQL con Docker Compose (`docker compose up -d`).
- El esquema lo administra **Flyway**: al arrancar, el servicio aplica en orden las migraciones de `src/main/resources/db/migration` que todavía no corrieron y las registra en la tabla `flyway_schema_history`.
- Las migraciones se nombran `V<número>__<descripción>.sql`. Una migración ya aplicada no se modifica: cada cambio va en un archivo nuevo.
- Hibernate no crea tablas (`ddl-auto=validate`): solo verifica que las entidades coincidan con el esquema.
- Los tests usan H2 en memoria con las mismas migraciones.

## Integración con la cátedra

El servicio necesita estas variables de entorno para arrancar; si falta alguna, no levanta:

| Variable | Qué es |
| --- | --- |
| `CATEDRA_BASE_URL` | URL base de la API REST de la cátedra. |
| `CATEDRA_JWT` | JWT técnico de la cuenta del proyecto (dura un año). |

Sus valores se reciben por otro canal y **no se publican** en el repositorio, en los logs ni en la documentación.

```bash
export CATEDRA_BASE_URL=<url-de-la-catedra>
export CATEDRA_JWT=<jwt-tecnico>
./mvnw spring-boot:run
```

Los timeouts tienen valores por defecto (5 s de conexión y 30 s de lectura) y se pueden cambiar con `CATEDRA_CONNECT_TIMEOUT` y `CATEDRA_READ_TIMEOUT` (por ejemplo, `10s`).

## Seguridad

Todos los endpoints `/api/**` piden el JWT de usuario que emite **turnos-reservas-service** (`Authorization: Bearer <token>`). El catálogo no emite tokens: solo valida la firma HS512 y el vencimiento con el mismo secreto que turnos. Sin token, o con un token inválido o vencido, responde 401. Swagger (`/swagger-ui/**`, `/v3/api-docs/**`) queda público.

El secreto llega por la variable de entorno `JWT_SECRET` (obligatoria: sin ella el servicio no arranca). Tiene que ser **el mismo valor** que usa turnos; si difieren, todos los pedidos responden 401. Se genera una sola vez, en Base64 y con al menos 64 bytes:

```bash
openssl rand -base64 64 | tr -d '\n'
```

```bash
export JWT_SECRET=<el-mismo-secreto-que-turnos>
./mvnw spring-boot:run
```

Como el resto de los secretos, no se publica en el repositorio, en los logs ni en la documentación.

