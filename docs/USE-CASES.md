# RIVA - Casos de Uso

Este documento contiene la especificación de casos de uso extraída del informe funcional principal para mantener [RIVA.md](RIVA.md) más liviano.

---

## 6. Especificación de Casos de Uso

A continuación se detallan los casos de uso del sistema, organizados por módulo funcional. Cada caso de uso es atómico y declara explícitamente sus precondiciones, incluyendo otros casos de uso de los que depende.

---

### CU-01: Registrarse como Cliente

| Campo | Detalle |
|---|---|
| Identificador | CU-01 |
| Estado de implementacion | DONE - `AuthController POST /api/auth/register` crea un `Cliente` (jerarquía `Usuario` fiel a la UML) validando email único y robustez mínima de contraseña (`PasswordPolicy`), hasheando con BCrypt. Frontend: `RegisterView` (vista pública) toma nombre/apellido/email/contraseña con confirmación local (excepción "las contraseñas no coinciden"), postea a `/api/auth/register` (`api/auth.ts`), surfacea los errores del backend (email duplicado, contraseña débil — flujo alternativo) y, ante éxito, redirige al login con mensaje de confirmación. |
| Nombre | Registrarse como Cliente |
| Actor principal | Usuario no registrado |
| Precondiciones | El usuario no posee cuenta en el sistema. El usuario accede a la plataforma desde la pantalla pública. |
| Postcondiciones | El usuario queda registrado con rol Cliente y habilitado para iniciar sesión. |
| Flujo principal | 1. El usuario accede a la pantalla de registro. 2. Ingresa nombre, apellido, email y contraseña. 3. El sistema valida que el email no esté registrado previamente. 4. El sistema valida formato de email y robustez mínima de la contraseña. 5. El sistema hashea la contraseña con BCrypt y persiste el nuevo usuario con rol Cliente. 6. El sistema muestra confirmación de registro exitoso. |
| Flujo alternativo | 3a. Si el email ya existe: el sistema informa el error y solicita un email diferente. 4a. Si la contraseña no cumple los requisitos mínimos: el sistema indica las reglas no cumplidas. |
| Excepciones | Campos obligatorios vacíos: el sistema muestra mensajes de validación por campo. |
| Patrones aplicados | — |

---

### CU-02: Iniciar Sesión como Cliente

| Campo | Detalle |
|---|---|
| Identificador | CU-02 |
| Estado de implementacion | DONE - `POST /api/auth/login` valida credenciales y devuelve JWT (con `userId`, email, rol) + error genérico ante datos incorrectos. Carrito y pedidos usan el principal autenticado (se eliminó `X-Cliente-Id`). Frontend: `ClientLoginView` valida el rol `CLIENTE` del lado del cliente (flujo alternativo 3a — rechaza administradores por esta vía), persiste el JWT en `localStorage` y redirige al inicio; el header muestra el saludo + Salir y `api/cart.ts`/`api/orders.ts`/`api/notifications.ts` adjuntan el Bearer. Carrito/pedidos/notificaciones exigen sesión de cliente (sin sesión → login). CU-05 (recuperar) y bloqueo por intentos quedan fuera de alcance. |
| Nombre | Iniciar Sesión como Cliente |
| Actor principal | Cliente |
| Precondiciones | El usuario posee una cuenta activa con rol Cliente (registrada mediante CU-01). El usuario no tiene una sesión activa. |
| Postcondiciones | El cliente accede al panel de cliente con su sesión establecida. |
| Flujo principal | 1. El cliente ingresa email y contraseña en la pantalla de login. 2. El sistema valida las credenciales contra el repositorio de usuarios. 3. El sistema verifica que el rol asociado sea Cliente. 4. El sistema genera el token de sesión y redirige al panel del cliente. |
| Flujo alternativo | 2a. Credenciales incorrectas: el sistema muestra error genérico ("Email o contraseña incorrectos") sin indicar cuál de los dos falló. 3a. Si la cuenta corresponde a un rol distinto: el sistema rechaza el acceso por esta vía. |
| Excepciones | Tres intentos fallidos consecutivos: el sistema bloquea temporalmente el acceso al email durante un período configurable. |
| Patrones aplicados | — |

---

### CU-03: Iniciar Sesión como Administrador

| Campo | Detalle |
|---|---|
| Identificador | CU-03 |
| Estado de implementacion | DONE - El mismo `POST /api/auth/login` autentica administradores (rol `ADMINISTRADOR`, provisionado vía `DataSeeder`). Los endpoints admin (productos, categorías, `POST /api/orders/{id}/advance`) quedan restringidos a `ROLE_ADMINISTRADOR` en `SecurityConfig`. Frontend: `AdminLoginView` (vista de login admin) valida el rol `ADMINISTRADOR` del lado del cliente (rechaza otros roles — flujo alternativo 3a), persiste el JWT en `localStorage` (`api/auth.ts`) y redirige al panel; el header expone los botones Admin/Salir (logout vía CU-04). |
| Nombre | Iniciar Sesión como Administrador |
| Actor principal | Administrador |
| Precondiciones | El usuario posee una cuenta activa con rol Administrador previamente provisionada en el sistema. El usuario no tiene una sesión activa. |
| Postcondiciones | El administrador accede al panel de administración con su sesión establecida. |
| Flujo principal | 1. El administrador ingresa email y contraseña en la pantalla de login administrativo. 2. El sistema valida las credenciales. 3. El sistema verifica que el rol asociado sea Administrador. 4. El sistema genera el token de sesión y redirige al panel de administración. |
| Flujo alternativo | 2a. Credenciales incorrectas: el sistema muestra error genérico. 3a. Si la cuenta corresponde a un rol distinto: el sistema rechaza el acceso administrativo. |
| Excepciones | Tres intentos fallidos consecutivos: el sistema bloquea temporalmente el acceso al email. |
| Patrones aplicados | — |

