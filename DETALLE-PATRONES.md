# Detalle de los Patrones

## Strategy + Adapter

**Strategy** aparece en el módulo de pagos. Pedido no procesa el pago directamete, sino que delega esa responsabilidad en la interfaz MetodoPago. Las clases PagoTarjeta, PagoPayPal y PagoTransferencia implementan distintas formas de pagar. Esto permite cambiar el método de pago en tiempo de ejecución sin modificar la clase Pedido.

El **Adapter** complementa a Strategy porque cada método de pago puede necesitar comunicarse con una API externa distinta. Por ejemplo, PagoTarjeta usa AdapterTarjeta, PagoPayPal usa AdapterPaypal y PagoTransferencia usa AdapterTransferencia. Cada adapter traduce la forma particular de trabajar del proveedor externo a una interfaz común: AdapterPagoExterno, que devuelve un ResultadoPago. Así, el sistema no queda acoplado directamente a las APIs externas.

[screenshot-strategy-adapter.png]

## Observer

**Observer** aparece en el sistema de notificaciones de pedidos. Pedido actúa como sujeto observable, manteniendo una lista de CanalNotificacion. Los canales concretos, como CanalEmail, CanalSMS y CanalPush, son observadores.

Cuando cambia el estado de un pedido, el pedido ejecuta notificar(), y cada canal recibe la actualización mediante actualizar(pedido). Esto permite avisar al cliente por distintos medios sin que Pedido tenga que conocer los detalles de cada canal.

[screenshot-observer.png]

## State

**State** se usa para representar el ciclo de vida de un pedido. Pedido tiene un atributo EstadoPedido, y los estados concretos son EstadoPendiente, EstadoPagado, EstadoEnviado y EstadoEntregado.

Cada estado sabe si puede avanzar y cómo hacerlo. Por ejemplo, un pedido pendiente puede pasar a pagado, uno pagado puede pasar, y uno entregado ya no debería avanzar. Esto evita llenar Pedido de condicionales y encapsula las reglas de transición dentro de cada clase de estado.

[screenshot-state.png]

## Singleton

**Singleton** aparece en Configuracion. Esta clase centraliza parámetros generales del ecommerce, como moneda, tasaIVA, costoEnvio y umbralEnvioGratis.

La idea es que exista una única instancia de configuración accesible desde el sistema mediante obtenerInstancia(). Esto permite que distintas partes del ecommerce consulten los mismos valores globales sin crear múltiples objetos de configuración con datos inconsistentes. En el diagrama actual está modelado como Singleton, aunque convendría conectarlo con clases como Pedido, Carrito o TiendaFacade para mostrar claramente quíen lo utiliza.

[screenshot-singleton.png]

## Composite

**Composite** se usa para modelar el catálogo de productos como una estructura jerárquica.

La clase base es `ComponenteCatalogo`, que define la operación `obtenerProductos()`. A partir de esa abstracción aparecen dos tipos de elementos: `Categoria` y `Producto`.

`Categoria` funciona como el componente compuesto, porque contiene una lista de hijos del tipo `ComponenteCatalogo`, que pueden ser otras categorías o productos directamente. Por ejemplo, una categoría como "Hombre" contiene subcategorías como "Camisetas" o "Pantalones", cada una de las cuales agrupa los `Producto` correspondientes.

`Producto`, en cambio, funciona como hoja del árbol. No contiene otros elementos, sino que representa un producto final del catálogo. 

La ventaja del patrón es que el sistema puede tratar de forma uniforme a categorías y productos. Si llama a `obtenerProductos()` sobre una categoría, esta puede recorrer sus hijos y devolver todos los productos contenidos directa o indirectamente. Si se llama sobre un producto, simplemente devuelve ese producto. Esto permite manejar categorías, subcategorías y productos sin lógica especial para cada caso.

[screenshot-composite.png]

## Facade

Funciona como un **punto de entrada simplificado** para operar con varios subsistemas del sistema, como catálogo, carrito, pedidos, pagos y notificaciones.

En lugar de que una parte externa del sistema tenga que interactuar directamente con muchas clases distintas, puede usar métodos más generales de TiendaFacade, como buscarProductos(), agregarAlCarrito(), confirmarCompra(), consultarPedidos() o avanzarEstadoPedido().

Por ejemplo, confirmar una compra puede involucrar varias acciones internas: revisar el carrito, crear un pedido, procesar el pago y disparar notificaciones. Con Facade, esa complejidad queda encapsulada detrás de un método como confirmarCompra(). El cliente de la fachada no necesita conocer todos los objetos internos ni el orden exacto en que deben usarse.

[screenshot-facade.png]