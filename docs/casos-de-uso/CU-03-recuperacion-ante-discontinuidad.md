# CU-03. Recuperación ante discontinuidad

## Identificación

| Campo | Descripción |
| --- | --- |
| Objetivo | Volver a tener una copia local verificable cuando no es seguro continuar con la sincronización incremental. |
| Actor principal | Ninguno: es un proceso automático del servicio. |
| Actor de apoyo | Servicio de la cátedra, a través de CU-01 (snapshot) y CU-02 (cambios posteriores). |
| Disparador | CU-02 detecta que no puede continuar desde la versión local. |

## Situaciones que lo disparan

- La versión local es menor que la versión más antigua disponible en Redis: falta el historial que la conecta con las versiones publicadas. Ejemplo del contrato: versión local 3, más antigua disponible 4 y actual 7.
- Faltan en Redis los cambios de una versión pendiente.
- Una entidad que figura como cambiada no existe en Redis.

En todos los casos no se puede comprobar cómo debería quedar la copia, así que no se aplican cambios sueltos.

## Flujo principal

1. El servicio registra el motivo de la discontinuidad (ver CU-06).
2. El servicio ejecuta la sincronización completa (CU-01).
3. Con la versión del snapshot como nueva versión local, el servicio verifica si hay versiones posteriores publicadas.
4. Si las hay, las incorpora con la sincronización incremental (CU-02).
5. El servicio registra que la recuperación terminó correctamente.

## Flujos alternativos

- **2a. Falla la sincronización completa.** Se aplica lo definido en CU-01: la copia anterior queda intacta, el error queda registrado y se vuelve a intentar con el próximo disparador.
- **4a. CU-02 vuelve a encontrar una discontinuidad.** El servicio no inicia otra reconstrucción en este mismo intento: registra el problema y termina. Lo vuelve a intentar con el próximo disparador.
- **4b. Falla CU-02 por otro motivo** (por ejemplo, Redis no responde). La copia queda en la versión del snapshot, que es consistente, y el atraso se recupera con el próximo disparador.

## Postcondiciones

- **Éxito:** la copia local corresponde a una versión verificable (la del snapshot o una posterior aplicada en orden).
- **Fallo:** se conserva la última copia consistente y queda registrado por qué no se pudo recuperar.

## Reglas

- Ante una discontinuidad nunca se aplican cambios salteando versiones.
- **D-08. Una sola reconstrucción por disparador:** evita un ciclo infinito de snapshots si la cátedra sigue publicando versiones más rápido de lo que el servicio se recupera.

## Fuentes

- Enunciado: secciones 5 (punto 6), 6.1, 6.2 y 8.
- Contrato de integración: secciones 14.2, 16 y 18.2.