---

### CU-04: Cerrar Sesión

| Campo | Detalle |
|---|---|
| Identificador | CU-04 |
| Estado de implementacion | DONE (backend) - `POST /api/auth/logout` con JWT stateless: el cliente descarta el token (sin blocklist). El endpoint es público para cubrir el caso de token ya expirado sin error. |
| Nombre | Cerrar Sesión |
| Actor principal | Cliente / Administrador |
| Precondiciones | El usuario tiene una sesión activa (vía CU-02 o CU-03). |
| Postcondiciones | La sesión del usuario queda invalidada y se redirige a la pantalla pública. |
| Flujo principal | 1. El usuario selecciona la opción "Cerrar sesión" desde su panel. 2. El sistema invalida el token de sesión. 3. El sistema redirige a la pantalla de inicio pública. |
| Flujo alternativo | — |
| Excepciones | Token ya inválido o expirado: el sistema completa el cierre de sesión local sin error. |
| Patrones aplicados | — |

---

### CU-05: Recuperar Contraseña

| Campo | Detalle |
|---|---|
| Identificador | CU-05 |
| Estado de implementacion | PENDIENTE - No hay recuperacion de contrasena, tokens de recuperacion ni envio de email. |
| Nombre | Recuperar Contraseña |
| Actor principal | Usuario sin sesión activa |
| Precondiciones | El usuario posee una cuenta registrada con un email accesible. El usuario no tiene una sesión activa. |
| Postcondiciones | El usuario recibe un enlace de recuperación que le permite establecer una nueva contraseña. |
| Flujo principal | 1. El usuario accede a la opción "Olvidé mi contraseña" desde la pantalla de login. 2. Ingresa el email asociado a su cuenta. 3. El sistema genera un token de recuperación con expiración. 4. El sistema envía un enlace de recuperación al email registrado. 5. El usuario accede al enlace e ingresa la nueva contraseña dos veces. 6. El sistema valida el token, hashea la nueva contraseña y la persiste. 7. El sistema confirma el cambio y habilita el login con la nueva credencial. |
| Flujo alternativo | 2a. Email no registrado: el sistema muestra mensaje genérico ("Si el email existe, recibirás instrucciones") por seguridad. 6a. Token vencido o inválido: el sistema rechaza el cambio y solicita iniciar el flujo nuevamente. |
| Excepciones | Las dos contraseñas ingresadas no coinciden: el sistema solicita reingresarlas. |
| Patrones aplicados | — |

---

### CU-06: Cambiar Contraseña

| Campo | Detalle |
|---|---|
| Identificador | CU-06 |
| Estado de implementacion | DONE - `POST /api/auth/change-password` (autenticado) valida la contraseña actual contra el hash y la robustez de la nueva antes de re-hashear y persistir. Frontend: `AccountView` (sección "Cuenta", disponible para Cliente y Administrador) toma contraseña actual + nueva + repetición, valida localmente la coincidencia (excepción "las contraseñas no coinciden") y postea a `/api/auth/change-password` con el JWT (`api/auth.ts` adjunta el Bearer); surfacea los errores del backend (actual incorrecta — flujo 4a; nueva débil — flujo 5a). |
| Nombre | Cambiar Contraseña |
| Actor principal | Cliente / Administrador |
| Precondiciones | El usuario tiene una sesión activa (vía CU-02 o CU-03). |
| Postcondiciones | La contraseña del usuario queda actualizada. |
| Flujo principal | 1. El usuario accede a la sección de configuración de cuenta. 2. Selecciona "Cambiar contraseña". 3. Ingresa la contraseña actual y la nueva contraseña dos veces. 4. El sistema valida la contraseña actual contra el hash almacenado. 5. El sistema valida la robustez de la nueva contraseña. 6. El sistema hashea y persiste la nueva contraseña. 7. El sistema confirma el cambio. |
| Flujo alternativo | 4a. Contraseña actual incorrecta: el sistema rechaza el cambio. 5a. Nueva contraseña no cumple requisitos: el sistema indica las reglas no cumplidas. |
| Excepciones | Las dos contraseñas nuevas no coinciden: el sistema solicita reingresarlas. |
| Patrones aplicados | — |

---

### CU-07: Navegar Catálogo por Categorías

