# M Bazaar - A Three-Tier E-Commerce System with Role-Based Access Control
### Pinnacle Lab + College Perspective | Java 17 + Spring Boot 4.1.1

> **Antigravity me banayenge, IntelliJ se run karenge** | DB: `ecommerce_db` @ `127.0.0.1:3306` (root/001100)

---

## 1. Project Overview

**Title:** M Bazaar - A Three-Tier E-Commerce System with Role-Based Access Control
**Submitted To:** SR Group of Institution

**Three-Tier Architecture:**

| Tier | Technology | Kaam |
|---|---|---|
| **Presentation Tier** | HTML, CSS, Vanilla JavaScript | 7 alag pages - user interface |
| **Application Tier** | Spring Boot REST API + Spring Security (JWT) | Business logic, login, role check |
| **Data Tier** | MySQL + JPA/Hibernate | Products, users, orders, categories |

**Goal:** Ek multi-category E-Commerce platform jaha har category (Electronics, Fashion, Books, Grocery etc) ka saman bech/kharid sakte hai.

**Roles (Updated 06-09-2026):**
* `GUEST` - bina login, sirf dekh sakta hai (products/categories/search) - DB me nahi
* `USER` - Buyer, kharid sakta hai (cart, order COD, apne orders)
* `SELLER` - Seller, apne products manage (CRUD own products, apne orders dekhe)
* `ADMIN` - Official Company, sab control (users, categories, all products, all orders, roles assign)

**Payment:** Abhi **COD (Cash on Delivery) hi** - Razorpay nahi.

**Architecture:** Proper **MVC (Model-View-Controller)** + Layered Architecture (Controller -> Service -> Repository -> DB)

---

## 2. Tech Stack (Tumhara `pom.xml:1` wala)

| Layer | Technology | Version |
|-------|------------|---------|
| Language | Java | 17 (Temurin 17.0.20.1) |
| Framework | Spring Boot | 4.1.1 |
| Build | Maven Wrapper | 3.9.16 (`mvnw.cmd`) |
| DB | MySQL | 8.0.46 (`ecommerce_db`) |
| ORM | Spring Data JPA + Hibernate 7.4.5 | - |
| Security | Spring Security 6.x | `starter-security` |
| Web | Spring MVC (Tomcat 11) | `starter-webmvc` |
| Driver | MySQL Connector/J | runtime |

**Verified:** `mvnw clean compile` -> BUILD SUCCESS, `HikariPool` connected.

---

## 3. MVC + Package Structure

```
com.ecom.E_Commerce/
├── ECommerceApplication.java        // Main class
├── controller/                      // API layer - URL handle
│   ├── AuthController.java          // /api/auth/register , /api/auth/login
│   ├── CategoryController.java      // /api/categories
│   ├── ProductController.java       // /api/products
│   ├── CartController.java          // /api/cart
│   ├── OrderController.java         // /api/orders
│   └── AdminController.java         // /api/admin/*
├── service/                         // Business logic
│   ├── UserService.java
│   ├── ProductService.java
│   ├── CartService.java
│   ├── OrderService.java
│   └── impl/                        // Implementation
├── repository/                      // DB query (JPA)
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── ProductRepository.java
│   ├── CartRepository.java
│   └── OrderRepository.java
├── model/ / entity/                 // Table = Class
│   ├── User.java
│   ├── Category.java
│   ├── Product.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Address.java
│   └── Role.java (enum)
├── dto/                             // Request/Response (Entity ko direct expose nahi)
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── ProductDto.java
│   └── ApiResponse.java
├── security/                        // JWT
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
├── config/                          // Setup
│   ├── SecurityConfig.java
│   └── WebConfig.java
├── exception/                       // Error handle
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
└── util/
```

**Flow:** `Frontend/Postman -> Controller -> Service -> Repository -> MySQL -> Service -> DTO -> Controller -> JSON`

---

## 4. Database Design (ER Diagram)

```
[users] 1----* [addresses]
  |                    (1 user ke multiple address)
  |
  *----* [carts] 1----* [cart_items] *----1 [products]
  |         (1 user ka 1 cart)
  |
  *----* [orders] 1----* [order_items] *----1 [products]
  |
[products] *----1 [categories]
```

**Tables Detail:**

**1. users**
| Column | Type | Note |
|--------|------|------|
| id | BIGINT PK | Auto |
| name | VARCHAR | |
| email | VARCHAR UNIQUE | Login ID |
| password | VARCHAR | BCrypt |
| role | ENUM | ADMIN, SELLER, USER (GUEST DB me nahi) |
| created_at | DATETIME | |

**2. categories**
| id | PK |
| name | VARCHAR UNIQUE (Electronics, Fashion) |
| description | VARCHAR |
| parent_id | FK -> categories.id (Sub-category ke liye) |

**3. products**
| id | PK |
| name | VARCHAR |
| description | TEXT |
| price | DECIMAL |
| stock | INT |
| image_url | VARCHAR |
| category_id | FK -> categories.id |
| created_at | DATETIME |

