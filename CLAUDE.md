# RIVA — Contexto General del Proyecto

## Qué es este proyecto

TPO de la materia **Proceso de Desarrollo de Software** (UADE). Es una plataforma de e-commerce de indumentaria y moda llamada **RIVA**, desarrollada en grupos de ~6 alumnos. Fecha de entrega: **16/06/2026**.

La consigna exige aplicar análisis y diseño orientado a objetos, diagramas UML y al menos 3 patrones de diseño. La evaluación pondera: documentación (20%), UML (30%), código Java (50%).

## Arquitectura

```
frontend/   →   React (Node 20)           → puerto 3000
backend/    →   Spring Boot 3 + Java 21   → puerto 8080
mongo       →   MongoDB 7                 → puerto 27017
```

Toda la infraestructura corre con Docker Compose de manera local. No hay entornos cloud ni CI/CD.

## Módulos funcionales

| Módulo | Descripción |
|---|---|
| Usuarios | Registro, login con roles (Cliente / Administrador), JWT |
| Catálogo | Productos con tallas, colores, categorías jerárquicas |
| Carrito | Agregar/modificar/eliminar ítems, total dinámico |
| Pago | Tarjeta, PayPal, Transferencia (patrón Strategy) |
| Pedidos | Ciclo Pendiente → Pagado → Enviado → Entregado (patrón State) |
| Notificaciones | Email, SMS, Push simulados ante cambios de estado (patrón Observer) |

## Patrones de diseño obligatorios

- **Strategy** — métodos de pago intercambiables en tiempo de ejecución
- **Observer** — notificaciones al cliente ante cambios de estado del pedido
- **State** — ciclo de vida del pedido encapsulado en estados
- **Composite** — jerarquía de categorías y subcategorías de productos

## Actores del sistema

- **Cliente** — navega catálogo, gestiona carrito, compra, consulta pedidos, configura notificaciones
- **Administrador** — gestiona productos, avanza estados de pedidos
- **Sistema de Pagos** — actor externo (simulado)
- **Sistema de Notificaciones** — actor externo (simulado)

## Variables de entorno

Crear un `.env` en la raíz del proyecto basado en estos valores por defecto:

```env
MONGO_USER=riva
MONGO_PASSWORD=rivapass
MONGO_DB=rivadb
JWT_SECRET=changeme_in_production
API_URL=http://localhost:8080
```

## Docker y comandos

```bash
make build       # construye las imágenes
make up          # levanta todos los servicios en background
make down        # baja los servicios
make rebuild     # down + build sin caché + up
make logs        # logs de todos los servicios
make logs-back   # logs del backend
make logs-front  # logs del frontend
make logs-mongo  # logs de MongoDB
make clean       # baja todo y elimina volúmenes e imágenes
make shell-back  # shell en el contenedor del backend
make shell-front # shell en el contenedor del frontend
make shell-mongo # mongosh en el contenedor de MongoDB
```

## Documentación del dominio

La carpeta `docs/` concentra los archivos que dan contexto al proyecto a nivel de entregable universitario. Son la fuente de verdad del dominio y deben consultarse antes de implementar cualquier funcionalidad.

- `docs/RIVA.md` — análisis funcional completo elaborado por el grupo: actores, requerimientos funcionales (RF-01 a RF-26), requerimientos no funcionales (RNF-01 a RNF-05) y casos de uso detallados (CU-01 a CU-24)
- `docs/CONSIGNA.md` — enunciado original de la cátedra con los requisitos de diseño, patrones exigidos, entregables y criterios de evaluación

## Convenciones generales

- No hacer push directo a `main`. Siempre rama + PR.
- Commits en formato Conventional Commits: `feat`, `fix`, `chore`.
- No commitear `.env` ni archivos con credenciales.