| Campo | Detalle |
|---|---|
| Identificador | CU-07 |
| Estado de implementacion | DONE - Backend expone arbol de categorias y productos por subarbol (`CatalogController`/`CatalogService`) y frontend navega categorias reales. |
| Nombre | Navegar Catálogo por Categorías |
| Actor principal | Cliente / Usuario no registrado |
| Precondiciones | Existe al menos una categoría activa con productos visibles. |
| Postcondiciones | El usuario visualiza los productos contenidos en la categoría o subcategoría seleccionada. |
| Flujo principal | 1. El usuario accede a la página principal del catálogo. 2. El sistema despliega la jerarquía de categorías y subcategorías (patrón Composite). 3. El usuario selecciona una categoría o subcategoría. 4. El sistema muestra el listado de productos activos contenidos directa o transitivamente en el nodo seleccionado. |
| Flujo alternativo | 4a. La categoría no contiene productos activos: el sistema muestra mensaje indicando que no hay resultados. |
| Excepciones | — |
| Patrones aplicados | Composite (recorrido jerárquico de categorías) |

---

### CU-08: Buscar Productos por Filtros

| Campo | Detalle |
|---|---|
| Identificador | CU-08 |
| Estado de implementacion | DONE - Backend filtra productos activos por nombre, categoria, talle, color y rango de precio; frontend tiene formulario basico de filtros. |
| Nombre | Buscar Productos por Filtros |
| Actor principal | Cliente / Usuario no registrado |
| Precondiciones | Existe al menos un producto activo en el catálogo. |
| Postcondiciones | El usuario visualiza los productos que cumplen con los criterios de búsqueda. |
| Flujo principal | 1. El usuario accede a la barra de búsqueda del catálogo. 2. Ingresa un texto libre y/o aplica filtros (categoría, talla, color, rango de precio). 3. El sistema consulta el catálogo aplicando los filtros. 4. El sistema muestra el listado de productos coincidentes con paginación. |
| Flujo alternativo | 4a. No hay coincidencias: el sistema sugiere ajustar los filtros y muestra mensaje informativo. |
| Excepciones | — |
| Patrones aplicados | — |

---

### CU-09: Ver Detalle de Producto

| Campo | Detalle |
|---|---|
| Identificador | CU-09 |
| Estado de implementacion | DONE - Backend expone detalle de producto activo con variantes/stock y frontend muestra ficha, variantes y estado sin stock. |
| Nombre | Ver Detalle de Producto |
| Actor principal | Cliente / Usuario no registrado |
| Precondiciones | El producto existe y se encuentra activo. El usuario lo seleccionó desde CU-07 o CU-08. |
| Postcondiciones | El usuario visualiza la ficha completa del producto. |
| Flujo principal | 1. El usuario selecciona un producto del listado. 2. El sistema muestra la ficha con nombre, descripción, precio, imágenes, tallas disponibles, colores disponibles, material y stock por variante. |
| Flujo alternativo | 2a. Producto sin stock en ninguna variante: el sistema lo muestra como "Sin stock" y deshabilita el agregado al carrito. |
| Excepciones | Producto desactivado entre la búsqueda y el acceso al detalle: el sistema redirige al catálogo con mensaje informativo. |
| Patrones aplicados | — |

---

### CU-10: Crear Producto

| Campo | Detalle |
|---|---|
| Identificador | CU-10 |
| Estado de implementacion | DONE - Backend permite crear productos con variantes y validaciones basicas, restringido a `ROLE_ADMINISTRADOR` en `SecurityConfig`. Frontend: `AdminProductsView` (panel admin protegido) ofrece el formulario de alta — nombre, descripción, precio, material, selector de categoría (árbol Composite aplanado), lista dinámica de imágenes y de variantes (talla/color/stock) — que postea a `POST /api/products` con el JWT (interceptor en `api/products.ts`). Valida la regla "cada variante define al menos talla o color" del lado del cliente y muestra los mensajes de error del backend; el producto creado aparece en el listado del panel y en el catálogo público. |
| Nombre | Crear Producto |
| Actor principal | Administrador |
| Precondiciones | El administrador ha iniciado sesión (vía CU-03). Existe al menos una categoría donde ubicar el producto (CU-13). |
| Postcondiciones | El producto queda creado y disponible en el catálogo público. |
| Flujo principal | 1. El administrador accede a la sección de gestión de productos. 2. Selecciona "Crear producto". 3. Ingresa nombre, descripción, precio, material, categoría/subcategoría y carga imágenes. 4. Define las variantes disponibles (combinaciones de talla y color) con su stock inicial. 5. El sistema valida los datos. 6. El sistema persiste el producto en estado activo. |
| Flujo alternativo | 5a. Precio negativo o stock inválido: el sistema rechaza la operación con mensaje de validación. 5b. Categoría inexistente: el administrador es derivado a CU-13 para crearla. |
| Excepciones | Pérdida de conexión durante el guardado: el sistema informa el error y solicita reintentar. |
| Patrones aplicados | Composite (asignación a la jerarquía de categorías) |

---

### CU-11: Editar Producto

