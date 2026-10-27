# Contrato con turnos-reservas-service

Turnos obtiene del catálogo los datos vigentes de un profesional y su agenda semanal antes de mostrar disponibilidad o de iniciar una reserva ([CU-05](casos-de-uso/CU-05-consultar-profesional-y-agenda-vigente.md)). Es el único medio por el que turnos accede al catálogo: no lee su base ni guarda una réplica.

## Consultar un profesional y su agenda vigente

```
GET /api/profesionales/{id}
Authorization: Bearer <JWT del usuario>
```

| Parámetro | Tipo | Descripción |
| --- | --- | --- |
| `id` | número | Identificador del profesional (el que asigna la cátedra). |

### Autenticación

Turnos **propaga el JWT del usuario** que hizo el pedido (D-12). El catálogo valida la firma HS512 y el vencimiento con el mismo secreto (`JWT_SECRET`) con el que turnos lo emitió; no hay un JWT técnico entre servicios.

### 200 OK

```json
{
  "id": 201,
  "categoriaId": 101,
  "nombre": "Ana",
  "apellido": "Torres",
  "habilitado": true,
  "horarios": [
    { "diaSemana": "MONDAY", "horaInicio": "12:00:00", "horaFin": "15:00:00", "duracionTurnoMinutos": 30 },
    { "diaSemana": "WEDNESDAY", "horaInicio": "17:00:00", "horaFin": "20:00:00", "duracionTurnoMinutos": 30 }
  ],
  "versionCatalogo": 1
}
```

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `id`, `categoriaId` | número | Identificadores de la cátedra. |
| `nombre`, `apellido` | texto | Datos del profesional. |
| `habilitado` | booleano | `false` si el profesional está dado de baja: se responde igual y turnos no debe iniciar reservas nuevas para él (CU-05, 3a). |
| `horarios` | lista | Solo los horarios semanales **habilitados**, ordenados por día (de `MONDAY` a `SUNDAY`) y hora de inicio. Vacía si no tiene ninguno (CU-05, 3b). |
| `horarios[].diaSemana` | texto | `MONDAY` … `SUNDAY`. |
| `horarios[].horaInicio`, `horarios[].horaFin` | texto | Hora local `HH:mm:ss`. |
| `horarios[].duracionTurnoMinutos` | número | Duración de cada turno dentro de la franja. |
| `versionCatalogo` | número | Versión local del catálogo con la que se armó la respuesta. |

### Errores

**401 Unauthorized**: falta el token, o la firma no coincide, o venció. Lo responde Spring Security **sin cuerpo**; el motivo va en el header `WWW-Authenticate`:

```
HTTP/1.1 401
WWW-Authenticate: Bearer error="invalid_token", error_description="An error occurred while attempting to decode the Jwt: Jwt expired at 2026-10-27T13:05:46Z", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1", ...
```

Sin token, el header es solo `WWW-Authenticate: Bearer ...`, sin `error`.

**404 Not Found**: el profesional no existe en la copia local. Turnos no debe iniciar la operación.

```json
{
  "title": "Not Found",
  "status": 404,
  "detail": "No existe el profesional 99999",
  "instance": "/api/profesionales/99999"
}
```

**503 Service Unavailable**: todavía no hay una sincronización completa aplicada (D-04). Turnos no puede validar información vigente y no debe iniciar la operación.

```json
{
  "title": "Service Unavailable",
  "status": 503,
  "detail": "El catálogo todavía no está disponible: no hay una sincronización completa aplicada",
  "instance": "/api/profesionales/201"
}
```

Los 404 y 503 siguen el formato `ProblemDetail` (RFC 9457), igual que los errores de la búsqueda.

## Consultar el estado de la sincronización

```
GET /api/sincronizacion/estado
Authorization: Bearer <JWT del usuario>
```

Informa en qué estado está la copia local ([CU-06](casos-de-uso/CU-06-informar-estado-de-sincronizacion.md)): turnos lo usa para saber con qué versión del catálogo trabaja y la app para avisar que los resultados pueden estar desactualizados. La autenticación es la misma que en la consulta de profesional (D-12).

### 200 OK

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
