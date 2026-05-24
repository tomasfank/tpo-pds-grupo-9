# RIVA — Backend

Backend de la plataforma RIVA (TPO Proceso de Desarrollo de Software, UADE). Servicio REST en Spring Boot 4 + Java 21 con persistencia MongoDB, expuesto en el puerto **8080**.

> Antes de tocar código, leé [`docs/RIVA.md`](../docs/RIVA.md) (análisis funcional, RF-01 a RF-26, CU-01 a CU-24) y [`backend/CLAUDE.md`](./CLAUDE.md) (convenciones del módulo).

---

## Estado actual del backend

| Módulo | Estado | Casos de uso cubiertos |
|---|---|---|
| Infraestructura / Bootstrap | Listo | — |
| Healthcheck | Listo | — |
| Catálogo (Categorías + Productos) | Listo (modelo + servicios + endpoints) | CU-07, CU-09, CU-10, CU-11, CU-12, CU-13 |
| Usuarios / Auth / JWT | **No implementado** | CU-01, CU-02, CU-03, CU-04, CU-05, CU-06 |
| Carrito | **No implementado** | CU-14, CU-15, CU-16, CU-17 |
| Pedidos | **No implementado** | CU-18, CU-21, CU-22, CU-23 |
| Pago (Strategy) | **No implementado** | CU-19, CU-20 |
| Notificaciones (Observer) | **No implementado** | CU-24 |
| Búsqueda con filtros | **No implementado** | CU-08 |

**Importante:** Spring Security está configurado con `permitAll()` temporalmente para poder probar el catálogo sin autenticación. Cuando entre el módulo de Auth (CU-01/02/03), los endpoints marcados como `(admin)` van a requerir rol Administrador. Los controllers tienen comentarios `TODO(auth)` indicando qué hay que cerrar.

---

## Stack

| | |
|---|---|
| Framework | Spring Boot 4.0.6 |
| Lenguaje | Java 21 |
| Persistencia | MongoDB 7 (via Spring Data MongoDB) |
| Seguridad | Spring Security 7 (con configuración mínima abierta por ahora) |
| Build | Maven 3.9 (dentro de Docker) |
| Puerto HTTP | 8080 |
| Puerto Mongo | 27017 |

---

## Estructura del proyecto

```
backend/
├── Dockerfile                       # build multi-stage: maven 3.9 + jre 21 alpine
├── CLAUDE.md                        # convenciones específicas del backend
├── README.md                        # este archivo
└── riva/                            # raíz del proyecto Maven
    ├── pom.xml
    ├── mvnw, mvnw.cmd               # wrapper de Maven (opcional, no se usa en Docker)
    ├── .mvn/wrapper/...
    └── src/main/
        ├── java/com/riva/
        │   ├── RivaApplication.java         # main class
        │   ├── config/
        │   │   └── SecurityConfig.java      # permitAll temporal
        │   ├── controller/
        │   │   ├── HealthController.java
        │   │   ├── CategoryController.java
        │   │   ├── ProductController.java
        │   │   └── CatalogController.java
        │   ├── service/
        │   │   ├── CategoryService.java     # CU-13
        │   │   ├── ProductService.java      # CU-10/11/12
        │   │   └── CatalogService.java      # CU-07/09
        │   ├── repository/
        │   │   ├── CategoryRepository.java
        │   │   └── ProductRepository.java
        │   ├── model/
        │   │   ├── category/Category.java   # Composite role
        │   │   └── product/
        │   │       ├── Product.java         # Leaf role
        │   │       ├── ProductVariant.java
        │   │       └── Size.java
        │   ├── pattern/
        │   │   └── composite/
        │   │       └── CatalogComponent.java # interface compartida
        │   ├── dto/                          # records de request/response
        │   └── exception/
        │       ├── NotFoundException.java
        │       ├── ConflictException.java
        │       ├── ValidationException.java
        │       └── GlobalExceptionHandler.java
        └── resources/
            └── application.yml
```

> El directorio se llamaba originalmente `proceso-desarrollo-software/`. Lo renombramos a `riva/` para alinearlo con el nombre del artefacto y el package `com.riva`.

---

## Cómo levantar el backend con Docker

Toda la infraestructura corre con `docker compose` desde la raíz del repo (`tpo-pds-grupo-9/`). No hace falta tener Java ni Maven instalados localmente.

### Pre-requisitos

- Docker Desktop o Docker Engine + Docker Compose v2
- Puertos 8080 y 27017 libres en tu máquina

### Comandos básicos (con Makefile en la raíz)