| Campo | Detalle |
|---|---|
| Identificador | CU-11 |
| Estado de implementacion | DONE - Backend permite editar productos (`PUT /api/products/{id}`, campos opcionales, recálculo de la cadena de categorías al reasignar), restringido a `ROLE_ADMINISTRADOR` en `SecurityConfig`. Frontend: el panel admin reutiliza el mismo formulario en modo edición — el botón "Editar" de cada producto carga sus datos, preserva el `id` de las variantes existentes (las nuevas las genera el backend) y "Guardar cambios" envía el `PUT`. Incluye "Cancelar edición" y reutiliza la validación de variantes. |
| Nombre | Editar Producto |
| Actor principal | Administrador |
| Precondiciones | El administrador ha iniciado sesión (vía CU-03). El producto a editar existe en el sistema (creado previamente vía CU-10). |
| Postcondiciones | El producto refleja los cambios realizados en el catálogo. |
| Flujo principal | 1. El administrador localiza el producto en la sección de gestión. 2. Selecciona "Editar". 3. Modifica uno o más atributos: nombre, descripción, precio, material, categoría, imágenes, variantes o stock. 4. El sistema valida los datos. 5. El sistema persiste los cambios. |
| Flujo alternativo | 4a. Validación fallida (precio negativo, stock inválido, etc.): el sistema rechaza la operación con mensaje específico. |
| Excepciones | Pérdida de conexión durante el guardado: el sistema informa el error y solicita reintentar. |
| Patrones aplicados | Composite (reasignación entre categorías) |

---

### CU-12: Desactivar Producto

| Campo | Detalle |
|---|---|
| Identificador | CU-12 |
| Estado de implementacion | DONE (flujo admin) - Backend desactiva productos (`DELETE /api/products/{id}`, marca `active=false`; el producto sigue referenciable desde pedidos históricos), restringido a `ROLE_ADMINISTRADOR`. Frontend: cada producto del panel admin tiene un botón "Desactivar" que solicita confirmación explícita (`window.confirm`, flujo principal paso 3; si se cancela el producto permanece activo — 3a) y llama al `DELETE`; al desactivarlo sale del listado activo. Excepción de carritos afectados cubierta: al refrescar el carrito (`GET /api/cart` y cada mutación), `CarritoService.productosInactivos` detecta los productos desactivados y `CarritoResponse` marca esos ítems con `disponible=false`; el `CartView` los resalta, avisa al cliente y bloquea "Confirmar compra" hasta que los quite (sin borrarlos automáticamente). Cubierto por `CarritoServiceTest`. |
| Nombre | Desactivar Producto |
| Actor principal | Administrador |
| Precondiciones | El administrador ha iniciado sesión (vía CU-03). El producto existe y se encuentra activo. |
| Postcondiciones | El producto deja de ser visible en el catálogo público, pero permanece referenciable desde pedidos históricos. |
| Flujo principal | 1. El administrador localiza el producto en la sección de gestión. 2. Selecciona "Desactivar". 3. El sistema solicita confirmación. 4. El sistema marca el producto como inactivo. |
| Flujo alternativo | 3a. El administrador cancela la confirmación: el producto permanece activo. |
| Excepciones | Producto referenciado en carritos activos: el sistema desactiva el producto e informa a los clientes afectados al refrescar su carrito. |
| Patrones aplicados | — |

---

### CU-13: Gestionar Categorías y Subcategorías

| Campo | Detalle |
|---|---|
| Identificador | CU-13 |
| Estado de implementacion | DONE - Backend permite listar, crear, renombrar, mover, activar/desactivar categorias con validaciones de jerarquia (ciclos al reubicar, recalculo de `ancestorIds` del subarbol y de los productos afectados, bloqueo de baja con productos activos), restringido a `ROLE_ADMINISTRADOR` en `SecurityConfig`. Frontend: `AdminCategoriesView` muestra el arbol completo indentado (patron Composite) con alta (nombre + categoria padre), renombrado inline, reubicacion via selector de nuevo padre (la UI excluye el propio nodo y sus descendientes para evitar ciclos; el backend revalida) y activar/desactivar (la baja pide confirmacion). Surfacea los errores del backend (ciclo, productos activos asociados — flujo alternativo 6a) y cada mutacion refresca el arbol admin y el catalogo publico (`api/categories.ts`). |
| Nombre | Gestionar Categorías y Subcategorías |
| Actor principal | Administrador |
| Precondiciones | El administrador ha iniciado sesión (vía CU-03). |
| Postcondiciones | La jerarquía de categorías refleja los cambios realizados (alta, edición o desactivación). |
| Flujo principal | 1. El administrador accede a la sección de gestión de categorías. 2. Visualiza el árbol completo de categorías y subcategorías (patrón Composite). 3. Selecciona crear, editar o desactivar un nodo. 4. Al crear: ingresa nombre y categoría padre (opcional). 5. Al editar: modifica el nombre o reubica el nodo dentro del árbol. 6. Al desactivar: el sistema valida que no queden productos activos asociados. 7. El sistema persiste los cambios. |
| Flujo alternativo | 6a. Existen productos activos asociados a la categoría que se intenta desactivar: el sistema impide la desactivación y solicita reasignar o desactivar los productos primero. |
| Excepciones | Ciclo en la jerarquía al reubicar un nodo: el sistema rechaza la operación. |
| Patrones aplicados | Composite (manipulación uniforme de nodos de la jerarquía) |

