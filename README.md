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
| `CATEDRA_REDIS_HOST` | Host del Redis de la cátedra, donde publica las versiones y los cambios del catálogo. |
| `CATEDRA_REDIS_USERNAME` | Usuario de Redis de la integración (solo lectura en `catedra:sync:*`). |
| `CATEDRA_REDIS_PASSWORD` | Clave de ese usuario. |

Sus valores se reciben por otro canal y **no se publican** en el repositorio, en los logs ni en la documentación.

```bash
export CATEDRA_BASE_URL=<url-de-la-catedra>
export CATEDRA_JWT=<jwt-tecnico>
export CATEDRA_REDIS_HOST=<host-de-redis>
export CATEDRA_REDIS_USERNAME=<usuario-de-redis>
export CATEDRA_REDIS_PASSWORD=<clave-de-redis>
./mvnw spring-boot:run
```

Los timeouts tienen valores por defecto (5 s de conexión y 30 s de lectura) y se pueden cambiar con `CATEDRA_CONNECT_TIMEOUT` y `CATEDRA_READ_TIMEOUT` (por ejemplo, `10s`). Redis usa el puerto 6379 y un timeout de 5 s, que se cambian con `CATEDRA_REDIS_PORT` y `CATEDRA_REDIS_TIMEOUT`. La conexión a Redis se abre recién en la primera lectura: si Redis no responde, el servicio arranca igual.

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

## API del catálogo

Los endpoints `/api/**` piden el JWT de usuario (ver [Seguridad](#seguridad)). La consulta de profesional que usa turnos está en [docs/contrato-turnos.md](docs/contrato-turnos.md).

### Estado de la sincronización

```
GET /api/sincronizacion/estado
Authorization: Bearer <JWT del usuario>
```

Informa en qué estado está la copia local ([CU-06](docs/casos-de-uso/CU-06-informar-estado-de-sincronizacion.md)): la app lo usa para avisar que los resultados pueden estar desactualizados.

#### 200 OK

Siempre responde 200, también sin datos. Al día:

```json
{
  "estado": "AL_DIA",
  "versionAplicada": 1,
  "aplicadaEn": "2026-10-27T21:37:37.530100Z",
  "ultimoIntento": {
    "inicio": "2026-10-27T21:37:36.651835Z",
    "fin": "2026-10-27T21:37:37.575769Z",
    "resultado": "OK",
    "intentos": 1,
    "mensaje": null
  }
}
```

Con error, conservando la copia anterior:

```json
{
  "estado": "CON_ERROR",
  "versionAplicada": 1,
  "aplicadaEn": "2026-10-27T13:34:32.228095Z",
  "ultimoIntento": {
    "inicio": "2026-10-27T21:35:47.877111Z",
    "fin": "2026-10-27T21:36:14.851805Z",
    "resultado": "ERROR",
    "intentos": 1,
    "mensaje": "Interrumpida por un reinicio del servicio"
  }
}
```

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `estado` | texto | `SIN_DATOS`, `SINCRONIZANDO`, `CON_ERROR` o `AL_DIA` (D-10, D-13). |
| `versionAplicada`, `aplicadaEn` | número, fecha | Versión de la copia local y cuándo se aplicó. `null` si nunca se aplicó una copia completa. |
| `ultimoIntento` | objeto | La última sincronización registrada; `null` si no hubo ninguna. |
| `ultimoIntento.inicio`, `ultimoIntento.fin` | fecha | `fin` es `null` mientras está en curso. |
| `ultimoIntento.resultado` | texto | `OK`, `CATEDRA_NO_DISPONIBLE`, `RECHAZO` o `ERROR`; `null` mientras está en curso. |
| `ultimoIntento.intentos` | número | Pedidos hechos a la cátedra, contando los reintentos (D-03). |
| `ultimoIntento.mensaje` | texto | Motivo funcional del error, sin datos sensibles (D-11); `null` si terminó bien. |

Las fechas van en UTC, formato ISO 8601. Sin token o con un token inválido responde **401**, igual que el resto de `/api/**`.
