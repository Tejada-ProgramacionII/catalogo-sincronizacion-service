# CU-01. Sincronización completa

## Identificación

| Campo | Descripción |
| --- | --- |
| Objetivo | Reemplazar la copia local del catálogo por un snapshot completo de la cátedra y registrar su versión. |
| Actor principal | Ninguno: es un proceso automático del servicio. |
| Actor de apoyo | Servicio de la cátedra, que entrega el snapshot por REST. |
| Disparadores | 1) El servicio arranca con la copia local vacía. 2) CU-03 determina que no se puede continuar con la sincronización incremental. |

## Precondiciones

- El servicio tiene configurada la cuenta técnica de integración con la cátedra.
- No se exige que exista una copia local previa: este caso cubre también la primera carga.

## Flujo principal

1. El servicio pide el snapshot completo al servicio de la cátedra.
2. La cátedra responde con las categorías, los profesionales, los horarios semanales (habilitados y deshabilitados) y el número de versión del snapshot.
3. El servicio reemplaza la copia local por las tres colecciones recibidas.
4. En la misma operación, registra el número de versión del snapshot como versión local aplicada.
5. El servicio registra que la sincronización completa terminó correctamente. Desde ese momento, las búsquedas y consultas usan la copia nueva.

## Flujos alternativos

- **1a. La cátedra no responde o devuelve un error.** El servicio reintenta un número limitado de veces. Si se agotan los intentos, registra el error (ver CU-06) y termina sin modificar la copia local ni la versión. Se vuelve a intentar con el próximo disparador.
- **1b. La cátedra rechaza la autenticación técnica.** El servicio registra el error de integración y termina sin reintentar, porque reintentar con las mismas credenciales no cambia el resultado.
- **3a. Falla el reemplazo de la copia local** (por ejemplo, un error de la base de datos). Se descarta todo lo aplicado en esa operación: la copia anterior y su versión quedan intactas. El servicio registra el error y termina.
- **3b. Llegan búsquedas o consultas mientras se aplica el snapshot.** Se responden con la copia anterior, que sigue completa hasta que el paso 4 termina. El flujo principal continúa.
- **1a / 3a sin copia anterior** (primer arranque). Las búsquedas y consultas responden que el catálogo todavía no está disponible, en lugar de devolver resultados vacíos.

## Postcondiciones

- **Éxito:** la copia local contiene exactamente las tres colecciones del snapshot y la versión local es exactamente la versión del snapshot.
- **Fallo:** la copia local y la versión siguen siendo las de antes del intento (o siguen vacías, si era la primera carga), y el error queda registrado.

## Reglas

- **D-02. Reemplazo todo o nada:** la copia local nunca queda con una parte del snapshot nuevo y otra parte del anterior.
- **D-03. Reintentos limitados:** no hay ciclos de reintento infinitos. La cantidad de intentos y la espera entre ellos se definen en el diseño.
- **D-04. Catálogo no disponible:** sin una copia local completa, el servicio no responde como si el catálogo estuviera vacío.
- El snapshot incluye entidades deshabilitadas: se guardan igual, porque representan bajas lógicas y los filtros las necesitan.
- El snapshot no trae el historial de cambios. Lo que ocurra después de su versión se incorpora con CU-02.

## Fuentes

- Enunciado: secciones 4.1, 6.1 y 8.
- Contrato de integración: secciones 7 (snapshot), 13 (errores REST), 16 (diagrama de sincronización) y 18.2.