---

### CU-14: Agregar Producto al Carrito

| Campo | Detalle |
|---|---|
| Identificador | CU-14 |
| Estado de implementacion | DONE - Backend agrega variantes al carrito validando stock y acumulando cantidad; frontend permite seleccionar variante/cantidad desde detalle y agregar al carrito. El carrito pertenece al cliente autenticado vía JWT (`api/cart.ts` adjunta el Bearer; se eliminó el `X-Cliente-Id` temporal). Si no hay sesión de cliente, "Agregar al carrito" redirige al login (precondición CU-02). |
| Nombre | Agregar Producto al Carrito |
| Actor principal | Cliente |
| Precondiciones | El cliente ha iniciado sesión (vía CU-02). El cliente se encuentra en la ficha del producto (vía CU-09). El producto está activo y posee stock en al menos una variante. |
| Postcondiciones | El producto, con la variante y cantidad seleccionadas, queda incorporado al carrito del cliente. El total del carrito se recalcula. |
| Flujo principal | 1. El cliente selecciona talla y color en la ficha del producto. 2. Indica la cantidad deseada (por defecto 1). 3. Selecciona "Agregar al carrito". 4. El sistema verifica el stock disponible para la variante seleccionada. 5. El sistema agrega el ítem al carrito y recalcula el total. 6. El sistema confirma el agregado. |
| Flujo alternativo | 4a. Stock insuficiente para la cantidad solicitada: el sistema informa el máximo disponible y agrega solo esa cantidad si el cliente lo acepta. 5a. El producto ya estaba en el carrito con la misma variante: el sistema suma la cantidad al ítem existente. |
| Excepciones | Sesión expirada durante el agregado: el sistema preserva la selección y redirige al login. |
| Patrones aplicados | — |

---

### CU-15: Modificar Cantidad en Carrito

| Campo | Detalle |
|---|---|
| Identificador | CU-15 |
| Estado de implementacion | DONE - Backend modifica cantidades validando stock; frontend permite incrementar/decrementar o ingresar cantidad. Si cantidad queda en cero, la UI no deriva automaticamente a eliminar sino que mantiene minimo 1. |
| Nombre | Modificar Cantidad en Carrito |
| Actor principal | Cliente |
| Precondiciones | El cliente ha iniciado sesión (vía CU-02). El carrito contiene al menos un ítem (vía CU-14). |
| Postcondiciones | La cantidad del ítem queda actualizada y el total del carrito se recalcula. |
| Flujo principal | 1. El cliente accede al carrito. 2. Selecciona el ítem a modificar. 3. Cambia la cantidad mediante los controles de incremento/decremento o ingreso directo. 4. El sistema valida la nueva cantidad contra el stock disponible. 5. El sistema actualiza el ítem y recalcula el total. |
| Flujo alternativo | 4a. Stock insuficiente: el sistema limita la cantidad al máximo disponible e informa al cliente. 3a. El cliente reduce la cantidad a cero: el sistema deriva a CU-16 (Eliminar Producto del Carrito). |
| Excepciones | Sesión expirada: el sistema preserva el carrito y redirige al login. |
| Patrones aplicados | — |

---

### CU-16: Eliminar Producto del Carrito

| Campo | Detalle |
|---|---|
| Identificador | CU-16 |
| Estado de implementacion | DONE - Backend elimina items del carrito y recalcula total; frontend tiene accion de eliminar item. |
| Nombre | Eliminar Producto del Carrito |
| Actor principal | Cliente |
| Precondiciones | El cliente ha iniciado sesión (vía CU-02). El carrito contiene al menos un ítem (vía CU-14). |
| Postcondiciones | El ítem es removido del carrito y el total se recalcula. |
| Flujo principal | 1. El cliente accede al carrito. 2. Selecciona "Eliminar" sobre un ítem. 3. El sistema solicita confirmación. 4. El sistema remueve el ítem y recalcula el total. |
| Flujo alternativo | 3a. El cliente cancela: el ítem permanece en el carrito. |
| Excepciones | Sesión expirada: el sistema preserva el carrito y redirige al login. |
| Patrones aplicados | — |

---

### CU-17: Vaciar Carrito

| Campo | Detalle |
|---|---|
| Identificador | CU-17 |
| Estado de implementacion | DONE - Backend vacia el carrito y recalcula total; frontend tiene accion de vaciar carrito. La confirmacion explicita previa no esta implementada. |
| Nombre | Vaciar Carrito |
| Actor principal | Cliente |
| Precondiciones | El cliente ha iniciado sesión (vía CU-02). El carrito contiene al menos un ítem. |
| Postcondiciones | El carrito queda sin ítems y el total se establece en cero. |
| Flujo principal | 1. El cliente accede al carrito. 2. Selecciona "Vaciar carrito". 3. El sistema solicita confirmación explícita. 4. El sistema remueve todos los ítems y recalcula el total. |
| Flujo alternativo | 3a. El cliente cancela: el carrito permanece intacto. |
| Excepciones | Sesión expirada: el sistema preserva el carrito y redirige al login. |
| Patrones aplicados | — |

