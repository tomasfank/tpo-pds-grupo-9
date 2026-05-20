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
| Cliente | Usuario registrado que realiza compras en la plataforma. | Navegar catálogo, gestionar carrito, comprar, consultar pedidos. |
| Administrador | Usuario con privilegios elevados que gestiona la plataforma. | Gestionar productos, actualizar estados de pedidos, administrar usuarios. |
| Sistema de Pagos | Actor externo que procesa las transacciones financieras. | Procesar pagos con tarjeta, PayPal, transferencia bancaria. |
| Sistema de Notificaciones | Actor externo que envía alertas al cliente. | Enviar notificaciones por email, SMS o push. |

---

## 4. Requerimientos Funcionales

### 4.1. Módulo de Usuarios

- **RF-01:** El sistema debe permitir el registro de nuevos usuarios indicando nombre, email, contraseña y rol (Cliente o Administrador).
- **RF-02:** El sistema debe validar credenciales en el inicio de sesión y retornar un mensaje de error ante datos incorrectos.
- **RF-03:** Cada rol debe habilitar un conjunto diferenciado de funcionalidades dentro de la aplicación.
- **RF-04:** El sistema debe permitir al usuario recuperar su contraseña mediante el email registrado.

### 4.2. Módulo de Catálogo

- **RF-05:** El catálogo debe organizarse en categorías y subcategorías usando una estructura jerárquica (patrón Composite).
- **RF-06:** Cada producto debe mostrar nombre, precio, stock, descripción, imágenes, tallas disponibles, colores y composición textil.
- **RF-07:** Los clientes deben poder buscar productos por nombre, categoría, talla, color y rango de precio.
- **RF-08:** El administrador debe poder crear, editar y desactivar productos del catálogo, incluyendo la gestión de tallas y colores disponibles.

### 4.3. Módulo de Carrito

- **RF-09:** Los clientes deben poder agregar uno o más productos al carrito desde la ficha de producto.
- **RF-10:** El sistema debe validar la disponibilidad de stock para la talla y color seleccionados al momento de agregar un producto.
- **RF-11:** Los clientes deben poder modificar la cantidad de ítems o eliminar productos del carrito.
- **RF-12:** El total del carrito debe recalcularse dinámicamente ante cualquier cambio.

### 4.4. Módulo de Pago

- **RF-13:** El sistema debe ofrecer al menos tres métodos de pago: tarjeta de crédito/débito, PayPal y transferencia bancaria.
- **RF-14:** La selección del método de pago debe poder realizarse en tiempo de ejecución (patrón Strategy).
- **RF-15:** Ante un pago exitoso, el sistema debe reducir el stock de los productos adquiridos.
- **RF-16:** Ante un fallo en el pago, el sistema debe notificar al usuario y no generar pedido.

### 4.5. Módulo de Pedidos

- **RF-17:** Al confirmar la compra, se debe generar un pedido con estado inicial "Pendiente".
- **RF-18:** El ciclo de estados del pedido es: Pendiente → Pagado → Enviado → Entregado (patrón State).
- **RF-19:** Solo el Administrador puede avanzar el estado de un pedido.
- **RF-20:** El cliente debe poder consultar el historial de sus pedidos con su estado actual.

### 4.6. Módulo de Notificaciones

- **RF-21:** El sistema debe notificar al cliente ante cada cambio de estado de su pedido (patrón Observer).
- **RF-22:** Los canales de notificación disponibles son: email, SMS y push (simulados).
- **RF-23:** El cliente debe poder configurar qué canales de notificación desea recibir.

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

## 6. Especificación de Casos de Uso

A continuación se detallan los casos de uso principales del sistema, organizados por módulo funcional.

---

### CU-01: Registrar Usuario

| Campo | Detalle |
|---|---|
| Identificador | CU-01 |
| Nombre | Registrar Usuario |
| Actor principal | Usuario no registrado |
| Precondiciones | El usuario no posee cuenta en el sistema. |
| Postcondiciones | El usuario queda registrado con el rol seleccionado y puede iniciar sesión. |
| Flujo principal | 1. El usuario accede a la pantalla de registro. 2. Ingresa nombre, apellido, email, contraseña y selecciona rol (Cliente o Administrador). 3. El sistema valida que el email no esté registrado previamente. 4. El sistema hashea la contraseña y persiste el nuevo usuario. 5. El sistema muestra confirmación de registro exitoso. |
| Flujo alternativo | 3a. Si el email ya existe: el sistema informa el error y solicita un email diferente. |
| Excepciones | Campos obligatorios vacíos: el sistema muestra mensajes de validación por campo. |
| Patrones aplicados | — |

---

### CU-02: Iniciar Sesión

