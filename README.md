# Proyecto Hextech — Arquitectura de Microservicios

## Descripción del contexto
 Elproyecto Hextech se pensó originalmente como una alternativa a aquellas personas que no tienen opciones para comprar sus juegos o contenido digital de forma eficiente y segura en Chile. Hextech busca ofrecer a los consumidores una serie de opciones las cuales no se ven mucho en nuestro país mediante un sistema más amigable y pendiente de ellos.

## Integrantes
- Javier Romero
- Cristóbal Zúñiga
- Benjamin Ruiz

## Microservicios implementados

| Servicio | Descripción | Puerto |
|---|---|---|
| server (Eureka) | Service Discovery | 8761 |
| gateway | API Gateway (Spring Cloud Gateway) | 8080 |
| ms-carrito | Gestión de carrito de compras | 9092 |
| ms-descuentos | Gestión de cupones y descuentos | 9098 |
| ms-envios | Gestión de envíos | 9099 |
| ms-order | Gestión de órdenes | 9097 |
| ms-pagos | Procesamiento de pagos | 9094 |
| ms-productos | Catálogo de productos | 9095 |
| ms-reviews | Reseñas de productos | 9091 |
| ms-subscriptions | Suscripciones | 9089 |
| ms-tickets | Tickets de soporte | 9096 |
| ms-users | Gestión de usuarios y autenticación (JWT) | 9090 |
| ms-wishlist | Lista de deseos | 9093 |

## Rutas principales del Gateway
Todas las rutas pasan por `http://localhost:8080` y se enrutan por Eureka (`lb://`):

| Ruta | Servicio destino |
|---|---|
| `/api/v1/descuentos/**`, `/api/v1/discounts/**` | ms-descuentos |
| `/api/v1/pagos/**` | ms-pagos |
| `/api/v1/productos/**` | ms-productos |
| `/api/v1/carrito/**` | ms-carrito |
| `/api/v1/envios/**` | ms-envios |
| `/api/v1/order/**` | ms-order |
| `/api/v1/reviews/**` | ms-reviews |
| `/api/v1/subscriptions/**` | ms-subscriptions |
| `/api/v1/tickets/**` | ms-tickets |
| `/api/v1/user/**`, `/api/v1/admin/**`, `/api/v1/auth/**` | ms-users |
| `/api/v1/wishlist/**` | ms-wishlist |

## Documentación Swagger / OpenAPI
Cada microservicio expone su propia UI de Swagger en:
Ejemplos:
- ms-productos: http://localhost:9095/swagger-ui.html
- ms-carrito: http://localhost:9092/swagger-ui.html
- ms-order: http://localhost:9097/swagger-ui.html
- ms-pagos: http://localhost:9094/swagger-ui.html
- ms-descuentos: http://localhost:9098/swagger-ui.html
- ms-reviews: http://localhost:9091/swagger-ui.html
- ms-subscriptions: http://localhost:9089/swagger-ui.html
- ms-tickets: http://localhost:9096/swagger-ui.html
- ms-wishlist: http://localhost:9093/swagger-ui.html
- ms-users: http://localhost:9090/swagger-ui.html
- ms-envios: http://localhost:9099/swagger-ui.html

## Instrucciones de ejecución

### Local (desde el IDE)
1. Levantar `server` (Eureka Server) primero
2. Levantar `gateway`
3. Levantar el resto de los microservicios (orden indistinto)
4. Verificar que todos se registraron correctamente en: http://localhost:8761

### Con Docker
```bash
docker-compose up --build
```

## Tecnologías utilizadas
- Java 21 + Spring Boot 3.4
- Spring Cloud Gateway + Eureka (Service Discovery)
- Spring Data JPA / Hibernate + MySQL
- Flyway (migraciones de base de datos)
- Spring Security + JWT
- WebClient (comunicación REST entre microservicios)
- JUnit 5 + Mockito (pruebas unitarias)
- JaCoCo (cobertura de pruebas)
- Swagger / OpenAPI (springdoc-openapi)
- Docker + Docker Compose