# Pruebas automatizadas

## Cómo correrlas

Requisito: Java 25. Los tests no necesitan Docker, PostgreSQL ni la cátedra.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 25)   # macOS; en otro sistema, apuntar a un JDK 25
./mvnw -B clean verify
```

`verify` compila, corre los tests (JUnit 5) y genera el reporte de cobertura de JaCoCo en `target/site/jacoco/index.html`. Para correr una clase o un método:

```bash
./mvnw -B test -Dtest=SincronizacionServiceTest
./mvnw -B test -Dtest='SincronizacionServiceTest#unAvisoRepetidoNoTieneEfectos'
```

En cada PR y cada push a `develop`, `testing` y `main`, el workflow `.github/workflows/sonar.yml` corre `./mvnw -B -ntp clean verify` con Java 25 y publica el análisis y la cobertura en SonarCloud (Quality Gate).

## Entorno de los tests

`src/test/resources/application.properties` reemplaza la configuración real:

- **Base:** H2 en memoria con **las mismas migraciones de Flyway** que PostgreSQL y `ddl-auto=validate`, así los tests también verifican el esquema.
- **Cátedra:** valores ficticios (`http://catedra.test`, `redis.test`, `kafka.test:9092`). Ningún test llama a la cátedra.
- **Kafka:** `spring.kafka.listener.auto-startup=false`. El consumidor no se conecta.
- **Sincronización:** apagada al arrancar y en la verificación periódica, y sin espera entre reintentos.
- **JWT:** un secreto de prueba en Base64, que no es una clave real.

## Qué cubre cada tipo

Son 202 tests en 46 clases, con los paquetes espejados a los de `main`.

### Unitarios (JUnit 5 + Mockito, sin Spring)

Cada pieza con sus dependencias mockeadas.

| Qué | Clases | Ejemplos de lo que verifican |
| --- | --- | --- |
| Orquestación de la sincronización | `SincronizacionServiceTest` | Reintentos limitados y registro de cada paso; un rechazo no reintenta; la verificación sin pendientes solo guarda la fecha; recuperación con snapshot ante una discontinuidad y una sola vez (`siVuelveAHaberDiscontinuidadDespuesDelSnapshotNoReconstruyeOtraVez`); avisos repetidos o de versiones ya incorporadas sin efectos; un aviso espera a la sincronización en curso. |
| Casos de uso | `SincronizarCatalogoCompletoUseCaseImplTest`, `SincronizarCatalogoIncrementalUseCaseImplTest`, `VerificarVersionesPendientesUseCaseImplTest`, `ConsultarEstadoSincronizacionUseCaseImplTest`, `BuscarProfesionalesUseCaseImplTest`, `ConsultarProfesionalUseCaseImplTest` | Versiones aplicadas en orden; discontinuidad por ventana o por cambios faltantes; una entidad no publicada no toca la base; cálculo del estado (D-13); 503 sin copia (D-04); categoría inexistente. |
| Fachada | `ProfesionalServiceTest` | Un `Optional` vacío se convierte en `ProfesionalNoEncontradoException`. |
| Modelo | `FiltroBusquedaProfesionalesTest` | El día de la semana que sale de la fecha (D-09). |
| Mappers | `*MapperTest` (persistencia, web, cliente, cache, mensajería) | Traducción campo por campo, incluido el caso `null`. |
| Adaptadores de entrada | `CatalogUpdatedConsumerTest`, `VerificacionPeriodicaSchedulerTest`, `SincronizacionAlArrancarListenerTest` | El offset se confirma recién después de procesar; un mensaje que no es JSON se descarta; un error no detiene el consumidor ni el scheduler; al arrancar se cierran los intentos interrumpidos. |
| Adaptador de Redis | `RedisCatalogoPublicadoClientAdapterTest` (con `StringRedisTemplate` mockeado) | Lectura de versiones, cambios y Hash; ids no publicados descartados; `WRONGPASS`/`NOPERM` como rechazo; errores sin usuario ni host. |
| Configuración | `CatedraRedisPropertiesTest`, `CatedraKafkaPropertiesTest`, `SincronizacionPropertiesTest`, `JwtConfigTest` | Sin una propiedad obligatoria el contexto no levanta; autoridades del claim `auth`. |

### Controllers (MockMvc standalone)

`ProfesionalControllerTest` y `SincronizacionControllerTest` arman el controller con su `Service` mockeado y el `DtoMapper` real:

- búsqueda con filtros y paginación;
- 400 por categoría inexistente, fecha con formato incorrecto o `size` fuera de rango, en los tres casos sin llegar a buscar;
- 404 y 503;
- formato de la respuesta del estado.

### Seguridad (`@SpringBootTest` + MockMvc)

`SecurityConfigTest` levanta la aplicación con la cadena de filtros real y firma tokens con el secreto de prueba:

- sin token → 401 en la búsqueda, la consulta de un profesional y el estado;
- token válido → 200;
- firmado con otro secreto → 401;
- vencido → 401.

### Persistencia e integración (`@SpringBootTest` sobre H2 con Flyway)

| Clase | Qué verifica |
| --- | --- |
| `JpaProfesionalRepositoryAdapterBusquedaTest` | La consulta JPQL real: cada filtro solo y combinados, nombre parcial sin mayúsculas, disponibilidad por día, orden por apellido y nombre, paginación y página vacía. |
| `JpaHorarioSemanalRepositoryAdapterHabilitadosTest` | Solo los horarios habilitados de un profesional. |
| `JpaIntentoSincronizacionRepositoryAdapterTest`, `JpaUltimaVerificacionRepositoryAdapterTest`, `JpaAvisoCatalogoRepositoryAdapterTest` | Las tablas del estado de la sincronización contra el esquema de las migraciones. |
| `SincronizarCatalogoCompletoIntegrationTest` | Un segundo snapshot reemplaza al primero aunque repita ids, y un snapshot inválido deja intacta la copia anterior con su versión (D-02). |
| `SincronizarCatalogoIncrementalIntegrationTest` | Una versión inserta y actualiza, y una versión que falla se deshace entera (D-06). |
| `CatalogoSincronizacionApplicationTests` | El contexto completo levanta. |

Los demás adaptadores JPA (`JpaCategoriaRepositoryAdapterTest`, `JpaHorarioSemanalRepositoryAdapterTest`, `JpaProfesionalRepositoryAdapterTest`, `JpaVersionAplicadaRepositoryAdapterTest`) se prueban con el repositorio de Spring Data mockeado y el mapper real.

### Cliente HTTP y Redis reales, sin la cátedra

- **`HttpCatalogoCatedraClientAdapterTest`** y **`CatedraClientConfigTest`** levantan un `HttpServer` del JDK que hace de cátedra:
  - pide el snapshot con la URL base y el JWT técnico;
  - un 4xx se clasifica como rechazo;
  - un 5xx, una respuesta sin cuerpo, nadie escuchando o un timeout se clasifican como no disponible.

  Se usa `HttpServer` y no `MockRestServiceServer` porque el request factory que configura los timeouts pisa al de este último.
- **`RedisCatalogoPublicadoClientAdapterSinRedisTest`** arma la conexión real de `CatedraRedisConfig` contra un puerto donde no escucha nadie: con Redis caído lanza `CatedraNoDisponibleException`, sin la clave en el mensaje.

### Fuera de los tests automáticos

La conexión real con la cátedra (REST, Redis y Kafka) se prueba a mano contra el servidor de la cátedra y queda registrada en cada PR (sección "Cómo se probó").
