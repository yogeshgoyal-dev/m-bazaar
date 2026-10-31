# PPT Outline — M Bazaar - A Three-Tier E-Commerce System with Role-Based Access Control
**Total slides: 14 | Duration: 10-12 min**

---

### Slide 1 — Title Slide
```
M Bazaar - A Three-Tier E-Commerce System with Role-Based Access Control
A Java + Spring Boot Web Application

Name:
Roll No:
Course / Year:
Subject: Mini Project
Guide:
Date:
```

---

### Slide 2 — Introduction
* Online shopping ka use har din badh raha hai
* Chhote/medium online stores ke liye ready-made software mehnga + complicated
* Manual order handling se mistakes — stock galat update, galat customer ko order
* **Solution:** ek web application jo shopping ka core kaam kare

> Speaker tip: 30 second me batao, zyada mat bolo

---

### Slide 3 — Problem Statement
* Customer ko product **dekhne, choose karne** me dikkat
* Cart me item add/remove karne ka koi reliable tarika nahi
* Stock check nahi hota → overselling
* Order track karne ka system nahi

**Goal:** Product Selection + Cart Management + Checkout — ye 3 cheezein solve karna

---

### Slide 4 — Objectives
1. Guest ko bina login products dekhne dena
2. Cart me item add / quantity change / remove
3. Stock check ke saath cart update
4. COD checkout — order banate waqt stock deduct
5. Order cancel pe stock restore
6. Role based access (Guest / User / Seller / Admin)
7. JWT authentication for security
8. MySQL me data storage

---

### Slide 5 — System Architecture (Diagram)
📌 **`docs/diagrams/02_Architecture_Diagram.puml`**

3-tier layered:
```
Frontend (HTML/CSS/JS)
      ↓
Controller Layer
      ↓
Service Layer  ← business logic
      ↓
Repository Layer
      ↓
MySQL Database
```

> Bolo: "Business logic Service layer me hai, DB se baat sirf Repository karti hai"

---

### Slide 6 — Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 4.1.1 |
| ORM | Spring Data JPA | - |
| Security | Spring Security + JWT | jjwt 0.12.6 |
| Database | MySQL | 8.0 |
| Frontend | HTML, CSS, Vanilla JS | - |
| Build | Maven | 3.9.x |
| IDE | IntelliJ IDEA | - |

---

### Slide 7 — Modules

| Module | Kya karta hai |
|---|---|
| Authentication | Register, Login, JWT token, Role update |
| Category | Category CRUD + sub-category |
| Product | Product CRUD, search, filter, image upload |
| Cart | Add, update qty, remove, clear, total |
| Order (Checkout) | COD order, stock deduct, cancel, status |
| Seller | Own products, sales, revenue stats |
| Admin | Users, roles, all orders, seller tracking |

---

### Slide 8 — ER Diagram
📌 **`docs/diagrams/01_ER_Diagram.puml`**

7 tables: `users, categories, products, carts, cart_items, orders, order_items`

Bol kya kya:
* 1 user = 1 cart
* 1 cart = many cart_items
* 1 order = many order_items
* `order_items` me **price snapshot** — taaki price change hone se purana order na bigde

---

### Slide 9 — Use Case Diagram
📌 **`docs/diagrams/04_Use_Case_Diagram.puml`**

4 actors: Guest, User, Seller, Admin

| Actor | Key use cases |
|---|---|
| Guest | Browse products, Search, Register, Login |
| User | Add to Cart, Place Order, View/Cancel Orders |
| Seller | Manage own products, View sales |
| Admin | Manage categories/users/roles, All orders, Update status |

---

### Slide 10 — Key Algorithm: Checkout Flow
📌 **`docs/diagrams/05_Sequence_Diagram_Checkout.puml`** ya **`10_Control_Flow_Checkout.puml`**

```
1. User cart kholega
2. Place Order (COD) click karega
3. System cart items fetch karega
4. Har item ka stock check (stock < qty → error, rollback)
5. Stock deduct karo
6. Order + OrderItems save karo (status = PLACED)
7. Cart clear karo
8. Order confirmation dikhao
```

**Cancel karne pe:** status PLACED hona chahiye → stock wapas add → status = CANCELLED

---

### Slide 11 — Security (JWT)
* Password **BCrypt** se encode hota hai (plain text me kabhi nahi)
* Login pe JWT token milta hai (24 hour expiry)
* Har request me `Authorization: Bearer <token>`
* `JwtAuthenticationFilter` token validate karta hai
* `SecurityConfig` role decide karta hai kaun kaun si API call kar sakta hai

```java
/api/auth/**          → public
/api/products/**, /api/categories/**  → GET only, public (Guest)
/api/cart/**, /api/orders/**         → USER, SELLER, ADMIN
/api/admin/**        → ADMIN only
/api/seller/**       → SELLER + ADMIN
```

