# SYNOPSIS

## M Bazaar - A Three-Tier E-Commerce System with Role-Based Access Control

**Course:** B.Tech / B.E. Computer Science (3rd Year)
**Subject:** Mini Project / Software Engineering
**Session:** 2026 - 27

---

### 1. Title
**M Bazaar - A Three-Tier E-Commerce System with Role-Based Access Control** — Design a Shopping system in Java for e-commerce websites, offering product selection, cart management and checkout functionality.

---

### 2. Problem Statement
Online shopping ka daily use badh raha hai, lekin chhote aur mid-level online stores ke liye ready-made shopping software bahut mehnga ya complicated hota hai. Manual order handling se mistakes hoti hain — jaise stock galat update hona, galat customer ko order mil jaana, ya cart ka total calculate na hona.

Is project me hum ek aisa web application banate hain jo online store ko ye teen basic kaam karne deta hai:

1. **Product Selection** — customer products dekh, category se filter aur search kar sake
2. **Cart Management** — cart me product add kare, quantity badhe-chhade, item hata sake
3. **Checkout** — cart se order bana sake, address de sake aur order track kar sake

---

### 3. Objectives
1. Customer ko bina login (Guest) ke bhi products dekhne dena
2. Login ke baad apna cart dekhne, modify karne ka facility dena
3. Stock availability check karke cart update karna (jab stock kam ho error dena)
4. COD (Cash on Delivery) se checkout karwana, order banate waqt stock kam karna
5. Order cancel karne par stock wapas add karna
6. Role based access — Guest, User, Seller aur Admin ke liye alag-alag features
7. Security ke liye JWT token authentication use karna
8. MySQL database me data store karna

---

### 4. Project Description
Ye project ek **Spring Boot** based web application hai jisme frontend HTML/CSS/JavaScript aur backend Java (Spring Boot) use hua hai. Saara data **MySQL** database me store hota hai.

Project me **4 roles** hain:

| Role | Kya kar sakta hai |
|---|---|
| **Guest** (no login) | Products aur categories dekh sakta hai, search kar sakta hai |
| **User** (Customer) | Guest + Cart manage, Order place (COD), Apne orders dekhna/cancel karna |
| **Seller** | User + Apne products add/edit/delete karna, Apni sales dekhna |
| **Admin** | Sab kuch — categories, users, roles, all products, all orders, seller tracking |

**Main Modules:**

1. **Authentication Module** — Register, Login, JWT token generate, current user, role update (Admin only)
2. **Category Module** — Category add/update/delete (with sub-category support), all categories list
3. **Product Module** — Product add/update/delete, search by keyword, filter by category, image upload, public/hide option
4. **Cart Module** — Add item, update quantity, remove item, clear cart, total calculation
5. **Order Module (Checkout)** — Place COD order from cart, stock deduction, view orders, cancel order with stock restore, Admin order status update
6. **Seller Module** — Seller dashboard: own products, sales, revenue, stats
7. **Admin Module** — All orders, user list, role assignment, seller tracking (stock + orders + revenue)

---

### 5. Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Programming Language | Java | 17 |
| Framework | Spring Boot | 4.1.1 |
| Build Tool | Maven | 3.9.x |
| Database | MySQL | 8.0 |
| ORM / Database Access | Spring Data JPA (Hibernate) | - |
| Security / Authentication | Spring Security + JWT (jjwt) | 0.12.6 |
| Web Framework | Spring MVC (Tomcat) | - |
| Frontend | HTML, CSS, Vanilla JavaScript | - |
| API Testing Tool | Postman | - |
| IDE | IntelliJ IDEA | - |

---

### 6. Architecture
Project **3-tier layered architecture** me banaya gaya hai:

```
Presentation Layer  (HTML / CSS / JavaScript)
        ↓
Controller Layer    (AuthController, ProductController, CartController, ...)
        ↓
Service Layer       (AuthService, ProductService, CartService, OrderService, ...)
        ↓
Repository Layer    (UserRepository, ProductRepository, ...)
        ↓
Database            (MySQL - ecommerce_db)
```

Business logic Service layer me hai aur database se baat sirf Repository layer karti hai.

---

### 7. Database Design
Project me total **7 tables** hain:

| Table | Purpose |
|---|---|
| `users` | User ki details (name, email, BCrypt password, role) |
| `categories` | Category aur sub-category (self-referencing parent_id) |
| `products` | Product details (name, price, stock, image, seller) |
| `carts` | Har user ka ek cart (user_id unique) |
| `cart_items` | Cart ke items (cart_id, product_id, quantity) |
| `orders` | Order details (total, status, payment method, address) |
| `order_items` | Order ke items with **price snapshot** at order time |

---

### 8. Future Scope
1. Online payment gateway (Razorpay / Stripe) integration
2. Wishlist / Favourite products feature
3. Product reviews and ratings
4. Saved address book for faster checkout
5. Email notification on order confirmation
6. Invoice / bill PDF generation
7. Product pagination and sorting for large catalogue
8. Mobile responsive design improvement

---

### 9. Conclusion
Is project ke through ek complete, working e-commerce shopping system banaya gaya hai jo product selection, cart management aur checkout — ye teen core functionalities provide karta hai. Spring Boot ke saath 3-tier architecture, JWT based security aur role-based access use kiya gaya hai. Project successfully MySQL database par test kiya gaya hai aur saare modules kaam kar rahe hain.

---

**Submitted by:** ____________________
**Roll No.:** ____________________
**Guide:** ____________________
