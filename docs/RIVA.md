# TRABAJO PRÁCTICO OBLIGATORIO
## Proceso de Desarrollo de Software
## RIVA
### Plataforma de E-Commerce — Marca de Indumentaria y Moda
### Entregable 1: Análisis Funcional y Casos de Uso

---

## 1. Introducción

RIVA es una plataforma de comercio electrónico orientada a la venta de indumentaria y accesorios de moda. El sistema permite a los usuarios explorar un catálogo organizado por categorías, gestionar un carrito de compras, completar transacciones mediante múltiples métodos de pago y hacer seguimiento del estado de sus pedidos en tiempo real.

### 1.1. Contexto del Sistema

La empresa RIVA busca posicionarse en el mercado de indumentaria y moda ofreciendo una experiencia de compra ágil y confiable. El sistema contempla dos perfiles de usuario principales: el Cliente, quien navega, compra y recibe notificaciones; y el Administrador, quien gestiona el catálogo y los pedidos.

### 1.2. Alcance del Sistema

El sistema cubre los siguientes módulos funcionales:

- Registro y autenticación de usuarios con roles diferenciados.
- Catálogo de indumentaria y accesorios organizado jerárquicamente.
- Carrito de compras con cálculo dinámico de totales.
- Proceso de compra con selección de método de pago.
- Gestión del ciclo de vida de pedidos.
- Sistema de notificaciones por múltiples canales.

---

## 2. Descripción del Dominio

El dominio elegido para la plataforma es la venta de indumentaria y accesorios de moda de la marca RIVA. Se optó por este rubro dado que presenta una estructura de categorías y subcategorías rica, atributos de producto variados (talla, color, material) y una base de usuarios acostumbrada a la compra online.

### 2.1. Categorías de Productos

El catálogo se organiza en tres categorías principales por género, cada una con sus subcategorías:

| Hombres | Mujeres | Niños |
|---|---|---|
| Camisetas, Camisas, Pantalones, Chaquetas, Hoodies, Ropa deportiva, Accesorios | Vestidos, Tops, Pantalones, Faldas, Chaquetas, Ropa deportiva, Accesorios | Camisetas, Pantalones, Conjuntos, Ropa deportiva, Accesorios |

### 2.2. Atributos de Productos

Cada producto cuenta con los siguientes atributos base:

- Nombre, descripción y marca (RIVA).
- Precio y stock disponible por variante.
- Categoría y subcategoría (estructura jerárquica Composite).
- Talla (XS, S, M, L, XL, XXL) y color disponibles.
- Material y composición textil.
- Imágenes de múltiples ángulos.
- Estado: activo / inactivo.

---

## 3. Actores del Sistema

Se identifican los siguientes actores principales:

| Actor | Descripción | Acciones principales |
|---|---|---|
| Cliente | Usuario registrado que realiza compras en la plataforma. | Navegar catálogo, gestionar carrito, comprar, consultar pedidos, configurar notificaciones. |
| Administrador | Usuario con privilegios elevados que gestiona la plataforma, provisionado internamente. | Gestionar productos, gestionar categorías, avanzar estados de pedidos. |
| Sistema de Pagos | Actor externo que procesa las transacciones financieras. | Procesar pagos con tarjeta, PayPal, transferencia bancaria. |
| Sistema de Notificaciones | Actor externo que envía alertas al cliente. | Enviar notificaciones por email, SMS o push. |

---

## 4. Requerimientos Funcionales

### 4.1. Módulo de Usuarios

- **RF-01:** El sistema debe permitir el registro de nuevos usuarios con rol Cliente indicando nombre, apellido, email y contraseña. Las cuentas con rol Administrador son provisionadas internamente.
- **RF-02:** El sistema debe validar credenciales en el inicio de sesión y retornar un mensaje de error genérico ante datos incorrectos, sin revelar cuál campo falló.
- **RF-03:** Cada rol debe habilitar un conjunto diferenciado de funcionalidades dentro de la aplicación, con pantallas de login separadas para Cliente y Administrador.
- **RF-04:** El sistema debe permitir al usuario recuperar su contraseña mediante un enlace enviado al email registrado con token de expiración.
- **RF-05:** El sistema debe permitir al usuario autenticado cambiar su contraseña validando previamente la contraseña actual.
- **RF-06:** El sistema debe permitir al usuario autenticado cerrar su sesión, invalidando el token activo.

### 4.2. Módulo de Catálogo

- **RF-07:** El catálogo debe organizarse en categorías y subcategorías usando una estructura jerárquica (patrón Composite).
- **RF-08:** Cada producto debe mostrar nombre, precio, descripción, imágenes, material y la lista de variantes disponibles (combinaciones de talla y color con su stock).
- **RF-09:** Los clientes deben poder buscar productos por nombre, categoría, talla, color y rango de precio.
- **RF-10:** El administrador debe poder crear, editar y desactivar productos del catálogo, incluyendo la gestión de variantes (tallas y colores) y su stock.
- **RF-11:** El administrador debe poder crear, editar, reubicar y desactivar categorías y subcategorías dentro de la jerarquía, sin permitir ciclos ni desactivar nodos con productos activos asociados.