```bash
make build       # construye las imágenes
make up          # levanta backend + mongo + frontend en background
make down        # baja los servicios
make rebuild     # down + build sin caché + up
make logs        # logs de todos los servicios
make logs-back   # logs del backend
make logs-mongo  # logs de Mongo
make clean       # baja todo y elimina volúmenes e imágenes
make shell-back  # shell dentro del contenedor del backend
make shell-mongo # mongosh dentro del contenedor de Mongo
```

### Equivalente con `docker compose` (si no usás Makefile)

```bash
cd tpo-pds-grupo-9
docker compose up -d --build backend       # solo backend + mongo (depende del healthcheck de mongo)
docker compose logs -f backend             # ver logs
docker compose down                        # bajar
```

### Verificar que arrancó

```bash
curl http://localhost:8080/api/health
# → {"status":"UP","service":"riva-backend","timestamp":"..."}
```

El contenedor `riva-backend` tiene un `HEALTHCHECK` configurado en el Dockerfile que llama a `/api/health` cada 30s. Podés verificar su estado con:

```bash
docker ps --filter name=riva-backend --format "table {{.Names}}\t{{.Status}}"
# → riva-backend   Up 30 seconds (healthy)
```

### Variables de entorno

Las definidas en [`docker-compose.yaml`](../docker-compose.yaml) (con defaults razonables):

```env
SPRING_MONGODB_URI=mongodb://riva:rivapass@mongo:27017/rivadb?authSource=admin
SPRING_MONGODB_DATABASE=rivadb
JWT_SECRET=changeme_in_production
```

Para sobreescribirlas, creá un `.env` en la raíz del repo con `MONGO_USER`, `MONGO_PASSWORD`, `MONGO_DB`, `JWT_SECRET`, etc. (ver root `CLAUDE.md`).

---

## Endpoints disponibles

Base path: `http://localhost:8080/api`

### Health

| Método | Path | CU | Descripción |
|---|---|---|---|
| GET | `/health` | — | Liveness check. Lo usa el HEALTHCHECK de Docker. |

### Catálogo (público)

| Método | Path | CU | Descripción |
|---|---|---|---|
| GET | `/catalog/tree` | CU-07 | Árbol completo de categorías con conteo de productos activos por nodo (Composite). |
| GET | `/catalog/categories/{id}/products` | CU-07 | Productos activos en el subárbol de una categoría (directos + transitivos). |
| GET | `/products` | CU-07 / CU-08 | Lista todos los productos activos. (Filtros de CU-08 pendientes.) |
| GET | `/products/{id}` | CU-09 | Detalle de un producto. |

### Categorías (admin — sin auth todavía)

| Método | Path | CU | Descripción |
|---|---|---|---|
| GET | `/categories` | CU-13 | Lista plana de categorías. |
| POST | `/categories` | CU-13 | Crea categoría. Body: `{"name":"...", "parentId": "..."?}`. |
| PUT | `/categories/{id}` | CU-13 | Renombra. Body: `{"name":"..."}`. |
| PUT | `/categories/{id}/parent` | CU-13 | Reubica. Body: `{"parentId":"..."?}`. Valida ciclos. |
| DELETE | `/categories/{id}` | CU-13 | Desactiva. Falla 409 si hay productos activos en el subárbol. |
| POST | `/categories/{id}/activate` | CU-13 | Reactiva una categoría desactivada. |

### Productos (admin — sin auth todavía)

| Método | Path | CU | Descripción |
|---|---|---|---|
| POST | `/products` | CU-10 | Crea producto con variantes (talla + color + stock). |
| PUT | `/products/{id}` | CU-11 | Edita campos del producto. Si cambia `categoryId`, recalcula `categoryAncestorIds`. |
| DELETE | `/products/{id}` | CU-12 | Desactiva (soft delete). |

### Códigos HTTP que devuelve el backend

| Código | Cuándo |
|---|---|
| 200 / 201 | OK / Creado |
| 400 | Validación fallida (body inválido, ciclo, variante duplicada, etc.) |
| 404 | Recurso no encontrado |
| 409 | Conflicto de negocio (desactivar categoría con productos activos, etc.) |
| 500 | Error no manejado (no debería pasar) |

El formato de error siempre es:

```json
{
  "timestamp": "2026-05-24T01:40:29.841Z",
  "status": 409,
  "error": "Conflict",
  "message": "No se puede desactivar la categoría: hay productos activos asociados en el subárbol",
  "details": null
}
```

---

## Pruebas manuales rápidas con `curl`

### 1. Crear jerarquía de prueba

