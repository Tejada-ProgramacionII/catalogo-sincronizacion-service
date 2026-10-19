# CU-06. Informar estado de sincronización

## Identificación

| Campo | Descripción |
| --- | --- |
| Objetivo | Informar en qué estado está la copia local del catálogo y cuál fue el último error de sincronización. |
| Actores principales | Usuario final (desde la app KMP) y servicio de turnos. |
| Actor de apoyo | Ninguno: se responde con lo que el servicio registró en CU-01, CU-02 y CU-03. |
| Disparador | La app o el servicio de turnos consultan el estado. |

## Precondiciones

- Quien consulta envía un JWT válido.

## Flujo principal

1. La app o el servicio de turnos piden el estado de sincronización.
2. El servicio responde con:
   - **Estado:** uno de los descritos en las reglas.
   - **Versión local aplicada** y **fecha de la última sincronización exitosa**.
   - **Último error**, si lo hubo: cuándo ocurrió, en qué proceso (completa, incremental o recuperación) y un motivo funcional.

## Flujos alternativos

- **2a. Todavía no hubo ninguna sincronización exitosa.** El estado es "sin datos", sin versión ni fecha de última sincronización.
- **JWT inválido o ausente.** El servicio rechaza la consulta.

## Postcondiciones

- No se modifica nada: es una consulta.

## Reglas

- **D-10. Estados de sincronización:**

  | Estado | Significado |
  | --- | --- |
  | Sin datos | Nunca se aplicó una copia completa. |
  | Al día | La última verificación no encontró versiones pendientes. |
  | Sincronizando | Hay una sincronización completa, incremental o una recuperación en curso. |
  | Con error | El último intento falló; la copia que se consulta es la última completa. |

- **D-11. El último error se informa sin datos sensibles:** se indica el proceso y un motivo funcional (por ejemplo, "la cátedra no respondió" o "discontinuidad de versiones"), nunca tokens, credenciales, direcciones internas ni trazas técnicas. El detalle técnico queda en los logs del servicio.
- La app usa este estado para avisar al usuario que los resultados pueden estar desactualizados. El servicio de turnos lo usa para saber con qué versión del catálogo está trabajando.

## Fuentes

- Enunciado: secciones 4.1 ("informar el estado y los errores de sincronización"), 5 (punto 15) y 9.
- Contrato de integración: secciones 16 y 18.
