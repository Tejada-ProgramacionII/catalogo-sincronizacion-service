# CU-05. Consultar profesional y agenda vigente

## Identificación

| Campo | Descripción |
| --- | --- |
| Objetivo | Entregar al servicio de turnos los datos vigentes de un profesional y su agenda semanal, para que pueda construir la disponibilidad o iniciar una reserva. |
| Actor principal | Servicio de turnos. |
| Actor de apoyo | Ninguno: se responde con la copia local. |
| Disparador | El servicio de turnos necesita validar un profesional antes de mostrar disponibilidad o de iniciar una reserva. |

## Precondiciones

- El servicio de turnos se autentica ante el catálogo con un JWT válido.
- Existe una copia local completa del catálogo.

## Flujo principal

1. El servicio de turnos pide un profesional por su identificador.
2. El servicio busca el profesional en la copia local.
3. El servicio responde con:
   - los datos del profesional (nombre, apellido, categoría y si está habilitado);
   - sus horarios semanales habilitados (día, hora de inicio, hora de fin y duración de cada turno);
   - la versión local del catálogo con la que se armó la respuesta.

## Flujos alternativos

- **2a. El profesional no existe en la copia local.** El servicio responde que no se encontró. Turnos no debe iniciar la operación.
- **3a. El profesional está deshabilitado.** El servicio responde igual, indicando que está deshabilitado. Turnos no debe iniciar reservas nuevas para él.
- **3b. El profesional no tiene horarios habilitados.** El servicio responde con la lista de horarios vacía.
- **2b. No existe una copia local completa.** El servicio responde que el catálogo todavía no está disponible (D-04).
- **2c. Hay una sincronización en curso.** Se responde con la última copia completa (D-02).
- **JWT inválido o ausente.** El servicio rechaza la consulta.

## Postcondiciones

- La copia local no se modifica: es una consulta.
- Turnos recibe la información vigente del profesional o un motivo claro para no iniciar la operación.

## Reglas

- Este es el único medio por el que turnos obtiene datos del catálogo: no accede a su base de datos ni guarda una réplica propia del catálogo (enunciado 3).
- Si el catálogo no responde, turnos no puede iniciar operaciones que requieran validar información vigente (enunciado 8). Esa reacción se documenta en los casos de uso de turnos.
- Turnos puede conservar en una reserva datos descriptivos del profesional como historial, pero no usarlos como catálogo vigente (enunciado 4.2).
- **PENDIENTE (Etapa de seguridad):** si la consulta lleva el JWT del usuario o un JWT técnico entre servicios.

## Fuentes

- Enunciado: secciones 3, 4.1, 4.2, 7 y 8.
- Contrato de integración: sección 17 (diagrama de reserva: turnos obtiene la agenda vigente del catálogo).