```bash
HOMBRES=$(curl -sf -X POST http://localhost:8080/api/categories \
  -H 'Content-Type: application/json' \
  -d '{"name":"Hombres"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['id'])")

CAMISETAS=$(curl -sf -X POST http://localhost:8080/api/categories \
  -H 'Content-Type: application/json' \
  -d "{\"name\":\"Camisetas\",\"parentId\":\"$HOMBRES\"}" \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['id'])")

curl -sf -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d "{
    \"name\":\"Camiseta básica RIVA\",
    \"description\":\"100% algodón\",
    \"price\":12999.99,
    \"material\":\"Algodón\",
    \"categoryId\":\"$CAMISETAS\",
    \"variants\":[
      {\"size\":\"M\",\"color\":\"Negro\",\"stock\":10},
      {\"size\":\"L\",\"color\":\"Negro\",\"stock\":5}
    ]
  }"
```

### 2. Ver el árbol del catálogo

```bash
curl -s http://localhost:8080/api/catalog/tree | python3 -m json.tool
```

### 3. Ver productos bajo una categoría (recursivo)

```bash
curl -s "http://localhost:8080/api/catalog/categories/$HOMBRES/products" | python3 -m json.tool
```

### 4. Probar reglas de negocio

```bash
# Debe devolver 409 — la categoría tiene producto activo
curl -i -X DELETE "http://localhost:8080/api/categories/$CAMISETAS"

# Debe devolver 400 — ciclo en la jerarquía
curl -i -X PUT "http://localhost:8080/api/categories/$HOMBRES/parent" \
  -H 'Content-Type: application/json' \
  -d "{\"parentId\":\"$CAMISETAS\"}"
```

---

## Patrones de diseño aplicados (hasta acá)

### Composite — Catálogo

Implementado en [`pattern/composite/CatalogComponent.java`](./riva/src/main/java/com/riva/pattern/composite/CatalogComponent.java).

- **Component**: `CatalogComponent` (interface) — define las operaciones uniformes (`getName`, `isActive`, `isLeaf`, `countActiveProducts`).
- **Composite**: `Category` — puede contener otras `Category` o `Product`. `countActiveProducts()` recursa sobre `children`.
- **Leaf**: `Product` — devuelve 1 o 0 según si está activo.

**Persistencia híbrida**: la estructura padre-hijo se guarda en Mongo como `parentId` + `ancestorIds: [...]` (denormalización). El árbol se rehidrata en memoria en `CatalogService` cuando se necesita recorrerlo. Esto permite:

- Composite "puro" en el código (el patrón es la entrada gráfica del entregable).
- Mongo idiomático (queries planos sobre índices, sin subdocumentos infinitos).
- Detectar ciclos al reubicar en O(1) — basta chequear si `target.id ∈ subject.ancestorIds`.
- Listar productos bajo categoría X con un solo `find({ categoryAncestorIds: X })`.

Los patrones que faltan implementar (Strategy, State, Observer) están documentados en `backend/CLAUDE.md`.

---

## Reglas de negocio activas

Implementadas y testeadas vía smoke test:

- **RF-07 / CU-07**: jerarquía Composite con conteo recursivo de productos activos.
- **RF-11 / CU-13 alt 6a**: no se puede desactivar una categoría con productos activos en el subárbol → HTTP 409.
- **RF-11 / CU-13 excepciones**: no se puede mover una categoría dentro de su propio subárbol (ciclo) → HTTP 400.
- **RIVA §2.2**: cada producto tiene al menos una variante (`@NotEmpty` en el DTO). Cada variante debe tener al menos talla o color. No se permiten combinaciones talla+color duplicadas dentro del mismo producto.
- **CU-10/11**: al asignar producto a categoría, se recalcula `categoryAncestorIds` para que el query recursivo funcione.

---

## Problemas encontrados y soluciones (importante para el equipo)

Esta sección documenta los obstáculos que aparecieron mientras armábamos el backend. **Léelos si vas a tocar configuración** — la mayoría son cambios de Spring Boot 4 que sorprenden si venís de tutoriales de SB 3.

### 1. Dockerfile apuntaba a paths incorrectos

El proyecto Maven generado por Spring Initializr quedó en `backend/proceso-desarrollo-software/`, pero el Dockerfile estaba en `backend/` y hacía `COPY pom.xml .`. **El build fallaba** porque no encontraba el `pom.xml`.

**Fix**: el Dockerfile ahora hace `COPY riva/pom.xml .` (después de renombrar el subdirectorio a `riva/`). Si en algún momento se mueve la raíz del proyecto, hay que actualizar los `COPY` en `backend/Dockerfile`.

### 2. Package renamed `com.pds.proceso_desarrollo_software` → `com.riva`

