# CU-02. Sincronización incremental

## Identificación

| Campo | Descripción |
| --- | --- |
| Objetivo | Incorporar a la copia local los cambios publicados por la cátedra después de la versión local, en orden y sin repetir efectos. |
| Actor principal | Ninguno: es un proceso automático del servicio. |
| Actor de apoyo | Servicio de la cátedra, que avisa por Kafka que hay una versión nueva y publica en Redis la metadata, los cambios por versión y el estado actual de cada entidad. |
| Disparadores | 1) Llega un aviso `CatalogUpdated` por Kafka. 2) El servicio arranca con una copia local existente. 3) Se cumple el intervalo de verificación periódica. |

## Precondiciones

- Existe una copia local completa con una versión aplicada (si no existe, corresponde CU-01).
- El servicio tiene configurada la conexión a Kafka y a Redis de su integración técnica.

## Flujo principal

1. El servicio recibe el disparador. Si es un aviso de Kafka, registra su `eventId` y su versión.
2. Si la versión avisada es menor o igual a la versión local, el aviso ya está incorporado: el servicio lo da por procesado y termina.
3. El servicio lee en Redis la versión actual publicada y la versión más antigua disponible.
4. Si la versión local ya es igual a la versión actual publicada, no hay nada para aplicar y termina.
5. El servicio comprueba que puede continuar desde su versión: la versión local no puede ser menor que la versión más antigua disponible en Redis.
6. Para cada versión pendiente, en orden ascendente:
   1. Lee la lista de cambios de esa versión: qué categorías, profesionales y horarios cambiaron.
   2. Lee en Redis el estado actual de cada entidad afectada.
   3. Actualiza la copia local con esos estados y avanza la versión local a esa versión, todo en una misma operación.
7. Cuando no quedan versiones pendientes, el servicio registra que la sincronización incremental terminó correctamente y da el aviso por procesado.

## Flujos alternativos

- **2a. Aviso duplicado o fuera de orden.** Es el caso del paso 2: no se modifica nada.
- **3a. Redis no responde.** El servicio reintenta un número limitado de veces. Si se agotan los intentos, registra el error (ver CU-06) y termina sin modificar la copia ni la versión. El atraso se recupera con el próximo disparador.
- **5a. Discontinuidad:** la versión local es menor que la versión más antigua disponible, así que falta el historial que la conecta con las versiones publicadas (la ventana de historial avanzó). El servicio no intenta aplicar cambios salteados y continúa en CU-03.
- **6.1a / 6.2a. Faltan los cambios de una versión o una entidad afectada no está en Redis.** El estado no se puede verificar: el servicio no avanza esa versión y continúa en CU-03.
- **6.3a. Falla la actualización de la copia local de una versión.** Se descartan los cambios de esa versión y la versión local queda en la última aplicada correctamente. El servicio registra el error y termina. El próximo disparador retoma desde ahí.

## Postcondiciones

- **Éxito:** la versión local es igual a la versión actual publicada y la copia local refleja el estado de esa versión.
- **Fallo:** la versión local es la última aplicada completa; ninguna versión quedó aplicada a medias y el error queda registrado.

## Reglas

- **D-05. Idempotencia por versión:** un aviso o un cambio ya incorporado no produce efectos nuevos. Recibir dos veces el mismo aviso deja el mismo resultado final.
- **D-06. Una versión se aplica completa o no se aplica:** la versión local solo avanza cuando terminaron bien todos los cambios de esa versión.
- **D-07. Kafka avisa, Redis decide:** el aviso solo dispara la verificación; lo que se aplica sale siempre de comparar la versión local con la publicada en Redis. Por eso también se verifica al arrancar y periódicamente, y un aviso perdido no deja la copia atrasada.
- Las versiones se aplican en orden ascendente, sin saltear ninguna.
- Los cambios de Redis indican qué entidades cambiaron, no cómo quedaron: el estado se lee siempre del dato actual de cada entidad.
- Las entidades deshabilitadas se guardan igual: son bajas lógicas.
- El aviso de Kafka se da por procesado recién después de guardar los cambios.

## Fuentes

- Enunciado: secciones 5, 6.2 y 8.
- Contrato de integración: secciones 14.2 a 14.5 (Redis), 15.1 y 15.3 (Kafka), 16 (diagrama de sincronización) y 18.1 y 18.2.
