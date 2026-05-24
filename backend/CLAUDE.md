# RIVA Backend — Contexto

## Fuente de verdad del dominio

Antes de implementar cualquier controller, service, entidad o regla de negocio, consultar [`docs/RIVA.md`](../docs/RIVA.md) (en la raíz del repo). Allí están los requerimientos funcionales (RF-01 a RF-26), los casos de uso detallados (CU-01 a CU-24) con flujos principal/alternativo/excepciones y precondiciones, y la asignación explícita de patrones de diseño (Strategy, Observer, State, Composite) a casos de uso concretos.

El contexto general del proyecto y la convención de patrones está en el [`CLAUDE.md`](../CLAUDE.md) de la raíz.

## Stack

- **Framework:** Spring Boot 3
- **Lenguaje:** Java 21
- **Base de datos:** MongoDB 7 (via Spring Data MongoDB)
- **Autenticación:** JWT (Spring Security)
- **Build:** Maven 3.9
- **Puerto:** 8080

## Estructura de paquetes esperada

```
backend/
├── src/main/java/com/riva/
│   ├── config/          # configuración de seguridad, beans, CORS
│   ├── controller/      # REST controllers por módulo
│   ├── service/         # lógica de negocio
│   ├── repository/      # interfaces de Spring Data MongoDB
│   ├── model/           # entidades del dominio
│   │   ├── user/
│   │   ├── category/
│   │   ├── product/
│   │   ├── cart/
│   │   ├── order/
│   │   └── notification/
│   ├── exception/       # excepciones de dominio + @RestControllerAdvice
│   ├── pattern/         # implementaciones de patrones de diseño
│   │   ├── strategy/    # métodos de pago
│   │   ├── observer/    # notificaciones
│   │   ├── state/       # estados del pedido
│   │   └── composite/   # jerarquía de categorías
│   └── dto/             # objetos de transferencia (request/response)
├── src/main/resources/
│   └── application.yml
├── src/test/
├── pom.xml
└── Dockerfile
```

## Variables de entorno

```env
# Spring Boot 4 movió el prefix de spring.data.mongodb.* a spring.mongodb.*
SPRING_MONGODB_URI=mongodb://riva:rivapass@mongo:27017/rivadb?authSource=admin
SPRING_MONGODB_DATABASE=rivadb
JWT_SECRET=changeme_in_production
```

En desarrollo local sin Docker, apuntar el URI a `localhost:27017`.

## Endpoints REST esperados por módulo

| Módulo | Método | Path |
|---|---|---|
| Auth | POST | `/api/auth/register` |
| Auth | POST | `/api/auth/login` |
| Productos | GET | `/api/products` |
| Productos | GET | `/api/products/{id}` |
| Productos | POST | `/api/products` (admin) |
| Productos | PUT | `/api/products/{id}` (admin) |
| Productos | DELETE | `/api/products/{id}` (admin) |
| Carrito | GET | `/api/cart` |
| Carrito | POST | `/api/cart/items` |
| Carrito | PUT | `/api/cart/items/{productId}` |
| Carrito | DELETE | `/api/cart/items/{productId}` |
| Pedidos | POST | `/api/orders` |
| Pedidos | GET | `/api/orders` |
| Pedidos | GET | `/api/orders/{id}` |
| Pedidos | PATCH | `/api/orders/{id}/status` (admin) |
| Notificaciones | PUT | `/api/notifications/preferences` |

## Patrones de diseño — ubicación e intención

### Strategy — Métodos de pago
- Interfaz `PaymentStrategy` con método `pay(amount)`
- Implementaciones: `CreditCardPayment`, `PaypalPayment`, `BankTransferPayment`
- El `OrderService` recibe la estrategia en tiempo de ejecución según lo que envíe el cliente

### Observer — Notificaciones
- Interfaz `NotificationObserver` con método `notify(Order order)`
- Implementaciones: `EmailNotifier`, `SmsNotifier`, `PushNotifier`
- El `Order` o `OrderService` actúa como subject y notifica a los observers suscritos según preferencias del cliente

### State — Ciclo de vida del pedido
- Interfaz `OrderState` con método `next(Order order)`
- Implementaciones: `PendingState`, `PaidState`, `ShippedState`, `DeliveredState`
- Solo el Administrador puede invocar la transición. El estado `DeliveredState` no tiene transición siguiente.

### Composite — Categorías de productos
- Interfaz o clase abstracta `CatalogComponent`
- `Category` puede contener otras `Category` o `Product` (hojas)
- Permite recorrer el árbol de categorías de forma uniforme

### NOTAS 

Siempre que apliques patrones de diseño en algún lugar, deja una nota con la información necesaria para saber que patrón es y porque tomaste la desición de utilizarlo allí. 

## Reglas de negocio críticas

- Al agregar al carrito: validar stock disponible para la **variante** (combinación de talla y color) seleccionada. Ver CU-14 en `docs/RIVA.md`.
- Al confirmar la compra (CU-18): se crea el pedido en estado **"Pendiente"** sin descontar stock.
- Al procesar el pago exitosamente (CU-20): descontar stock de las variantes adquiridas y transicionar el pedido a **"Pagado"**.
- Si el pago falla (CU-20): el pedido **permanece en "Pendiente"** sin descuento de stock, se notifica al cliente y se le permite reintentar con otro método (vuelve a CU-19).
- Si el stock de una variante se agota entre el carrito y el procesamiento del pago (concurrencia): cancelar la transacción de pago, mantener el pedido en "Pendiente" e informar al cliente.
- Las contraseñas deben hashearse con BCrypt antes de persistir.
- El JWT debe incluir el rol del usuario para el control de acceso en cada endpoint.
- Solo el Administrador puede avanzar el estado de un pedido (CU-23).

## Comandos locales

```bash
mvn clean install          # compilar y correr tests
mvn spring-boot:run        # levantar en modo desarrollo
mvn package -DskipTests    # generar el JAR
```

## Dockerfile

El build es multi-stage:
1. Stage `builder` — Maven descarga dependencias y empaqueta el JAR
2. Stage final — JRE 21 Alpine, corre el JAR con usuario no-root `riva`