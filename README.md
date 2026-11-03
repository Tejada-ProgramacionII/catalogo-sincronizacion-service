# catalogo-sincronizacion-service
  Servicio de catálogo y sincronización — Spring Boot. Réplica local del catálogo de la cátedra con sincronización completa e incremental (REST + Redis + Kafka).

## Levantar con Docker

Requisitos: **Docker** (Docker Desktop en macOS/Windows) y **ZeroTier** conectado a la red de la cátedra. Sin ZeroTier el servicio arranca igual, pero la sincronización queda `CON_ERROR` porque no llega a la cátedra.

1. Copiar el archivo de variables y completarlo con los valores reales de la cuenta técnica (ver [Integración con la cátedra](#integración-con-la-cátedra) y [Seguridad](#seguridad)). El `.env` no se commitea.

   ```bash
   cp .env.example .env
   ```

2. Construir la imagen y levantar la base y el servicio:

   ```bash
   docker compose up -d --build
   ```

   El servicio espera a que la base esté sana, aplica las migraciones y sincroniza con la cátedra. Queda en `http://localhost:8080` (Swagger en `/swagger-ui/index.html`).

3. Ver los logs y el estado:

   ```bash
   docker compose logs -f catalogo
   docker compose ps
   ```

4. Bajarlo (los datos quedan en el volumen `catalogo-db-data`; `docker compose down -v` también lo borra):

   ```bash
   docker compose down
   ```

La imagen se compila en dos etapas (JDK para compilar, solo el JRE para correr) y corre con un usuario sin privilegios. Los tests no corren en el build de la imagen: los corre el CI. Los contenedores usan su propia red, `10.20.0.0/24`, para no chocar con la `172.17.x` de la cátedra.

### Relay en macOS

En macOS con Docker Desktop, los contenedores no enrutan por la interfaz de ZeroTier (macOS devuelve no route to host); hace falta un relay local en el host hacia 172.17.201.1 (puertos 8090, 6379, 9094). En Linux no es necesario.

El relay no forma parte del repositorio y se corre en el Mac **antes de `docker compose up`**. Hace dos cosas:

1. Levanta `socat` en `127.0.0.1`, en los puertos 8090, 6379 y 9094, hacia los mismos puertos de `172.17.201.1`.
2. Agrega en la VM de Docker una regla DNAT que manda el tráfico de `172.17.201.1` a `host.docker.internal`, es decir, a esos `socat`.

Hay que repetirlo después de reiniciar Docker Desktop. Con el relay, el servicio en Docker conecta con REST, Redis y Kafka, y `GET /api/sincronizacion/estado` responde `AL_DIA`.

## Base de datos y migraciones

- Base local: PostgreSQL con Docker Compose (`docker compose up -d catalogo-db` para levantar solo la base y correr el servicio con `./mvnw spring-boot:run`).
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
| `CATEDRA_KAFKA_BOOTSTRAP_SERVERS` | Bootstrap servers del Kafka de la cátedra. |
| `CATEDRA_KAFKA_CATALOG_TOPIC` | Topic de los avisos `CatalogUpdated` de la integración. |
| `CATEDRA_KAFKA_CONSUMER_GROUP` | Consumer group de la integración. |

Sus valores se reciben por otro canal y **no se publican** en el repositorio, en los logs ni en la documentación.

```bash
export CATEDRA_BASE_URL=<url-de-la-catedra>
export CATEDRA_JWT=<jwt-tecnico>
export CATEDRA_REDIS_HOST=<host-de-redis>
export CATEDRA_REDIS_USERNAME=<usuario-de-redis>
export CATEDRA_REDIS_PASSWORD=<clave-de-redis>
export CATEDRA_KAFKA_BOOTSTRAP_SERVERS=<bootstrap-servers>
export CATEDRA_KAFKA_CATALOG_TOPIC=<topic-del-catalogo>
export CATEDRA_KAFKA_CONSUMER_GROUP=<consumer-group>
./mvnw spring-boot:run
```

Los timeouts tienen valores por defecto (5 s de conexión y 30 s de lectura) y se pueden cambiar con `CATEDRA_CONNECT_TIMEOUT` y `CATEDRA_READ_TIMEOUT` (por ejemplo, `10s`). Redis usa el puerto 6379 y un timeout de 5 s, que se cambian con `CATEDRA_REDIS_PORT` y `CATEDRA_REDIS_TIMEOUT`. La conexión a Redis se abre recién en la primera lectura: si Redis no responde, el servicio arranca igual.

### Sincronización

- **Al arrancar**: sin copia local hace la sincronización completa (CU-01); con copia, verifica si hay versiones nuevas.
- **Aviso `CatalogUpdated` por Kafka**: dispara la incremental (CU-02). Un aviso repetido (mismo `eventId`) o de una versión ya incorporada no tiene efectos; el offset se confirma después de procesarlo.
- **Verificación periódica** cada 5 minutos (`CATEDRA_SINCRONIZACION_INTERVALO_VERIFICACION`, por ejemplo `1m`): compara la versión local con la publicada en Redis y recupera los avisos perdidos (D-07).

Kafka solo avisa: los datos salen siempre de Redis, o de un snapshot si no se puede continuar desde la versión local (CU-03). Las sincronizaciones no se pisan: un aviso espera a la que está corriendo y una verificación periódica que encuentra otra en curso no hace nada.

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

Siempre responde 200, también sin datos. Al día (la verificación periódica no encontró versiones pendientes):

```json
{
  "estado": "AL_DIA",
  "versionAplicada": 1,
  "aplicadaEn": "2026-10-27T21:37:37.530100Z",
  "ultimoIntento": {
    "proceso": "COMPLETA",
    "inicio": "2026-10-27T21:37:36.651835Z",
    "fin": "2026-10-27T21:37:37.575769Z",
    "resultado": "OK",
    "intentos": 1,
    "mensaje": null
  },
  "ultimaVerificacion": "2026-10-28T14:50:33.863013Z"
}
```

Con error, conservando la copia anterior:

```json
{
  "estado": "CON_ERROR",
  "versionAplicada": 1,
  "aplicadaEn": "2026-10-27T21:37:37.530100Z",
  "ultimoIntento": {
    "proceso": "INCREMENTAL",
    "inicio": "2026-10-28T14:51:38.497339Z",
    "fin": "2026-10-28T14:51:38.939439Z",
    "resultado": "RECHAZO",
    "intentos": 1,
    "mensaje": "La cátedra rechazó el pedido de la integración"
  },
  "ultimaVerificacion": "2026-10-28T14:51:13.068915Z"
}
```

Si después una verificación no encuentra versiones pendientes, vuelve a `AL_DIA` aunque el último intento siga siendo ese error (`"ultimaVerificacion": "2026-10-28T14:52:01.204379Z"`, posterior a su `fin`).

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `estado` | texto | `SIN_DATOS`, `SINCRONIZANDO`, `CON_ERROR` o `AL_DIA` (D-10, D-13). |
| `versionAplicada`, `aplicadaEn` | número, fecha | Versión de la copia local y cuándo se aplicó. `null` si nunca se aplicó una copia completa. |
| `ultimoIntento` | objeto | La última sincronización registrada; `null` si no hubo ninguna. |
| `ultimoIntento.proceso` | texto | `COMPLETA` (CU-01), `INCREMENTAL` (CU-02) o `RECUPERACION` (CU-03): una incremental que encontró una discontinuidad y se recuperó con un snapshot. |
| `ultimoIntento.inicio`, `ultimoIntento.fin` | fecha | `fin` es `null` mientras está en curso. |
| `ultimoIntento.resultado` | texto | `OK`, `CATEDRA_NO_DISPONIBLE`, `RECHAZO`, `DISCONTINUIDAD` (la recuperación tampoco pudo continuar, D-08) o `ERROR`; `null` mientras está en curso. |
| `ultimoIntento.intentos` | número | Pedidos hechos a la cátedra (REST o Redis), contando los reintentos (D-03) y, en una recuperación, los de cada paso. |
| `ultimoIntento.mensaje` | texto | Motivo funcional, sin datos sensibles (D-11): el del error, o el de la discontinuidad si se recuperó bien; `null` si terminó bien sin recuperar. |
| `ultimaVerificacion` | fecha | Última vez que una verificación (al arrancar o periódica) comprobó contra Redis que no había versiones pendientes; `null` si todavía no hubo ninguna. Esas verificaciones no registran un intento. |

Las fechas van en UTC, formato ISO 8601. Sin token o con un token inválido responde **401**, igual que el resto de `/api/**`.