---

### Slide 12 — Live Demo / Screenshots
1. **Home page** — categories + 21 products + search
2. **Login** — Guest vs User vs Admin ka UI change
3. **Cart** — add item, quantity +/-, remove, total
4. **Checkout** — COD order place, success message
5. **My Orders** — status PLACED → SHIPPED → DELIVERED
6. **Seller Dashboard** — products, sales, revenue
7. **Admin Panel** — all orders, users, roles, seller tracking
8. **Postman** — 401 error without token (proof of security)

---

### Slide 13 — Testing
* **Functional testing** — Postman se saare 30+ APIs test kiye
* **Security testing** — bina token / wrong role pe 401/403 verify kiya
* **Edge cases:**
  * Cart empty → order place nahi hota
  * Stock se zyada quantity → error
  * Non-PLACED order cancel nahi hota
  * Seller doosre ka product edit nahi kar sakta

> Agar guide bole "unit tests?" — bolo abhi integration testing ki, unit testing future scope me.

---

### Slide 14 — Conclusion & Future Scope

**Conclusion:**
Spring Boot aur MySQL ke saath working e-commerce shopping system banaya jo
product selection, cart management aur checkout provide karta hai. Role-based
access aur JWT security add ki gayi.

**Future Scope:**
* Razorpay online payment
* Wishlist feature
* Reviews & ratings
* Saved address book
* Email notification
* Invoice PDF

---

### Slide 15 — Thank You
```
Thank You!
Any Questions?

Project: M Bazaar - A Three-Tier E-Commerce System with Role-Based Access Control
Tech: Java 17 + Spring Boot 4.1.1 + MySQL
```

---

## Viva Questions — Ready Answers

**Q1. JWT kaise kaam karta hai?**
Login pe server ek token banata hai jo 3 parts se hota hai: Header, Payload
(user id + role), Signature (secret key se encrypted). Har request me ye token
jaata hai, server signature verify karke user ki identity aur role pata karta
hai. Token me password nahi hota — sirf id, email, role hota hai.

**Q2. Cart me same product 2 baar add karo toh?**
Naya row nahi banta — existing `CartItem` ki quantity badh jaati hai
(`CartService.addItem()` me check hota hai ki cart_item already hai ya nahi).

**Q3. Order cancel karne pe stock kaise wapas aata hai?**
`OrderService.cancelOrder()` me har `OrderItem` ka product fetch karke
`product.setStock(stock + quantity)` karke save karta hai, phir order status
`CANCELLED` set karta hai. Ye check karta hai ki order `PLACED` hi ho.

**Q4. Seller doosre ka product kaise edit nahi kar sakta?**
`ProductService.update()` me check hai — agar seller `ROLE_ADMIN` nahi hai aur
product ka seller current user se match nahi karta toh exception throw hota hai:
"You can only update your own products".

**Q5. Password secure hai?**
Haan. `BCryptPasswordEncoder` use hua hai jo password ko one-way hash karta
hai — database me plain password kabhi store nahi hota. BCrypt salt internally
add karta hai, isliye same password ka hash bhi har baar alag hota hai.

**Q6. Stock kab kam hota hai?**
Sirf order place karte waqt aur `OrderService.placeOrder()` me — har cart item
ke liye `product.setStock(stock - quantity)`. Cart me add karte waqt sirf
**check** hota hai, deduct nahi.

**Q7. Payment kaise ho raha hai?**
Sirf **COD (Cash on Delivery)** — abhi online payment gateway nahi hai.
`OrderService.placeOrder()` me agar payment method COD nahi hota toh error
throw hota hai. Razorpay future scope me hai.

**Q8. GUEST ko cart kyu nahi dikhta?**
Security: `/api/cart/**` pe `hasAnyRole("USER","SELLER","ADMIN")` lagaya hai.
Guest ke paas token hi nahi hota. Frontend bhi `showPage()` me check karta hai
aur login page pe bhej deta hai.

**Q9. @PreAuthorize aur SecurityConfig me kya farak hai?**
SecurityConfig me **URL/path based** rule hai (kaun si URL pe kaun access
karega). `@PreAuthorize` **method level** pe lagta hai — specific method ko
guard karta hai, chahe wo kisi bhi controller se call ho. Project me dono
dono use hue hain — URL rules SecurityConfig me, extra safety `@PreAuthorize`.

**Q10. N+1 query problem kya hai?**
Jab ek query ke result ke liye loop me har row ka alag query chala lo —
jaise `AdminController.getSellerStats()` me har order ke har item ke liye
product fetch hota hai. 1 + N queries ban jaati hain, performance ghatati hai.
Solution: `@EntityGraph` ya JOIN fetch use karna.