Para alinear con `backend/CLAUDE.md` (que documenta `com.riva.*`) y con el nombre del artefacto. **Si alguien escribe código bajo el package viejo, no va a ser detectado por `@SpringBootApplication` y los beans no se cargan.**

### 3. Spring Boot 4 dividió las propiedades de MongoDB en dos prefixes

Este es el más traicionero. Spring Boot 4 reorganizó las propiedades de MongoDB en **dos prefixes según la capa**:

| Capa | Prefix | Ejemplos |
|---|---|---|
| Driver / conexión | `spring.mongodb.*` | `uri`, `host`, `port`, `database`, `username`, `password`, `ssl.*` |
| Spring Data / mapping | `spring.data.mongodb.*` | `auto-index-creation`, `field-naming-strategy`, `gridfs.*`, `representation.big-decimal` |

**El problema**: si usás las keys viejas (`spring.data.mongodb.uri` o env var `SPRING_DATA_MONGODB_URI`), están marcadas `deprecated` con `level: error` y **Spring las ignora silenciosamente**. El MongoClient se crea con defaults absolutos (`localhost:27017`) y no hay warning en los logs. El síntoma es que falla la conexión a Mongo con "Connection refused" apuntando a `localhost` aunque tu env var dice `mongo:27017`.

**Fix aplicado**: en `docker-compose.yaml` usar `SPRING_MONGODB_URI` y `SPRING_MONGODB_DATABASE`. En `application.yml`, `auto-index-creation` sigue bajo `spring.data.mongodb.*` (capa Spring Data).

**Si vas a sumar propiedades de Mongo, verificá primero en qué jar viven:**

```bash
docker exec riva-backend sh -c \
  "unzip -p /app/app.jar BOOT-INF/lib/spring-boot-mongodb-4.0.6.jar | \
   unzip -p - META-INF/spring-configuration-metadata.json"
```

(Lo mismo con `spring-boot-data-mongodb-4.0.6.jar`.)

### 4. Recursión silenciosa en placeholders de application.yml

Si en el yml escribís `spring.mongodb.uri: ${SPRING_MONGODB_URI:default}`, la env var `SPRING_MONGODB_URI` también se mapea por relaxed binding a la misma key (`spring.mongodb.uri`), causando una resolución ambigua que termina cayendo al `default`. **No se rompe, no avisa, simplemente usa el default**.

**Fix**: dejá que las env vars se bindeen solas vía relaxed binding. No las repitas en `application.yml` con `${...:default}` si la env var tiene un nombre que choca con la prop. Si necesitás un default para dev local sin Docker, usá un nombre de env var distinto o setea el default en un profile dev.

### 5. Healthcheck en Dockerfile vs Compose

Optamos por **Dockerfile** (`HEALTHCHECK CMD wget -qO- http://localhost:8080/api/health`). Beneficios:

- La imagen es portable: cualquier `docker run` hereda el healthcheck.
- Compose lo usa para `depends_on.condition: service_healthy` sin duplicar lógica.
- `wget` está disponible en `eclipse-temurin:21-jre-alpine` vía BusyBox (no hace falta instalar nada).

Si necesitás tunear el intervalo por entorno (más agresivo en dev, más relajado en prod), Compose puede sobreescribir el del Dockerfile con `healthcheck:` en el servicio.

### 6. YAML language server en VSCode marca falsos errores

Si la extensión `redhat.vscode-yaml` está instalada con su catálogo automático activo, intenta mapear `application.yml` a un schema de Kubernetes y marca todo en rojo (`Property spring is not allowed`, `Missing property "kind"`).

**Fix aplicado**: [`.vscode/settings.json`](../.vscode/settings.json) tiene `yaml.schemaStore.enable: false` para este workspace. Spring Boot no publica un schema JSON formal en SchemaStore (las propiedades son dinámicas según las dependencias del classpath).

**Para autocomplete real de application.yml**, instalá la extensión **`vmware.vscode-spring-boot`** (Spring Boot Tools). Lee la metadata del classpath y te avisa si tipeás una key inválida — fundamental para no caer en el problema #3.

### 7. Spring Boot 4 renombró el starter de web

- Antes (SB 3): `spring-boot-starter-web`
- Ahora (SB 4): `spring-boot-starter-webmvc`

Lo mismo con el starter de test: `spring-boot-starter-test` → `spring-boot-starter-webmvc-test`. Si copiás un `pom.xml` de un tutorial de SB 3, Maven no va a encontrar el artefacto.

### 8. Spring Security con `permitAll` está habilitado a propósito

Spring Security está en el classpath. Por defecto bloquearía todos los endpoints con HTTP Basic auto-generado. Para no bloquear el catálogo durante la fase de desarrollo, [`SecurityConfig`](./riva/src/main/java/com/riva/config/SecurityConfig.java) tiene:

