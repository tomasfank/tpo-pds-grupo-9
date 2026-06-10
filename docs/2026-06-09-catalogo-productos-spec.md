# Spec - Catalogo de Productos

## Objetivo

Implementar la primera version funcional del catalogo de RIVA en backend y frontend, cubriendo navegacion por categorias, busqueda por filtros basicos y detalle de producto, usando el patron `Composite` del UML en el backend.

## Casos de uso cubiertos

- `CU-07`: Navegar Catalogo por Categorias
- `CU-08`: Buscar Productos por Filtros
- `CU-09`: Ver Detalle de Producto

## Alcance

La feature debe dejar operativo un flujo publico de catalogo end-to-end:

- visualizar la jerarquia de categorias y subcategorias
- listar productos activos por categoria o subarbol
- buscar productos por filtros basicos
- ver el detalle de un producto
- mostrar variantes disponibles y estado de stock
- consumir datos reales desde el backend propio
- poblar la base con una seed inicial consistente con el dominio de RIVA

## Fuera de alcance

Esta feature no incluye:

- carrito
- autenticacion y autorizacion con JWT
- panel administrativo de gestion completo
- carga real de imagenes o integracion con storage
- integracion con Fake Store API
- pagos, pedidos o notificaciones

## Decision de diseno

### Criterio de cumplimiento del UML

Para esta feature no es obligatorio replicar el UML al 100% en nombres exactos de clases, atributos o metodos.

Lo obligatorio es:

- que el patron `Composite` exista realmente en backend
- que las responsabilidades minimas del patron esten cubiertas
- que no falten piezas necesarias para justificar la implementacion del patron

Se permite:

- agregar atributos o metodos extra
- usar nombres de clases o propiedades distintos al diagrama
- introducir detalles tecnicos de persistencia o transporte que no aparezcan en UML

No se permite:

- reemplazar el `Composite` por una implementacion plana sin jerarquia real
- omitir la separacion conceptual entre componente comun, compuesto y hoja
- resolver la feature solo con queries sueltas si el modelo deja de reflejar el patron

### Backend

El backend debe implementar el patron `Composite` para el catalogo segun el UML:

- `ComponenteCatalogo` es la abstraccion comun
- `Categoria` es el compuesto
- `Producto` es la hoja

La logica de navegacion del catalogo debe apoyarse en esa estructura. No alcanza con exponer tablas o colecciones sueltas: la jerarquia tiene que estar representada en el modelo y ser la base de las consultas del catalogo.

### Frontend

El frontend no necesita implementar ningun patron de diseno especifico. Su responsabilidad es consumir el backend propio y resolver la experiencia de:

- home/catalogo
- vista por categoria
- filtros
- detalle de producto

### Seed

La seed inicial debe incluir:

- categorias raiz
- subcategorias
- productos
- variantes por talle y color
- stock por variante
- una URL generica de imagen por producto

La imagen generica se usa solo como dato temporal. La feature no debe quedar acoplada a Fake Store API.

## Requerimientos funcionales de la feature

### 1. Jerarquia de catalogo

El sistema debe exponer la jerarquia de categorias y subcategorias activas para que el frontend pueda navegarla.

Requisitos:

- una categoria puede tener cero o muchas subcategorias
- una categoria puede agrupar productos directa o indirectamente
- no deben mostrarse categorias inactivas en la navegacion publica

### 2. Listado de productos por categoria

El sistema debe permitir listar los productos activos contenidos en una categoria seleccionada, incluyendo los que pertenezcan a subcategorias del subarbol.

Requisitos:

- si el usuario selecciona una categoria padre, se deben incluir productos del subarbol completo
- solo deben devolverse productos activos
- el listado debe incluir la informacion necesaria para una card de producto

### 3. Busqueda por filtros

El sistema debe permitir filtrar productos activos por:

- nombre
- categoria
- talle
- color

Filtro opcional para este primer corte si el costo es bajo:

- rango de precio

Decision:

- `nombre`, `categoria`, `talle` y `color` forman parte del alcance obligatorio
- `precioMin` y `precioMax` quedan como opcionales del primer corte, pero conviene modelarlos desde el inicio en el contrato si no agregan complejidad innecesaria