| Campo | Detalle |
|---|---|
| Identificador | CU-02 |
| Nombre | Iniciar Sesión |
| Actor principal | Cliente / Administrador |
| Precondiciones | El usuario posee una cuenta registrada en el sistema. |
| Postcondiciones | El usuario accede a su panel de acuerdo a su rol. |
| Flujo principal | 1. El usuario ingresa email y contraseña en la pantalla de login. 2. El sistema valida las credenciales contra el repositorio de usuarios. 3. El sistema establece la sesión y redirige al panel correspondiente al rol. |
| Flujo alternativo | 2a. Credenciales incorrectas: el sistema muestra error genérico ("Email o contraseña incorrectos") sin indicar cuál de los dos falló. 2b. Cuenta inexistente: mismo tratamiento que 2a. |
| Excepciones | Tres intentos fallidos consecutivos: el sistema bloquea temporalmente el acceso. |
| Patrones aplicados | — |

---

### CU-03: Gestionar Carrito de Compras

| Campo | Detalle |
|---|---|
| Identificador | CU-03 |
| Nombre | Gestionar Carrito de Compras |
| Actor principal | Cliente |
| Precondiciones | El cliente ha iniciado sesión. |
| Postcondiciones | El carrito refleja los productos seleccionados con el total actualizado. |
| Flujo principal | 1. El cliente navega el catálogo y selecciona un producto. 2. El sistema verifica disponibilidad de stock. 3. El producto se agrega al carrito con cantidad 1. 4. El cliente puede modificar la cantidad o eliminar el ítem. 5. El sistema recalcula el total dinámicamente en cada cambio. |
| Flujo alternativo | 2a. Stock insuficiente: el sistema informa la cantidad máxima disponible y no agrega el ítem. 4a. El cliente vacía el carrito: el sistema solicita confirmación antes de proceder. |
| Excepciones | Sesión expirada: el sistema guarda el carrito y redirige al login. |
| Patrones aplicados | — |

---

### CU-04: Confirmar Compra

| Campo | Detalle |
|---|---|
| Identificador | CU-04 |
| Nombre | Confirmar Compra |
| Actor principal | Cliente |
| Precondiciones | El cliente tiene al menos un producto en el carrito y ha iniciado sesión. |
| Postcondiciones | Se genera un pedido con estado "Pendiente" y se descuenta el stock de los productos. |
| Flujo principal | 1. El cliente revisa el resumen del carrito y hace clic en "Confirmar compra". 2. El sistema muestra las opciones de método de pago: Tarjeta, PayPal o Transferencia. 3. El cliente selecciona el método y completa los datos requeridos. 4. El sistema delega el procesamiento al strategy de pago correspondiente. 5. El proveedor externo confirma el pago. 6. El sistema descuenta el stock, genera el pedido con estado "Pagado" y notifica al cliente. |
| Flujo alternativo | 5a. El pago es rechazado: el sistema notifica al cliente y no genera pedido ni descuenta stock. 5b. Timeout de pago: el sistema informa el error y mantiene el carrito intacto. |
| Excepciones | Producto sin stock al momento del pago: se cancela la transacción y se informa al cliente. |
| Patrones aplicados | Strategy (métodos de pago), Observer (notificación post-compra), State (estado inicial del pedido) |

---

### CU-05: Cambiar Estado de Pedido

| Campo | Detalle |
|---|---|
| Identificador | CU-05 |
| Nombre | Cambiar Estado de Pedido |
| Actor principal | Administrador |
| Precondiciones | Existe al menos un pedido en el sistema. El administrador ha iniciado sesión. |
| Postcondiciones | El pedido avanza al siguiente estado. El cliente recibe una notificación. |
| Flujo principal | 1. El administrador accede al panel de gestión de pedidos. 2. Selecciona un pedido y visualiza su estado actual. 3. El sistema muestra las transiciones disponibles según el estado (patrón State). 4. El administrador selecciona la nueva transición (ej. Pagado → Enviado). 5. El sistema actualiza el estado del pedido. 6. El sistema notifica al cliente a través de los canales configurados (patrón Observer). |
| Flujo alternativo | 3a. El pedido está en estado "Entregado" (final): no se muestran transiciones disponibles. 6a. Fallo en canal de notificación: el sistema registra el error y reintenta; el estado del pedido no se revierte. |
| Excepciones | Pedido no encontrado: el sistema muestra mensaje de error al administrador. |
| Patrones aplicados | State (transiciones de estado), Observer (notificaciones) |

---

### CU-06: Gestionar Catálogo de Productos

