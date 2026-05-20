# RIVA Backend — Contexto

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
│   │   ├── product/
│   │   ├── cart/
│   │   ├── order/
│   │   └── notification/
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
SPRING_DATA_MONGODB_URI=mongodb://riva:rivapass@mongo:27017/rivadb?authSource=admin
SPRING_DATA_MONGODB_DATABASE=rivadb
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

## Reglas de negocio críticas

- Al agregar al carrito: validar stock disponible para la talla y color seleccionados.
- Al confirmar compra: descontar stock solo si el pago es exitoso.
- Si el pago falla: no generar pedido, no modificar stock, notificar al cliente.
- Si el stock se agota entre el carrito y el checkout: cancelar la transacción e informar.
- Las contraseñas deben hashearse con BCrypt antes de persistir.
- El JWT debe incluir el rol del usuario para el control de acceso en cada endpoint.
- Solo el Administrador puede avanzar el estado de un pedido.

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