```java
.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
```

**No es la configuración final.** Cuando entre el módulo de Auth (CU-01/02/03), hay que reemplazar el `permitAll()` por un `JwtAuthenticationFilter` + reglas por rol. Los endpoints admin (`POST/PUT/DELETE` de categorías y productos) tienen comentarios `TODO(auth)` indicando dónde.

### 9. Variable Mongo y caracteres especiales

La URI de Mongo contiene `@`, `:`, `?` y `=`. Cuando se exporta como env var en docker-compose, no hay problema (compose hace el escaping correcto). Si la setás manualmente con `export SPRING_MONGODB_URI=...`, el shell puede comerse algún carácter — usá comillas simples siempre:

```bash
export SPRING_MONGODB_URI='mongodb://riva:rivapass@localhost:27017/rivadb?authSource=admin'
```

### 10. La extensión Java de VSCode necesita JDK 21

Si abrís el proyecto y ves errores de "cannot resolve symbol" o íconos rojos en clases de Spring, lo más probable es que VSCode no tenga un JDK 21 detectado. Instalalo (Eclipse Temurin 21 anda perfecto) y verificá con `Java: Configure Runtime` en el Command Palette.

---

## Pendientes y próximos pasos

### Pendientes del módulo Catálogo

- **CU-08**: búsqueda con filtros (nombre, talla, color, rango de precio). Requiere queries custom con `MongoTemplate` o `@Query`.
- **`@ConfigurationProperties("app")` + spring-boot-configuration-processor**: para que la extensión Spring Tools no marque `app.jwt.secret` como unknown property.
- **Tests**: no hay tests escritos todavía. El proyecto tiene `spring-boot-starter-webmvc-test` + `spring-security-test` en el classpath listos para usar.

### Próximo paso lógico

**Auth + JWT (CU-01 / CU-02 / CU-03 / CU-04 / CU-05 / CU-06)** es el desbloqueante:

- Sin Auth, los endpoints `(admin)` están abiertos y no podemos diferenciar Cliente de Administrador.
- Sin Auth, no podemos avanzar a Carrito (CU-14+) que requiere asociar el carrito a un usuario.
- Trae el módulo Usuarios completo + Spring Security con filtro JWT + BCrypt para passwords (RNF-01).

Una vez resuelto, los siguientes módulos en orden natural:

1. **Carrito (CU-14 a CU-17)** — usa el modelo de `Product`/`Variant` ya armado.
2. **Pedidos + Strategy + State + Observer (CU-18 a CU-24)** — implementa los tres patrones restantes que faltan para cumplir la consigna de "al menos 3 patrones de diseño".

---

## Notas para los integrantes del grupo

- **No commitear `.env`** ni archivos con credenciales reales. Los defaults del repo son seguros porque son "changeme".
- **No hacer push directo a `main`.** Siempre rama + PR (ver `CLAUDE.md` raíz).
- **Cuando agregues un patrón de diseño nuevo**, dejá un comentario en la clase explicando qué patrón es, qué rol cumple y por qué se eligió. Lo pide `backend/CLAUDE.md` y va a ser pedido también para el entregable.
- **Cuando agregues un endpoint nuevo**, actualizá la tabla de endpoints de este README + el `TODO(auth)` correspondiente si es admin-only.
- **Si tocás `pom.xml` o `Dockerfile`**, corré `make rebuild` (no `make up`) para forzar el rebuild de la imagen.
- **Si tocás `docker-compose.yaml`**, los demás van a tener que hacer `make down && make up` para que tome los cambios.
- **Si Mongo deja de responder**, primero `docker ps` para ver si está healthy. Si el container está down, `make logs-mongo` muestra por qué. Si querés borrar todos los datos: `make clean` (también borra el volumen).
- **Para inspeccionar Mongo directamente**: `make shell-mongo` te abre `mongosh` dentro del contenedor (ya autenticado).
- **Documentación del dominio**: [`docs/RIVA.md`](../docs/RIVA.md) es la fuente de verdad para RF y CU. [`docs/CONSIGNA.md`](../docs/CONSIGNA.md) es el enunciado original de la cátedra.

---

## Comandos rápidos cheat-sheet

```bash
# Levantar todo
make up

# Ver logs del backend en vivo
make logs-back

# Smoke test
curl http://localhost:8080/api/health
curl http://localhost:8080/api/catalog/tree | python3 -m json.tool

# Limpiar todo (borra datos de Mongo)
make clean

# Entrar al contenedor del backend
make shell-back

# Entrar a mongosh
make shell-mongo
```