### 4. Detalle de producto

El sistema debe devolver el detalle completo de un producto activo.

Requisitos:

- nombre
- descripcion
- precio
- material
- marca
- imagenes
- categoria asociada
- variantes disponibles
- stock por variante

Si el producto no existe o esta inactivo, el backend debe responder con error consistente y el frontend debe redirigir o mostrar estado vacio segun corresponda.

### 5. Estado de stock

El detalle de producto debe permitir detectar si:

- hay stock en alguna variante
- no hay stock en ninguna variante

En el frontend:

- si ninguna variante tiene stock, debe mostrarse como `Sin stock`
- en esta feature no hace falta implementar compra ni reserva, solo visualizacion correcta

## Diseno backend

### Modelo de dominio

El backend ya tiene una base alineada con el UML:

- `Category`
- `Product`
- `ProductVariant`
- `CatalogComponent`

La implementacion de esta feature debe consolidar ese modelo y evitar bypasses que rompan el `Composite`.

No hace falta que los nombres del codigo coincidan uno a uno con el diagrama, pero si hace falta que el modelo cubra como minimo:

- una abstraccion comun para nodos del catalogo
- un nodo compuesto capaz de agrupar otros nodos o representar la jerarquia
- un nodo hoja que represente al producto final
- una forma consistente de obtener productos desde una categoria o subarbol

### Responsabilidades esperadas

#### `CatalogComponent`

- representar la abstraccion comun del catalogo
- definir las operaciones comunes necesarias para navegar la estructura

#### `Category`

- representar nodos compuestos
- mantener referencia a jerarquia por padre y ancestros
- permitir obtener productos del subarbol

#### `Product`

- representar nodo hoja
- contener atributos de negocio del producto
- contener variantes
- contener lista de imagenes

### Endpoints esperados

#### Navegacion de catalogo

- `GET /api/catalog/tree`

Devuelve el arbol publico de categorias activas.

#### Productos por categoria

- `GET /api/catalog/categories/{id}/products`

Devuelve productos activos de la categoria seleccionada y su subarbol.

#### Busqueda de productos

- `GET /api/products`

Debe evolucionar para aceptar filtros por query params, por ejemplo:

- `name`
- `categoryId`
- `size`
- `color`
- `priceMin`
- `priceMax`

Decision de contrato:

- si no hay filtros, devuelve todos los productos activos
- si hay filtros, devuelve solo coincidencias

#### Detalle de producto

- `GET /api/products/{id}`

Debe devolver el producto completo listo para la pantalla de detalle.

### Reglas de negocio backend

- no devolver productos inactivos en endpoints publicos
- no devolver categorias inactivas en el arbol publico
- el filtro por talle y color debe aplicarse sobre variantes
- el detalle de producto debe incluir variantes aunque alguna tenga stock cero
- la seed debe respetar la relacion categoria-producto-variantes

### Seed inicial

La base debe poder levantarse con datos minimos sin carga manual.

Contenido esperado:

- categorias raiz: por ejemplo `Hombre`, `Mujer`, `Ninos`
- subcategorias: por ejemplo `Remeras`, `Pantalones`, `Camperas`
- al menos 2 o 3 productos por grupo principal para probar navegacion y filtros
- al menos 2 variantes por producto
- una imagen generica por producto

Decision para imagen temporal:

- usar una URL generica controlada por el proyecto
- no usar todavia URLs de Fake Store API

Ejemplo valido:

- `https://placehold.co/800x1000?text=RIVA`

## Diseno frontend

### Objetivo

Reemplazar el consumo actual de Fake Store API por el backend propio y adaptar la UI existente al modelo real de RIVA.

### Pantallas o vistas impactadas

- home
- vista por categoria
- detalle de producto

### Cambios esperados

#### 1. Cliente API propio

Crear o adaptar cliente HTTP para consumir:

- arbol de categorias
- productos filtrados
- productos por categoria
- detalle de producto

#### 2. Tipos frontend

Actualizar los tipos para reflejar el backend real:

