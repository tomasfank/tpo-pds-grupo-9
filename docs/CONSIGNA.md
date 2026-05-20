# TPO Proceso de Desarrollo de Software

## Aplicación de E-Commerce

### Objetivo

Diseñar y desarrollar una aplicación de comercio electrónico en Java, aplicando conceptos de Análisis y Diseño Orientado a Objetos, diagramas UML y al menos tres patrones de diseño vistos en clase. El trabajo se realizará en grupos de aproximadamente 6 alumnos.

### Enunciado

La empresa E-Market desea lanzar una plataforma de e-commerce. La aplicación debe
permitir:

1. Registro y login de usuarios
    - Cada usuario puede registrarse como Cliente o Administrador.
    - El login debe validar credenciales y permitir acceso diferenciado según el rol.

2. Catálogo de productos
    - Los alumnos decidirán qué tipo de productos se venden (ejemplo: libros, ropa tecnología, alimentos).
    - Cada producto tiene atributos básicos (nombre, precio, stock, categoría).
    - El catálogo debe poder organizarse jerárquicamente (categorías y subcategorías).

3. Carrito de compras
    - Los clientes pueden agregar productos al carrito, modificar cantidades y eliminar ítems.
    - El sistema debe calcular el total dinámicamente.

4. Métodos de pago
    - Deben existir al menos tres formas de pago (ejemplo: tarjeta de crédito, PayPal, transferencia).
    - El sistema debe permitir elegir el método de pago en tiempo de ejecución.

5. Gestión de pedidos
    - Al confirmar la compra, se genera un pedido con estado inicial “Pendiente”.
    - El pedido puede cambiar de estado (Pendiente → Pagado → Enviado → Entregado).
    - Los administradores pueden actualizar el estado de los pedidos.

6. Notificaciones
    - Los clientes deben recibir notificaciones cuando su pedido cambie de estado.
    - Las notificaciones pueden ser por email, SMS o push (simulado).

### Requisitos de diseño
1. Diagramas UML:
    - Diagrama de clases principal.
    - Diagrama de secuencia para al menos dos casos de uso (ejemplo: “Confirmar compra”, “Cambiar estado de pedido”).

2. Patrones de diseño (mínimo 3):
    - Strategy: para implementar los distintos métodos de pago.
    - Observer: para las notificaciones de cambios de estado en los pedidos.
    - State: para manejar los estados de un pedido.
    - (Opcional: Composite para categorías de productos, Singleton para la clase de configuración, Facade para simplificar acceso a subsistemas).

3. Implementación en Java:
    - Código orientado a objetos, con encapsulamiento, herencia y polimorfismo.
    - Aplicación de los patrones seleccionados.
    - Validaciones básicas (ejemplo: stock suficiente, credenciales correctas).

### Entregables
1. Informe con análisis funcional, casos de uso, .

2. Diagramas UML completos y diseño detallado de patrones aplicados.

3. Código Java funcionando, con comentarios para fácil comprensión

### Evaluación
1. Documentación y presentación (20%): claridad del informe, explicación de decisiones de diseño.

2. Diseño UML (30%): claridad, corrección y aplicación de patrones.

3. Implementación en Java (50%): calidad del código, uso de principios GRASP,
SOLID, evitar “Bad Smells”, aplicación de POO y patrones.

4. Para aprobar, cada uno de los entregables debe estar aprobado, es decir, no es
posible aprobar entregando sólo el código y la documentación sin entregar los diagramas UML.

6. Fecha de entrega: 16/06/26.

7. El detalle de la defensa se explicará en clase.