# API del catálogo

Todos los endpoints `/api/**` piden el JWT de usuario que emite turnos ([seguridad.md](seguridad.md)):

```
Authorization: Bearer <JWT del usuario>
```

| Método | Ruta | Caso de uso | Respuestas |
| --- | --- | --- | --- |
| `GET` | `/api/profesionales` | Buscar y filtrar profesionales (CU-04) | 200, 400, 401, 503 |
| `GET` | `/api/profesionales/{id}` | Consultar un profesional y su agenda vigente (CU-05) | 200, 400, 401, 404, 503 |
| `GET` | `/api/sincronizacion/estado` | Informar el estado de la sincronización (CU-06) | 200, 401 |

Públicos, sin token: la documentación OpenAPI (`/v3/api-docs/**`) y Swagger UI (`/swagger-ui/**`, `/swagger-ui.html`).

Las respuestas son JSON. Las fechas van en UTC con formato ISO 8601 y las horas como `HH:mm:ss`. Los errores 400, 404 y 503 siguen el formato `ProblemDetail` (RFC 9457, `application/problem+json`). El 401 lo responde Spring Security sin cuerpo.

## Buscar profesionales

```
GET /api/profesionales?categoriaId=103&nombre=al&habilitado=true&fecha=2026-11-09&page=0&size=20
```

Busca **solo en la copia local**: ninguna búsqueda consulta a la cátedra.

| Parámetro | Tipo | Por defecto | Descripción |
| --- | --- | --- | --- |
| `categoriaId` | número | — | Solo los profesionales de esa categoría. Tiene que existir en la copia local. |
| `nombre` | texto | — | Coincidencia parcial en el nombre **o** el apellido, sin distinguir mayúsculas. En blanco no filtra. |
| `habilitado` | booleano | — | `true` solo habilitados, `false` solo deshabilitados. |
| `fecha` | fecha `yyyy-MM-dd` | — | Disponibilidad (D-09): profesional habilitado con al menos un horario semanal habilitado para el **día de la semana** de esa fecha. Las ocupaciones reales las resuelve turnos. |
| `page` | número ≥ 0 | `0` | Página, empezando en 0. |
| `size` | número entre 1 y 100 | `20` | Tamaño de la página. |

Todos los filtros son opcionales y se combinan con Y. Los resultados se ordenan por apellido, nombre e `id` (el `id` desempata para que el orden entre páginas sea estable).

### 200 OK

Respuesta real de `GET /api/profesionales?categoriaId=103&size=2` sobre la copia local (versión 1 del catálogo):

```json
{
  "contenido": [
    { "id": 207, "categoriaId": 103, "nombre": "Diego", "apellido": "Alvarez", "habilitado": true },
    { "id": 223, "categoriaId": 103, "nombre": "Rocio", "apellido": "Benitez", "habilitado": true }
  ],
  "numero": 0,
  "tamanio": 2,
  "totalElementos": 4,
  "totalPaginas": 2
}
```

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `contenido` | lista | Profesionales de la página. Vacía si ninguno cumple los filtros: es un resultado válido, distinto de *catálogo no disponible*. |
| `contenido[].id`, `contenido[].categoriaId` | número | Identificadores de la cátedra. |
| `contenido[].nombre`, `contenido[].apellido` | texto | Datos del profesional. |
| `contenido[].habilitado` | booleano | `false` si está dado de baja. |
| `numero`, `tamanio` | número | Página y tamaño pedidos. |
| `totalElementos`, `totalPaginas` | número | Totales de la búsqueda. |

### Errores

**400 Bad Request**, sin ejecutar la búsqueda:

- La categoría no existe en la copia local:

  ```json
  {
    "title": "Bad Request",
    "status": 400,
    "detail": "La categoría 99 no existe",
    "instance": "/api/profesionales"
  }
  ```

- Un parámetro con formato inválido (por ejemplo, `fecha=14/09/2026` o `categoriaId=abc`) o fuera de rango (`page` negativo, `size` menor que 1 o mayor que 100). El `ProblemDetail` lo arma Spring e indica qué parámetro falló.

**401 Unauthorized**: sin token, o con un token inválido o vencido.

**503 Service Unavailable**: todavía no hay una sincronización completa aplicada (D-04). No se responde una lista vacía, porque haría creer que no hay profesionales.

```json
{
  "title": "Service Unavailable",
  "status": 503,
  "detail": "El catálogo todavía no está disponible: no hay una sincronización completa aplicada",
  "instance": "/api/profesionales"
}
```

## Consultar un profesional y su agenda

```
GET /api/profesionales/{id}
```

Es el endpoint que consume turnos. Su contrato completo (campos, autenticación y errores) está en [contrato-turnos.md](contrato-turnos.md). Resumen:

- **200**: datos del profesional, sus horarios semanales **habilitados** ordenados por día y hora de inicio, y `versionCatalogo` (la versión local con la que se armó la respuesta). Un profesional deshabilitado se responde igual, con `habilitado: false`.
- **400**: `id` no numérico.
- **401**: sin token o con un token inválido o vencido.
- **404**: el profesional no existe en la copia local (`"detail": "No existe el profesional 99999"`).
- **503**: sin sincronización completa aplicada (D-04).

## Estado de la sincronización

```
GET /api/sincronizacion/estado
```

Siempre responde **200**, también sin datos; sin token, **401**.

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

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `estado` | texto | `SIN_DATOS`, `SINCRONIZANDO`, `CON_ERROR` o `AL_DIA` (D-10). Cómo se calcula: [sincronizacion.md](sincronizacion.md#estado-de-la-sincronización). |
| `versionAplicada`, `aplicadaEn` | número, fecha | Versión de la copia local y cuándo se aplicó; `null` si nunca se aplicó una copia completa. |
| `ultimoIntento` | objeto | La última sincronización registrada; `null` si no hubo ninguna. |
| `ultimoIntento.proceso` | texto | `COMPLETA`, `INCREMENTAL` o `RECUPERACION`. |
| `ultimoIntento.inicio`, `ultimoIntento.fin` | fecha | `fin` es `null` mientras está en curso. |
| `ultimoIntento.resultado` | texto | `OK`, `CATEDRA_NO_DISPONIBLE`, `RECHAZO`, `DISCONTINUIDAD` o `ERROR`; `null` mientras está en curso. |
| `ultimoIntento.intentos` | número | Pedidos hechos a la cátedra (REST o Redis), reintentos incluidos. |
| `ultimoIntento.mensaje` | texto | Motivo funcional sin datos sensibles (D-11); `null` si terminó bien sin recuperar. |
| `ultimaVerificacion` | fecha | Última verificación contra Redis sin versiones pendientes; `null` si no hubo ninguna. |

Con error, conservando la copia anterior:

```json
{
  "estado": "CON_ERROR",
  "versionAplicada": 1,
  "aplicadaEn": "2026-10-27T21:37:37.530100Z",
  "ultimoIntento": {
    "proceso": "INCREMENTAL",
    "inicio": "2026-11-03T15:52:40.054159Z",
    "fin": "2026-11-03T15:52:50.119963Z",
    "resultado": "CATEDRA_NO_DISPONIBLE",
    "intentos": 3,
    "mensaje": "La cátedra no respondió después de 3 intentos"
  },
  "ultimaVerificacion": "2026-10-28T14:52:01.204379Z"
}
```