- producto
- variante
- categoria

Debe desaparecer la dependencia del shape de Fake Store API.

#### 3. Home y navegacion

La home debe usar categorias y productos reales.

Requisitos:

- construir la navegacion a partir del arbol de categorias
- permitir entrar a una categoria y ver sus productos
- dejar de hardcodear `men`, `women`, `kids` como unica fuente de verdad

#### 4. Filtros

La vista de catalogo debe ofrecer filtros basicos:

- nombre
- talle
- color
- categoria actual o seleccionada

El filtro puede resolverse:

- desde backend por query params
- con un estado simple en frontend que dispare nuevas consultas

#### 5. Detalle de producto

Debe mostrar:

- imagen principal
- nombre
- descripcion
- precio
- material
- variantes
- estado de stock

No hace falta aun:

- selector final para compra
- agregar al carrito persistente

Pero si conviene mostrar variantes de forma que despues pueda evolucionar a compra sin rehacer la vista.

#### 6. Manejo de estados

El frontend debe contemplar:

- loading
- error
- sin resultados
- producto no encontrado
- producto sin stock

## Tareas de implementacion

### Bloque 1. Consolidar modelo de catalogo en backend

- revisar que `CatalogComponent`, `Category` y `Product`, o sus equivalentes, respeten el `Composite` del UML
- ajustar metodos o contratos si hoy hay diferencias importantes con el diagrama o falta alguna responsabilidad del patron
- validar que la navegacion de subarbol se apoye en la jerarquia y no en atajos inconsistentes

### Bloque 2. Exponer endpoints publicos de catalogo

- cerrar contrato de `GET /api/catalog/tree`
- cerrar contrato de `GET /api/catalog/categories/{id}/products`
- extender `GET /api/products` con filtros
- validar `GET /api/products/{id}` para detalle publico

### Bloque 3. Seed inicial

- definir datos minimos de categorias
- definir productos y variantes
- asignar imagen generica por producto
- asegurar que el entorno local levante con datos navegables

### Bloque 4. Adaptar frontend al backend propio

- reemplazar cliente Fake Store
- actualizar tipos
- conectar home y categorias a endpoints reales
- conectar detalle de producto a endpoint real

### Bloque 5. Filtros y estados de UI

- agregar filtros basicos en catalogo
- manejar loading, error y vacios
- mostrar `Sin stock` cuando corresponda

### Bloque 6. Testing

#### Backend

- tests de servicio para recorrido de categorias y subarbol
- tests de filtros por nombre, categoria, talle y color
- tests de detalle publico de producto activo
- tests de exclusion de productos o categorias inactivas

#### Frontend

- tests de render basico de listado
- tests de carga de detalle
- tests de estado vacio y error

## Criterios de aceptacion

La feature se considera terminada cuando:

- el backend implementa el `Composite` del UML para el catalogo
- existe una seed inicial util para probar la navegacion
- el frontend deja de consumir Fake Store API
- el usuario puede navegar categorias reales
- el usuario puede ver productos por categoria
- el usuario puede filtrar productos por criterios basicos
- el usuario puede abrir el detalle de un producto real
- el usuario puede ver variantes y estado de stock

## Riesgos y decisiones abiertas

### Riesgos

- el frontend actual esta modelado contra un schema externo y va a requerir adaptacion de tipos y vistas
- si la seed no queda bien armada, el flujo puede parecer roto aunque el codigo funcione
- si el filtro de categoria y subcategoria no se define bien, se puede romper la idea central del `Composite`

### Decisiones ya tomadas

- el patron obligatorio para esta feature es `Composite` y vive en backend
- no es obligatorio copiar literalmente nombres de clases, atributos o metodos del UML, pero si cubrir todas las responsabilidades minimas del patron
- el frontend no implementa patron especifico
- la imagen inicial sera una URL generica por producto
- Fake Store API no forma parte de esta iteracion

## Orden recomendado de ejecucion

1. consolidar modelo y contratos backend
2. crear seed inicial
3. adaptar cliente API y tipos frontend
4. conectar listado y detalle
5. agregar filtros
6. cerrar tests y validacion manual
