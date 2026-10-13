# CU-04. Buscar y filtrar profesionales

## Identificación

| Campo | Descripción |
| --- | --- |
| Objetivo | Permitir que el usuario encuentre profesionales usando filtros sobre la copia local del catálogo. |
| Actor principal | Usuario final, desde la app KMP. |
| Actor de apoyo | Ninguno: no se consulta a la cátedra durante la búsqueda. |
| Disparador | El usuario realiza una búsqueda en la app. |

## Precondiciones

- El usuario inició sesión y la app envía su JWT.
- Existe una copia local completa del catálogo.

## Flujo principal

1. El usuario indica los filtros que quiere usar. Todos son opcionales y se pueden combinar:
   - **Categoría:** una categoría del catálogo.
   - **Nombre:** un texto que se busca en el nombre o el apellido del profesional.
   - **Estado habilitado:** solo habilitados o solo deshabilitados.
   - **Disponibilidad:** una fecha.
2. El servicio busca en la copia local los profesionales que cumplen todos los filtros indicados.
3. El servicio devuelve los resultados paginados y ordenados por apellido y nombre. Cada resultado incluye los datos del profesional, su categoría y si está habilitado.

## Flujos alternativos

- **1a. Un filtro es inválido** (por ejemplo, una fecha con formato incorrecto o una categoría inexistente). El servicio responde con un error de validación que indica qué filtro está mal, sin ejecutar la búsqueda.
- **2a. Ningún profesional cumple los filtros.** El servicio devuelve una lista vacía. Es un resultado válido, distinto de "catálogo no disponible".
- **2b. No existe una copia local completa** (primer arranque sin sincronización exitosa). El servicio responde que el catálogo todavía no está disponible (D-04).
- **2c. Hay una sincronización en curso.** La búsqueda se resuelve sobre la última copia completa (D-02).
- **Sin sesión o con JWT inválido.** El servicio rechaza la búsqueda sin ejecutarla.

## Postcondiciones

- La copia local no se modifica: es una consulta.
- El usuario recibe los profesionales que cumplen los filtros, o un error que explica por qué no se pudo buscar.

## Reglas

- **Solo datos locales:** ninguna búsqueda consulta al servicio de la cátedra.
- La búsqueda por nombre no distingue mayúsculas de minúsculas y acepta coincidencias parciales.
- **D-09. Significado del filtro de disponibilidad:** un profesional está disponible en una fecha si está habilitado y tiene al menos un horario semanal habilitado para el día de la semana de esa fecha. Es la disponibilidad que se puede saber con el catálogo; si quedan turnos libres ese día depende de las ocupaciones de la cátedra, y eso lo resuelve el servicio de turnos.

## Fuentes

- Enunciado: secciones 4.1, 5 (punto 7), 6.2 y 11.
- Contrato de integración: secciones 7 y 14.4 (estructura de categorías, profesionales y horarios).
