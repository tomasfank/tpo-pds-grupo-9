# Detalle de los Patrones

## Strategy + Adapter

**Strategy** aparece en el módulo de pagos. Pedido no procesa el pago directamete, sino que delega esa responsabilidad en la interfaz MetodoPago. Las clases PagoTarjeta, PagoPayPal y PagoTransferencia implementan distintas formas de pagar. Esto permite cambiar el método de pago en tiempo de ejecución sin modificar la clase Pedido.

El **Adapter** complementa a Strategy porque cada método de pago puede necesitar comunicarse con una API externa distinta. Por ejemplo, PagoTarjeta usa AdapterTarjeta, PagoPayPal usa AdapterPaypal y PagoTransferencia usa AdapterTransferencia. Cada adapter traduce la forma particular de trabajar del proveedor externo a una interfaz común: AdapterPagoExterno, que devuelve un ResultadoPago. Así, el sistema no queda acoplado directamente a las APIs externas.

[screenshot-strategy-adapter.png]

## Observer

**Observer** aparece en el sistema de notificaciones de pedidos. La interfaz `SujetoObservable` define los métodos suscribir(), desuscribir() y notificar(). Pedido implementa esa interfaz y actúa como sujeto concreto: mantiene una lista de `CanalNotificacion` y elige cuando disparar las notificaciones.

La interfaz observador (`CanalNotificacion`) define el método `actualizar(pedido)` que cada canal debe implementar. Los observadores concretos como `CanalEmail`, `CanalSMS` y `CanalPush` implementan esa interfaz.

Cuando cambia el estado de un pedido, este ejecuta `notificar()`, que recorre la lista de canales y llama a `actualizar(pedido)` en cada uno. Esto permite avisar al cliente por distintos medios sin que `Pedido` conozca los detalles de cada canal.

[./images/observer-section.png](./images/observer-section-blur.png)

## State

**State** se usa para representar el ciclo de vida de un pedido. `Pedido` funciona como contexto, tiene un atributo "estado" de tipo `EstadoPedido` que referencia al estado actual.

La interfaz `EstadoPedido` define tres métodos: `avanzar(pedido Pedido)`, `puedeAvanzar()` y `nombre()`. Los estados concretos que la implementan son `EstadoPendiente`, `EstadoPagado`, `EstadoEnviado` y `EstadoEntregado`.

`avanzar(pedido Pedido)` recibe el propio Pedido como parámetro: esto le permite al estado concreto cambiar del estado actual del pedido al siguiente, sin que Pedido sepa la lógica de transición. `puedeAvanzar()` devuelve true si la transición es válida desde el estado actual. `nombre()` devuelve el nombre del estado, utilizado para registrar cada cambio en `historialEstados`.

La cadena de transición es: Pendiente → Pagado → Enviado → Entregado. `EstadoEntregado` es el estado final: su `puedeAvanzar()` devuelve `false` y su `avanzar` no hace nada. Esto evita llenar `Pedido` de condicionales y encapsula las reglas de transición dentro de cada clase de estado.

[./images/state-section.png](./images/state-section-blur.png)

## Singleton

**Singleton** aparece en Configuracion. Esta clase centraliza parámetros generales del ecommerce como moneda, tasaIVA, costoEnvio y umbralEnvioGratis.

La clase se contiene a si misma: tiene un atributo estático `instancia` del propio tipo `Configuracion`, representado en el diagrama con una flecha de dependencia que va de la clase hacia sí misma. El acceso se hace únicamente vía `obtenerInstancia()`, que crea ese objeto la primera vez y devuelve la misma referencia en llamadas posteriores. Así, cualquier parte del sistema que necesite saber la moneda, la tasa de IVA o el umbral de envío gratis siempre consulta el mismo objeto sin riesgo de inconsistencias.

[./images/singleton-section.png](./images/singleton-section.png)

## Composite

**Composite** se usa para modelar el catálogo de productos como una estructura jerárquica.

La clase base es `ComponenteCatalogo`, que define la operación `obtenerProductos()`. A partir de esa abstracción aparecen dos tipos de elementos: `Categoria` y `Producto`.

`Categoria` funciona como el componente compuesto, porque contiene una lista de hijos del tipo `ComponenteCatalogo`, que pueden ser otras categorías o productos directamente. Por ejemplo, una categoría como "Hombre" contiene subcategorías como "Camisetas" o "Pantalones", cada una de las cuales agrupa los `Producto` correspondientes.

`Producto`, en cambio, funciona como hoja del árbol. No contiene otros elementos, sino que representa un producto final del catálogo. 

La ventaja del patrón es que el sistema puede tratar de forma uniforme a categorías y productos. Si llama a `obtenerProductos()` sobre una categoría, esta puede recorrer sus hijos y devolver todos los productos contenidos directa o indirectamente. Si se llama sobre un producto, simplemente devuelve ese producto. Esto permite manejar categorías, subcategorías y productos sin lógica especial para cada caso.

[./images/composite-section.png](./images/composite-section.png)

## Facade

Sin **Facade**, cualquier cliente externo tendría que orquestar manualmente los subsistemas en el orden correcto: verificar el carrito, instanciar un `Pedido`, invocar `procesarPago()` sobre el `MetodoPago` elegido y, finalmente, llamar a `notificar()` para avisar al cliente por sus canales configurados.

`TiendaFacade` encapsula toda esa secuencia en un único método `confirmarCompra()` que simplifica la operatoria para el cliente. Internamente crea y coordina los objetos necesarios (`Pedido`, `MetodoPago`, `CanalNotificacion`) sin exponerlos. El cliente solo conoce la fachada y recibe un `ResultadoPago`. No necesita saber cómo funciona ningón subsistema por dentro.

[./images/facade-section.png](./images/facade-section.png)