### 4.3. Módulo de Carrito

- **RF-12:** Los clientes deben poder agregar uno o más productos al carrito desde la ficha de producto, seleccionando talla, color y cantidad.
- **RF-13:** El sistema debe validar la disponibilidad de stock para la variante seleccionada al momento de agregar o modificar la cantidad de un ítem.
- **RF-14:** Los clientes deben poder modificar la cantidad de ítems, eliminar productos individuales o vaciar el carrito completo.
- **RF-15:** El total del carrito debe recalcularse dinámicamente ante cualquier cambio.

### 4.4. Módulo de Pago

- **RF-16:** El sistema debe ofrecer al menos tres métodos de pago: tarjeta de crédito/débito, PayPal y transferencia bancaria.
- **RF-17:** La selección del método de pago debe poder realizarse en tiempo de ejecución (patrón Strategy), permitiendo incorporar nuevos métodos sin modificar el código existente.
- **RF-18:** Ante un pago exitoso, el sistema debe reducir el stock de las variantes adquiridas y transicionar el pedido a estado "Pagado".
- **RF-19:** Ante un fallo en el pago, el sistema debe notificar al usuario, mantener el pedido en estado "Pendiente" sin descontar stock y permitir reintentar con otro método.

### 4.5. Módulo de Pedidos

- **RF-20:** Al confirmar la compra, se debe generar un pedido con estado inicial "Pendiente" asociado al cliente.
- **RF-21:** El ciclo de estados del pedido es: Pendiente → Pagado → Enviado → Entregado (patrón State), con transiciones controladas por el estado actual.
- **RF-22:** Solo el Administrador puede avanzar el estado de un pedido hacia "Enviado" o "Entregado".
- **RF-23:** El cliente debe poder consultar el historial de sus pedidos con su estado actual y acceder al detalle de cada uno.

### 4.6. Módulo de Notificaciones

- **RF-24:** El sistema debe notificar al cliente ante cada cambio de estado de su pedido (patrón Observer).
- **RF-25:** Los canales de notificación disponibles son: email, SMS y push (simulados).
- **RF-26:** El cliente debe poder configurar qué canales de notificación desea recibir, suscribiéndose o desuscribiéndose dinámicamente.

---

## 5. Requerimientos No Funcionales

| ID | Categoría | Descripción |
|---|---|---|
| RNF-01 | Seguridad | Las contraseñas deben almacenarse con hash (BCrypt). Las sesiones deben expirar por inactividad. |
| RNF-02 | Usabilidad | La interfaz debe permitir completar una compra en no más de 5 pasos. |
| RNF-03 | Mantenibilidad | El código debe seguir principios SOLID y GRASP, con baja acoplación y alta cohesión. |
| RNF-04 | Extensibilidad | Debe ser posible agregar nuevos métodos de pago o canales de notificación sin modificar código existente. |
| RNF-05 | Disponibilidad | El sistema debe estar disponible el 99% del tiempo en horario comercial. |

---

## 6. Casos de Uso

La especificación completa de casos de uso y sus relaciones se encuentra en [USE-CASES.md](USE-CASES.md).

---

## 7. Glosario

| Término | Definición |
|---|---|
| Carrito | Contenedor temporal que almacena los productos seleccionados por el cliente antes de confirmar la compra. |
| Categoría | Agrupación jerárquica de productos (puede contener subcategorías y productos hoja). |
| Estado de Pedido | Fase en el ciclo de vida de un pedido: Pendiente, Pagado, Enviado o Entregado. |
| Método de Pago | Mecanismo de pago seleccionado por el cliente: tarjeta de crédito/débito, PayPal o transferencia bancaria. |
| Notificación | Alerta enviada al cliente informando cambios en el estado de sus pedidos, a través de email, SMS o push. |
| Pedido | Registro generado al confirmar una compra, que agrupa los productos adquiridos y su información asociada. |
| Rol | Conjunto de permisos asignado a un usuario: Cliente o Administrador. |
| Stock | Cantidad de unidades disponibles de una variante específica de producto en el inventario del sistema. |
| Variante | Combinación específica de talla y color de un producto, con su propio stock asociado. |
| Sesión | Estado autenticado del usuario en la aplicación, representado por un token con expiración. |
| Strategy | Patrón de diseño que permite seleccionar un algoritmo (método de pago) en tiempo de ejecución. |
| Observer | Patrón de diseño que permite notificar automáticamente a múltiples suscriptores ante un evento. |
| State | Patrón de diseño que encapsula los estados y transiciones del ciclo de vida de un pedido. |
| Composite | Patrón de diseño que permite tratar categorías y subcategorías de forma uniforme en una jerarquía. |
