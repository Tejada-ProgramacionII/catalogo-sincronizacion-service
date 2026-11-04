# Seguridad

## Identidades

Hay dos identidades distintas, y ninguna credencial de una se usa para la otra:

| Identidad | Quién la emite | Para qué la usa el catálogo |
| --- | --- | --- |
| **Usuario final** | turnos-reservas-service (login, JWT HS512) | Autorizar los pedidos a `/api/**`. El catálogo no emite tokens ni guarda usuarios o contraseñas. |
| **Cuenta técnica del proyecto** | La cátedra | Pedir el snapshot (JWT técnico), leer Redis (usuario de solo lectura en `catedra:sync:*`) y consumir el topic de Kafka. Nunca se expone a la app ni a sus usuarios. |

## Validación del JWT de usuario (D-12)

El catálogo es un *resource server* sin estado (`spring-boot-starter-security-oauth2-resource-server`):

- **Quién lo manda.** La app lo manda en cada búsqueda, porque consulta al catálogo directo (D-01). Turnos **propaga el mismo JWT del usuario** cuando consulta un profesional; no hay un JWT técnico entre servicios.
- **Cómo se valida** (`JwtConfig`): un `NimbusJwtDecoder` con el secreto compartido valida la **firma HS512** y el **vencimiento**. El secreto llega en Base64 por `JWT_SECRET` y tiene que ser el mismo valor que usa turnos para firmar; si difieren, todos los pedidos responden 401.
- **Autoridades.** Se toman del claim `auth`, sin prefijo, igual que los emite turnos. Hoy ningún endpoint pide un rol: alcanza con estar autenticado.
- **Errores.** Sin token, con firma inválida o vencido, Spring Security responde **401 sin cuerpo**, con el motivo en el header `WWW-Authenticate` ([contrato-turnos.md](contrato-turnos.md#errores)).

## Endpoints públicos (`SecurityConfig`)

| Ruta | Por qué es pública |
| --- | --- |
| `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` | Documentación OpenAPI. |
| `/error` | Cuando hay un error interno, Spring reenvía el pedido a `/error` sin el token. Si `/error` pidiera token, ese reenvío terminaría en un 401 que esconde el error real. |

Cualquier otra ruta pide un JWT válido (`anyRequest().authenticated()`).

## CSRF desactivado (java:S4502)

`SecurityConfig` desactiva CSRF (`csrf(AbstractHttpConfigurer::disable)`) y usa sesiones `STATELESS`. SonarCloud lo marca como java:S4502, y está marcado como **falso positivo** con justificación. CSRF aprovecha que el navegador manda solo las credenciales que guarda (cookies de sesión). Esta API no usa cookies ni sesiones: el token va en el header `Authorization`, que el navegador no agrega por su cuenta. Por eso un sitio ajeno no puede hacer pedidos autenticados en nombre del usuario.

## Secretos por variables de entorno

Ningún secreto está en el repositorio, en los logs ni en la documentación. Todos llegan por variables de entorno:

| Variable | Qué es |
| --- | --- |
| `JWT_SECRET` | Secreto HS512 compartido con turnos. |
| `CATEDRA_JWT` | JWT técnico de la cuenta del proyecto. `CatedraClientConfig` lo pone como header `Authorization` del `RestClient`. |
| `CATEDRA_REDIS_USERNAME`, `CATEDRA_REDIS_PASSWORD` | Usuario de Redis de solo lectura en `catedra:sync:*`. |
| `CATEDRA_BASE_URL`, `CATEDRA_REDIS_HOST`, `CATEDRA_KAFKA_*` | Direcciones internas de la cátedra: tampoco van al repositorio. |

**Si falta una, el servicio no arranca.** Las propiedades usan `${VAR:}` y se validan con `@NotBlank` (`CatedraProperties`, `CatedraRedisProperties`, `CatedraKafkaProperties`). Sin el default vacío, Spring dejaría el placeholder literal y arrancaría igual. `jwt.secret=${JWT_SECRET}` no tiene default: sin la variable, el contexto no levanta. Los tests usan valores ficticios (`src/test/resources/application.properties`).

## Errores sin datos sensibles (D-11)

- **El registro de las sincronizaciones**, que se informa en `GET /api/sincronizacion/estado`, guarda **motivos funcionales fijos**: *"La cátedra rechazó el pedido de la integración"*, *"La cátedra no respondió después de 3 intentos"*, *"Error inesperado al sincronizar el catálogo"*, *"Interrumpida por un reinicio del servicio"*. Nunca guarda tokens, credenciales, hosts ni trazas.
- **El detalle técnico va solo a los logs**, también sin secretos. Por ejemplo, los adaptadores de Redis informan *"Redis rechazó las credenciales o los permisos de la integración"* sin el usuario ni el host, y hay tests que lo verifican.
- **Las respuestas de error de la API** (`ProblemDetail`) solo dicen qué falló desde el punto de vista del usuario: categoría inexistente, profesional inexistente o catálogo no disponible.