---

### CU-18: Confirmar Compra

| Campo | Detalle |
|---|---|
| Identificador | CU-18 |
| Estado de implementacion | DONE - Backend crea `Pedido` desde carrito, valida stock, congela items/precios e inicia en `EstadoPendiente`; frontend confirma compra desde carrito. No vacia carrito porque eso queda para pago/fachada. |
| Nombre | Confirmar Compra |
| Actor principal | Cliente |
| Precondiciones | El cliente ha iniciado sesión (vía CU-02). El carrito contiene al menos un ítem (vía CU-14). Todos los ítems del carrito mantienen stock disponible. |
| Postcondiciones | Se crea un pedido en estado "Pendiente" asociado al carrito. El cliente avanza a la selección del método de pago (CU-19). |
| Flujo principal | 1. El cliente accede al carrito y selecciona "Confirmar compra". 2. El sistema muestra el resumen: ítems, cantidades, subtotal y total. 3. El cliente revisa y solicita los datos de envío si corresponde. 4. El cliente confirma el resumen. 5. El sistema verifica nuevamente el stock de todos los ítems. 6. El sistema crea el pedido en estado "Pendiente" (patrón State, estado inicial) y deriva al cliente al flujo de selección de método de pago. |
| Flujo alternativo | 5a. Stock insuficiente en uno o más ítems: el sistema marca los ítems afectados, ajusta las cantidades o los remueve, y solicita al cliente reconfirmar antes de continuar. |
| Excepciones | Sesión expirada: el sistema preserva el carrito y redirige al login. |
| Patrones aplicados | State (creación del pedido en estado inicial "Pendiente") |

---

### CU-19: Seleccionar Método de Pago

| Campo | Detalle |
|---|---|
| Identificador | CU-19 |
| Estado de implementacion | DONE (backend) - Strategy completo: `MetodoPagoFactory` instancia en tiempo de ejecucion la estrategia segun el metodo elegido (`PagoTarjeta` / `PagoPayPal` / `PagoTransferencia`), valida el formato de los datos via `MetodoPago.validarDatosPago()` y el metodo seleccionado queda persistido en el pedido (`metodoPagoNombre`). La seleccion + procesamiento se reciben en `POST /api/orders/{id}/payment`. Frontend: el `OrderCard` (vista `Pedidos`) muestra el pago cuando el pedido esta `Pendiente` con un selector de los tres metodos (Tarjeta / PayPal / Transferencia) que renderiza el formulario correspondiente y arma el `PaymentRequest` segun la opcion elegida. |
| Nombre | Seleccionar Método de Pago |
| Actor principal | Cliente |
| Precondiciones | Existe un pedido en estado "Pendiente" asociado al cliente, creado mediante CU-18. |
| Postcondiciones | El pedido queda asociado a un método de pago y avanza al procesamiento de pago (CU-20). |
| Flujo principal | 1. El sistema muestra los métodos de pago disponibles: Tarjeta de crédito/débito, PayPal y Transferencia bancaria. 2. El cliente selecciona uno. 3. El sistema solicita los datos requeridos según el método seleccionado. 4. El cliente ingresa los datos. 5. El sistema valida el formato de los datos sin contactar al proveedor externo. 6. El sistema instancia el strategy de pago correspondiente y avanza a CU-20. |
| Flujo alternativo | 5a. Datos con formato inválido: el sistema indica el campo a corregir y permanece en la pantalla. 2a. El cliente cancela: el pedido permanece en estado "Pendiente" y el carrito intacto. |
| Excepciones | — |
| Patrones aplicados | Strategy (selección del algoritmo de pago en tiempo de ejecución) |

---

### CU-20: Procesar Pago

| Campo | Detalle |
|---|---|
| Identificador | CU-20 |
| Estado de implementacion | DONE (backend) - `PedidoService.procesarPago` instancia el Strategy, delega en el proveedor externo via Adapters (`AdapterTarjeta` / `AdapterPayPal` / `AdapterTransferencia` sobre servicios externos simulados), revalida stock por concurrencia, descuenta el stock de las variantes, transiciona el pedido Pendiente -> Pagado (State) y vacia el carrito post-pago. Observer integrado: antes de la transicion el service suscribe al pedido los canales habilitados del cliente (`Cliente.canalesNotificacionHabilitados()` segun sus `PreferenciasNotificacion`), por lo que el pago exitoso dispara las notificaciones simuladas (Email/SMS/Push) por los canales activos. Frontend: el cliente paga desde la vista `Pedidos` (Tarjeta/PayPal/Transferencia simulados), lo que ahora genera la notificacion real (visible en los logs del backend). |
| Nombre | Procesar Pago |
| Actor principal | Cliente |
| Actores secundarios | Sistema de Pagos (externo) |
| Precondiciones | Existe un pedido en estado "Pendiente" con un método de pago seleccionado (vía CU-19). |
| Postcondiciones | Ante éxito: el pedido transiciona a estado "Pagado", se descuenta el stock y se notifica al cliente. Ante fallo: el pedido permanece en "Pendiente" sin descontar stock. |
| Flujo principal | 1. El sistema delega el procesamiento al strategy de pago correspondiente. 2. El strategy se comunica con el Sistema de Pagos externo. 3. El proveedor confirma el pago exitoso. 4. El sistema descuenta el stock de las variantes adquiridas. 5. El sistema actualiza el estado del pedido a "Pagado" (patrón State). 6. El sistema dispara la notificación al cliente a través de los canales configurados (patrón Observer). 7. El sistema vacía el carrito y muestra confirmación de la compra. |
| Flujo alternativo | 3a. Pago rechazado por el proveedor: el sistema mantiene el pedido en "Pendiente", informa al cliente y le permite reintentar con otro método (volver a CU-19). 3b. Timeout en la comunicación con el proveedor: el sistema informa el error y reintenta consulta de estado antes de revertir. |
| Excepciones | Producto sin stock al momento del pago (concurrencia): el sistema cancela la transacción de pago si fue iniciada, mantiene el pedido en "Pendiente" e informa al cliente. |
| Patrones aplicados | Strategy (ejecución del método de pago), State (transición Pendiente → Pagado), Observer (notificación post-pago) |