| Campo | Detalle |
|---|---|
| Identificador | CU-06 |
| Nombre | Gestionar Catálogo de Productos |
| Actor principal | Administrador |
| Precondiciones | El administrador ha iniciado sesión. |
| Postcondiciones | El catálogo refleja los cambios realizados (nuevo producto, edición o desactivación). |
| Flujo principal | 1. El administrador accede a la sección de gestión de productos. 2. Puede crear un nuevo producto: ingresa nombre, precio, stock por talla/color, categoría, subcategoría, material y descripción. 3. Puede editar un producto existente: modifica uno o más atributos, incluyendo tallas y colores disponibles. 4. Puede desactivar un producto: el ítem deja de ser visible en el catálogo público. 5. El sistema valida los datos y persiste los cambios. |
| Flujo alternativo | 2a. Categoría inexistente: el administrador puede crear una nueva categoría o subcategoría en el momento. 5a. Precio negativo o stock inválido: el sistema rechaza la operación con mensaje de validación. |
| Excepciones | Pérdida de conexión durante el guardado: el sistema informa el error y solicita reintentar. |
| Patrones aplicados | Composite (jerarquía de categorías) |

---

### CU-07: Consultar Historial de Pedidos

| Campo | Detalle |
|---|---|
| Identificador | CU-07 |
| Nombre | Consultar Historial de Pedidos |
| Actor principal | Cliente |
| Precondiciones | El cliente ha iniciado sesión y posee al menos un pedido registrado. |
| Postcondiciones | El cliente visualiza el listado de sus pedidos con detalle de cada uno. |
| Flujo principal | 1. El cliente accede a "Mis pedidos" desde su panel. 2. El sistema muestra el listado de pedidos ordenados por fecha descendente. 3. El cliente selecciona un pedido para ver su detalle: productos, cantidades, total, método de pago y estado actual. |
| Flujo alternativo | Sin pedidos registrados: el sistema muestra un mensaje indicando que no hay compras realizadas. |
| Excepciones | — |
| Patrones aplicados | — |

---

### CU-08: Configurar Notificaciones

| Campo | Detalle |
|---|---|
| Identificador | CU-08 |
| Nombre | Configurar Notificaciones |
| Actor principal | Cliente |
| Precondiciones | El cliente ha iniciado sesión. |
| Postcondiciones | Las preferencias de notificación del cliente quedan actualizadas. |
| Flujo principal | 1. El cliente accede a la sección de configuración de su cuenta. 2. Visualiza los canales disponibles: Email, SMS y Push. 3. Activa o desactiva cada canal según su preferencia. 4. El sistema persiste la configuración y la aplica a futuros eventos. |
| Flujo alternativo | El cliente desactiva todos los canales: el sistema advierte que no recibirá notificaciones sobre sus pedidos. |
| Excepciones | — |
| Patrones aplicados | Observer (suscripción/desuscripción de canales) |

---

## 7. Relaciones entre Casos de Uso

El siguiente esquema muestra las relaciones de inclusión y extensión entre los casos de uso del sistema:

- **CU-04** (Confirmar Compra) incluye **CU-03** (Gestionar Carrito) como precondición.
- **CU-04** extiende a **CU-08** al disparar notificaciones post-compra.
- **CU-05** (Cambiar Estado) extiende a **CU-08** al disparar notificaciones al cliente.
- **CU-02** (Iniciar Sesión) es precondición de CU-03, CU-04, CU-05, CU-06 y CU-07.
- **CU-01** (Registrar Usuario) es precondición de CU-02.

Los casos de uso CU-05 y CU-06 son exclusivos del rol **Administrador**. Los casos CU-03, CU-04, CU-07 y CU-08 son exclusivos del rol **Cliente**. CU-01 y CU-02 son accesibles a cualquier usuario no autenticado.

---

## 8. Glosario

| Término | Definición |
|---|---|
| Carrito | Contenedor temporal que almacena los productos seleccionados por el cliente antes de confirmar la compra. |
| Categoría | Agrupación jerárquica de productos (puede contener subcategorías y productos hoja). |
| Estado de Pedido | Fase en el ciclo de vida de un pedido: Pendiente, Pagado, Enviado o Entregado. |
| Método de Pago | Mecanismo de pago seleccionado por el cliente: tarjeta de crédito/débito, PayPal o transferencia bancaria. |
| Notificación | Alerta enviada al cliente informando cambios en el estado de sus pedidos, a través de email, SMS o push. |
| Pedido | Registro generado al confirmar una compra, que agrupa los productos adquiridos y su información asociada. |
| Rol | Conjunto de permisos asignado a un usuario: Cliente o Administrador. |
| Stock | Cantidad de unidades disponibles de un producto en el inventario del sistema. |
| Strategy | Patrón de diseño que permite seleccionar un algoritmo (método de pago) en tiempo de ejecución. |
| Observer | Patrón de diseño que permite notificar automáticamente a múltiples suscriptores ante un evento. |
| State | Patrón de diseño que encapsula los estados y transiciones del ciclo de vida de un pedido. |
| Composite | Patrón de diseño que permite tratar categorías y subcategorías de forma uniforme en una jerarquía. |
