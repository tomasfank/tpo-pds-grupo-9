# RIVA Frontend — Contexto

## Fuente de verdad del dominio

Antes de implementar cualquier vista, componente o flujo, consultar [`docs/RIVA.md`](../docs/RIVA.md) (en la raíz del repo). Allí están los requerimientos funcionales (RF-01 a RF-26), los casos de uso detallados (CU-01 a CU-24) con flujos principal/alternativo/excepciones y precondiciones, y las relaciones entre casos de uso.

Para cada vista del frontend, partir de los casos de uso del rol correspondiente: la matriz de CU por rol está en la sección 7.4 de `docs/RIVA.md`. El contexto general del proyecto está en el [`CLAUDE.md`](../CLAUDE.md) de la raíz.

## Stack

- **Framework:** React (Create React App o Vite)
- **Runtime:** Node 20 Alpine
- **Puerto:** 3000
- **Comunicación con backend:** REST sobre `REACT_APP_API_URL` (default `http://localhost:8080`)

## Estructura esperada del proyecto

```
frontend/
├── public/
├── src/
│   ├── components/      # componentes reutilizables (Button, Input, Modal, etc.)
│   ├── pages/           # una carpeta por pantalla/vista
│   ├── services/        # llamadas HTTP al backend (axios o fetch)
│   ├── context/         # AuthContext, CartContext
│   ├── hooks/           # custom hooks
│   └── App.js
├── package.json
└── Dockerfile
```

## Vistas requeridas por el dominio

| Vista | Roles |
|---|---|
| Registro | Público |
| Login | Público |
| Catálogo / listado de productos | Cliente |
| Detalle de producto | Cliente |
| Carrito de compras | Cliente |
| Checkout (selección de método de pago) | Cliente |
| Mis pedidos / historial | Cliente |
| Configuración de notificaciones | Cliente |
| Panel de administración — productos | Administrador |
| Panel de administración — pedidos | Administrador |

## Variables de entorno

```env
REACT_APP_API_URL=http://localhost:8080
```

En producción/Docker esta variable se inyecta por el `docker-compose.yaml` vía `API_URL`.

## Comandos locales

```bash
npm install      # instalar dependencias
npm start        # levantar dev server en puerto 3000
npm run build    # build de producción (genera /build)
npm test         # correr tests
```

## Dockerfile

El build es multi-stage:
1. Stage `builder` — instala dependencias y corre `npm run build`
2. Stage final — copia `/build` y sirve la app

El contenedor corre con usuario no-root `riva`.

## Consideraciones de diseño

- La sesión del usuario se maneja con JWT recibido del backend. Almacenar en `localStorage` o context según decisión del equipo.
- Las rutas deben estar protegidas por rol: el cliente no puede acceder al panel de admin y viceversa.
- Los métodos de pago (Tarjeta, PayPal, Transferencia) se renderizan condicionalmente según selección del usuario — no hardcodear lógica de negocio en el front.
- El estado del carrito debe recalcularse en cada cambio de cantidad o eliminación de ítem.
- Las notificaciones de cambio de estado de pedido pueden mostrarse como toast/alert al cliente.