---

### CU-21: Consultar Historial de Pedidos

| Campo | Detalle |
|---|---|
| Identificador | CU-21 |
| Estado de implementacion | DONE - Backend lista pedidos por cliente ordenados por fecha descendente y frontend muestra historial en vista `Pedidos`, resolviendo la identidad desde el JWT (`api/orders.ts` adjunta el Bearer; se eliminó el `X-Cliente-Id` temporal). El acceso a "Pedidos" exige sesión de cliente. |
| Nombre | Consultar Historial de Pedidos |
| Actor principal | Cliente |
| Precondiciones | El cliente ha iniciado sesión (vía CU-02). |
| Postcondiciones | El cliente visualiza el listado de sus pedidos con el estado actual de cada uno. |
| Flujo principal | 1. El cliente accede a "Mis pedidos" desde su panel. 2. El sistema muestra el listado de pedidos del cliente ordenados por fecha descendente, indicando número, fecha, total y estado actual de cada uno. |
| Flujo alternativo | 2a. Sin pedidos registrados: el sistema muestra un mensaje indicando que no hay compras realizadas. |
| Excepciones | — |
| Patrones aplicados | — |

---

### CU-22: Consultar Detalle de Pedido

| Campo | Detalle |
|---|---|
| Identificador | CU-22 |
| Estado de implementacion | DONE - Backend expone el detalle completo (items, variantes, cantidades, precios unitarios, total, metodo de pago utilizado via `metodoPagoNombre` e historial de transiciones de estado) y rechaza pedidos de otro cliente. Frontend muestra el detalle embebido en cada tarjeta del historial. |
| Nombre | Consultar Detalle de Pedido |
| Actor principal | Cliente |
| Precondiciones | El cliente ha iniciado sesión (vía CU-02). El cliente seleccionó un pedido desde el historial (vía CU-21). El pedido pertenece al cliente. |
| Postcondiciones | El cliente visualiza la información completa del pedido seleccionado. |
| Flujo principal | 1. El cliente selecciona un pedido del historial. 2. El sistema muestra el detalle: productos, variantes (talla y color), cantidades, precios unitarios, total, método de pago utilizado y estado actual con su historial de transiciones. |
| Flujo alternativo | — |
| Excepciones | Pedido no pertenece al cliente que consulta: el sistema rechaza el acceso. |
| Patrones aplicados | — |

---

### CU-23: Avanzar Estado de Pedido

| Campo | Detalle |
|---|---|
| Identificador | CU-23 |
| Estado de implementacion | DONE - Backend implementa State y `POST /api/orders/{id}/advance`, restringido a `ROLE_ADMINISTRADOR` en `SecurityConfig`. Observer integrado: `PedidoService.avanzarEstado` suscribe los canales habilitados del cliente al pedido antes de cada transicion, y `Pedido.avanzarEstado()` dispara `notificar()`; ademas `Pedido.notificar()` aisla el fallo de un canal (lo registra y continua, sin revertir el estado — flujo alternativo 6a). Cubierto por `PedidoServiceTest` (incluye `listarTodos`) y `ObserverTest`. Frontend: `AdminOrdersView` (panel admin → "Pedidos") lista todos los pedidos via `GET /api/orders/admin` (nuevo endpoint solo-admin) y muestra el boton "Avanzar a {siguiente}" segun el estado (Pagado→Enviado, Enviado→Entregado; Pendiente espera el pago, Entregado es terminal — patron State). Al avanzar, el backend notifica al cliente por sus canales (Observer) y la vista refleja el nuevo estado. Los controles de transicion se quitaron del `OrderCard` del cliente (el cliente solo paga y carga su direccion via `PATCH`); asi cada transicion queda donde corresponde por rol. |
| Nombre | Avanzar Estado de Pedido |
| Actor principal | Administrador |
| Precondiciones | El administrador ha iniciado sesión (vía CU-03). Existe al menos un pedido en estado distinto de "Entregado". |
| Postcondiciones | El pedido avanza al siguiente estado según el ciclo definido. El cliente recibe una notificación por los canales configurados. |
| Flujo principal | 1. El administrador accede al panel de gestión de pedidos. 2. Filtra y selecciona un pedido. 3. El sistema muestra el estado actual y las transiciones disponibles según el estado (patrón State). 4. El administrador selecciona la transición (Pagado → Enviado o Enviado → Entregado). 5. El sistema actualiza el estado del pedido. 6. El sistema notifica al cliente mediante los canales habilitados (patrón Observer). |
| Flujo alternativo | 3a. El pedido está en estado "Entregado" (final): no se muestran transiciones disponibles. 6a. Fallo en un canal de notificación: el sistema registra el error, reintenta, y el estado del pedido no se revierte. |
| Excepciones | Pedido no encontrado: el sistema muestra mensaje de error al administrador. |
| Patrones aplicados | State (transiciones controladas por el estado actual), Observer (notificación a los suscriptores) |

