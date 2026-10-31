# M Bazaar - A Three-Tier E-Commerce System with Role-Based Access Control

Java 17 + Spring Boot 4.1.1 + MySQL based e-commerce shopping system with
**Product Selection → Cart Management → Checkout (COD)** flow.

---

## Features

| # | Feature | APIs |
|---|---|---|
| 1 | Product Selection (browse, category filter, search) | 5 |
| 2 | Cart Management (add / update qty / remove / clear) | 5 |
| 3 | Checkout — COD order placement + order tracking | 6 |
| 4 | Authentication with JWT (Register / Login / Role) | 6 |
| 5 | Category management with sub-categories (Admin) | 5 |
| 6 | Product image upload to server | 2 |
| 7 | Seller dashboard (own products, sales, revenue) | 3 |
| 8 | Admin panel (users, roles, all orders, seller tracking) | 4 |

### Roles
* **GUEST** — no login, can only browse products & categories
* **USER** — customer: cart + own orders
* **SELLER** — user + manage own products, view own sales
* **ADMIN** — company: everything (users, roles, categories, all orders)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1.1 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | MySQL 8.0 (`ecommerce_db`) |
| Frontend | HTML, CSS, Vanilla JavaScript |
| Build | Maven |

---

## Project Structure

```
src/main/java/com/ecom/E_Commerce/
├── ECommerceApplication.java      # Main class
├── controller/                    # API layer
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── ProductController.java
│   ├── ProductImageController.java
│   ├── CartController.java
│   ├── OrderController.java
│   ├── AdminController.java
│   └── SellerController.java
├── service/                       # Business logic
│   ├── AuthService.java
│   ├── CategoryService.java
│   ├── ProductService.java
│   ├── CartService.java
│   ├── OrderService.java
│   └── FileStorageService.java
├── repository/                    # Database access (JPA)
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── ProductRepository.java
│   ├── CartRepository.java
│   ├── CartItemRepository.java
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
├── model/                         # Database entities
│   ├── User.java, Role.java
│   ├── Category.java
│   ├── Product.java
│   ├── Cart.java, CartItem.java
│   ├── Order.java, OrderItem.java, OrderStatus.java
├── dto/                           # Request / Response objects
├── security/                      # JWT filter, token provider
├── config/                        # SecurityConfig, WebConfig
└── exception/                     # GlobalExceptionHandler

src/main/resources/
├── application.properties         # DB + JWT config
└── static/                        # Frontend (index.html, css, js, uploads)

docs/
├── SYNOPSIS.md                    # Project synopsis
├── PROJECT_DOCS.md                # Full documentation
└── diagrams/                      # UML / ER / DFD diagrams (.puml)
```

---

## How To Run

### Prerequisites
* Java 17 (Temurin)
* MySQL 8.0 running on port 3306
* Maven (or use included `mvnw`)

### Steps

**1. Database banao** (optional — auto create ho jayega)
```sql
CREATE DATABASE ecommerce_db;
```

**2. Config check karo** — `src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/ecommerce_db?...
spring.datasource.username=root
spring.datasource.password=001100
```
> Default tables `ddl-auto=update` se automatically ban jayengi.

**3. Application run karo**
```bash
./mvnw spring-boot:run
```
ya IntelliJ me `ECommerceApplication.java` par Run button dabao.

**4. Browser me kholo**
```
http://localhost:8080
```

**Default Admin account:** `yogesh@testcom` / `123456`

---

## Database Tables

| Table | Purpose |
|---|---|
| `users` | name, email, password (BCrypt), role |
| `categories` | name, description, parent_id (sub-category) |
| `products` | name, description, price, stock, image_url, is_public, seller_id |
| `carts` | user_id (1 user = 1 cart) |
| `cart_items` | cart_id, product_id, quantity |
| `orders` | user_id, total_amount, status, payment_method, shipping_address |
| `order_items` | order_id, product_id, quantity, price (snapshot) |

---

## API Reference

Full API list with request bodies → [`APIs.txt`](APIs.txt) and [`API_Body_and_Keys.txt`](API_Body_and_Keys.txt)

Postman collection → [`Pinnacle_ECommerce_Collection.postman_collection.json`](Pinnacle_ECommerce_Collection.postman_collection.json)

### Main APIs

**Public (no token needed)**
```
GET    /api/categories
GET    /api/products
GET    /api/products?categoryId=1
GET    /api/products?keyword=iphone
GET    /api/products/{id}
POST   /api/auth/register
POST   /api/auth/login
```

**Requires JWT token**
```
GET    /api/cart
POST   /api/cart/add              { productId, quantity }
PUT    /api/cart/update           { productId, quantity }
DELETE /api/cart/remove/{productId}
DELETE /api/cart/clear

POST   /api/orders/place          { shippingAddress, paymentMethod:"COD" }
GET    /api/orders
GET    /api/orders/{id}
PUT    /api/orders/{id}/cancel
```

**Admin only**
```
POST   /api/admin/categories
PUT    /api/admin/categories/{id}
DELETE /api/admin/categories/{id}
GET    /api/admin/orders
PUT    /api/admin/orders/{id}/status    { status }
GET    /api/admin/users
PUT    /api/auth/role                    { email, role }
```

**Seller only**
```
GET    /api/seller/products
GET    /api/seller/orders
GET    /api/seller/stats
```

---

## Diagrams

`docs/diagrams/` me 10 diagrams hain (PlantUML source):

| File | Diagram |
|---|---|
| `01_ER_Diagram.puml` | Entity Relationship |
| `02_Architecture_Diagram.puml` | 3-tier architecture |
| `03_Class_Diagram.puml` | UML Class diagram |
| `04_Use_Case_Diagram.puml` | Use cases per role |
| `05_Sequence_Diagram_Checkout.puml` | Checkout sequence flow |
| `06_DFD_Level0.puml` | DFD context |
| `07_DFD_Level1.puml` | DFD level 1 |
| `08_Activity_Diagram_Login.puml` | Login activity |
| `09_Deployment_Diagram.puml` | Deployment |
| `10_Control_Flow_Checkout.puml` | Control flow |

**Render karne ke liye:** [plantuml.com/plantuml/plantuml](https://www.plantuml.com/plantuml/plantuml) pe file paste karo, ya VS Code me "PlantUML" extension install karo.

---

## Testing

API testing Postman se ki gayi hai. Collection import karke top-to-bottom chalayein:

```
01 Register → 02 Login → 03 Categories → 04 Products → 05 Filter
→ 06 Search → 07 Product by ID → 08 Get Cart → 09 Add to Cart
→ 10 Update Qty → 11 Place Order → 12 My Orders → 13 Order by ID
```

---

## Future Scope
* Razorpay / Stripe payment gateway
* Wishlist feature
* Product reviews & ratings
* Saved address book
* Email notification on order
* Invoice PDF generation