**4. carts**
| id | PK |
| user_id | FK UNIQUE -> users.id |

**5. cart_items**
| id | PK |
| cart_id | FK -> carts.id |
| product_id | FK -> products.id |
| quantity | INT |

**6. orders**
| id | PK |
| user_id | FK -> users.id |
| total_amount | DECIMAL |
| status | ENUM (PLACED, SHIPPED, DELIVERED, CANCELLED) |
| payment_method | VARCHAR (COD) |
| shipping_address | VARCHAR |
| order_date | DATETIME |

**7. order_items**
| id | PK |
| order_id | FK -> orders.id |
| product_id | FK -> products.id |
| quantity | INT |
| price | DECIMAL (order time ka price) |

**8. addresses**
| id | PK |
| user_id | FK -> users.id |
| street, city, state, pincode | VARCHAR |
| is_default | BOOLEAN |

---

## 5. Features - Phase 1 (Abhi banayenge)

### A. Auth (JWT) - Updated Roles
* `POST /api/auth/register` - {name,email,password,role: "USER" or "SELLER"} -> default USER, SELLER allow, ADMIN via DB ( `service/AuthService.java:41` )
* `POST /api/auth/login` - email, password -> JWT token return (role claim)
* `GET /api/auth/me` - apni profile (JWT se)

### B. Category
* `GET /api/categories` - sab category
* `POST /api/admin/categories` - ADMIN hi add kare (Electronics etc)
* Sub-category support: `parentId` se

### C. Product (Multi-Category)
* `GET /api/products` - sab product (filter: `?category=1&minPrice=500`)
* `GET /api/products/{id}` - ek product
* `GET /api/products/search?keyword=iphone` - search
* `POST /api/admin/products` - ADMIN add product (name, price, stock, categoryId, image)
* `PUT /api/admin/products/{id}` - update
* `DELETE /api/admin/products/{id}`

### D. Cart
* `GET /api/cart` - apna cart dekho (JWT se user)
* `POST /api/cart/add` - {productId, quantity}
* `PUT /api/cart/update` - quantity change
* `DELETE /api/cart/remove/{productId}`

### E. Order (COD)
* `POST /api/orders/place` - cart se order banao, {shippingAddress, paymentMethod: "COD"} -> Order create, Cart clear, stock kam
* `GET /api/orders` - apne orders
* `GET /api/orders/{id}` - order detail
* `PUT /api/orders/{id}/cancel` - cancel (PLACED ho to)
* `GET /api/admin/orders` - ADMIN sab orders dekhe
* `PUT /api/admin/orders/{id}/status` - status update (SHIPPED etc)

---

## 6. API Flow Example (COD Order)

```
1. User Register: POST /api/auth/register {name, email, password} -> 201
2. Login: POST /api/auth/login -> {token: "eyJhbGc..."}
3. Header me: Authorization: Bearer <token>
4. Products dekho: GET /api/products?category=Electronics
5. Cart add: POST /api/cart/add {productId:1, quantity:2} 
6. Order place: POST /api/orders/place {shippingAddress:"Delhi", paymentMethod:"COD"} 
   -> Order PLACED, total=20000, status=PLACED
7. Admin: PUT /api/admin/orders/1/status {status:"SHIPPED"}
```

---

## 7. Security Flow (JWT)

* Register pe password BCrypt se encode
* Login pe `AuthenticationManager` check -> `JwtTokenProvider` token banaye (expiry 24h)
* Har request pe `JwtAuthenticationFilter` token validate -> `SecurityContext` me user set
* `SecurityConfig:61` me: `/api/auth/**` public (GUEST), `/api/products/**`+`/api/categories/**` public (GUEST), `/api/admin/**` -> ADMIN only, `/api/seller/**` -> SELLER+ADMIN, `/api/cart/**`+`/api/orders/**` -> USER/SELLER/ADMIN

---

## 8. College ke liye kya dikhana hai

* **PPT me:** MVC diagram, ER diagram, Tech Stack, Features, Screenshots (Postman + MySQL tables)
* **Report me:** Introduction, Objective, System Design, Database Schema, API List, Future Scope (Razorpay, Wishlist)
* **Demo:** Register -> Add Product (Admin) -> Add to Cart -> Place COD Order -> Admin Order Manage

---

## 9. Next Steps (Hum ab karenge)

1. `application.properties` done ✅
2. **Step 1:** `User` + `Role` entity + `Auth` (JWT) 
3. **Step 2:** `Category` + `Product` entity + CRUD
4. **Step 3:** `Cart` + `CartItem`
5. **Step 4:** `Order` + `OrderItem` (COD)
6. **Step 5:** Global Exception + Security Config
7. Testing via Postman + IntelliJ Run

> Har step Antigravity me code likhenge, IntelliJ me `mvnw spring-boot:run` ya Run button se test karenge.

Bolo, Step 1 - User Auth se start kare?