---

### CU-24: Configurar Canales de Notificación

| Campo | Detalle |
|---|---|
| Identificador | CU-24 |
| Estado de implementacion | DONE - Observer completo e integrado: `Pedido` implementa `SujetoObservable`; `Cliente` embebe `PreferenciasNotificacion` (default todos activos) y expone `canalesNotificacionHabilitados()` que arma `CanalEmail`/`CanalSMS`/`CanalPush` segun las preferencias (Email con el contacto real; SMS/Push con destino simulado derivado de la cuenta — RF-25). Backend: `NotificationService` + `GET`/`PUT /api/notifications/preferences` (solo Cliente autenticado) persisten las preferencias; `PedidoService` deriva la suscripcion de esas preferencias al notificar (ver CU-20/CU-23). El backend permite desactivar los tres canales. Frontend: `NotificationsView` (Configuración → Notificaciones) muestra los canales como checkboxes, los guarda vía `PUT` y, si el cliente apaga todos, advierte y pide confirmación (flujo alternativo 3a). Cubierto por `NotificationServiceTest` y `ClienteTest`. |
| Nombre | Configurar Canales de Notificación |
| Actor principal | Cliente |
| Precondiciones | El cliente ha iniciado sesión (vía CU-02). |
| Postcondiciones | Las preferencias de notificación quedan actualizadas y se aplican a futuros eventos generados por el sistema. |
| Flujo principal | 1. El cliente accede a la sección de configuración de su cuenta. 2. Visualiza los canales disponibles: Email, SMS y Push. 3. Activa o desactiva cada canal según su preferencia. 4. El sistema persiste la configuración y actualiza la suscripción del cliente como observador (patrón Observer). |
| Flujo alternativo | 3a. El cliente desactiva todos los canales: el sistema advierte que no recibirá notificaciones sobre sus pedidos y solicita confirmación. |
| Excepciones | — |
| Patrones aplicados | Observer (suscripción y desuscripción dinámica de canales) |

---

## 7. Relaciones entre Casos de Uso

El siguiente esquema resume las dependencias e inclusiones más relevantes entre los casos de uso del sistema.

### 7.1. Precondiciones de autenticación

- **CU-01** (Registrarse como Cliente) es precondición de **CU-02**.
- **CU-02** (Iniciar Sesión como Cliente) es precondición de: CU-04, CU-06, CU-14, CU-15, CU-16, CU-17, CU-18, CU-19, CU-20, CU-21, CU-22 y CU-24.
- **CU-03** (Iniciar Sesión como Administrador) es precondición de: CU-04, CU-06, CU-10, CU-11, CU-12, CU-13 y CU-23.

### 7.2. Inclusiones (`<<include>>`)

- **CU-18** (Confirmar Compra) incluye **CU-19** (Seleccionar Método de Pago).
- **CU-19** (Seleccionar Método de Pago) incluye **CU-20** (Procesar Pago).
- **CU-22** (Consultar Detalle de Pedido) incluye **CU-21** (Consultar Historial de Pedidos) como precondición de navegación.
- **CU-10** (Crear Producto) incluye **CU-13** (Gestionar Categorías y Subcategorías) cuando se requiere crear una categoría inexistente.

### 7.3. Extensiones (`<<extend>>`)

- **CU-20** (Procesar Pago) extiende a **CU-24** al disparar notificaciones automáticas tras el pago exitoso.
- **CU-23** (Avanzar Estado de Pedido) extiende a **CU-24** al disparar notificaciones ante cada cambio de estado.
- **CU-15** (Modificar Cantidad en Carrito) extiende a **CU-16** cuando la cantidad indicada es cero.

### 7.4. Casos de uso por rol

| Rol | Casos de uso accesibles |
|---|---|
| Usuario no registrado | CU-01, CU-02, CU-03, CU-05, CU-07, CU-08, CU-09 |
| Cliente | CU-02, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-14, CU-15, CU-16, CU-17, CU-18, CU-19, CU-20, CU-21, CU-22, CU-24 |
| Administrador | CU-03, CU-04, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-23 |

---
