# cakify
From Oven to Order – A web-based cake ordering and management system for a home-based bakery. Customers can browse cakes, place orders, and send inquiries while the admin manages products, orders, and analytics via a dashboard.


# From Oven to Order
**Managing Your Cakes, One Click at a Time!**

A web-based platform designed for a home-based cake business to streamline order management, product cataloging, and customer inquiries. Customers can browse cakes, place orders, and leave reviews, while the owner manages products, orders, and business analytics via an admin dashboard.

---

## Features

### Customer
- Browse cakes by category (birthday, wedding, party, etc.)
- View cake details (image, description, size, price)
- Place orders with customer information and special notes
- Send inquiries via a contact form
- Post reviews (verified buyers only)
- Receive email notifications for order updates

### Admin / Owner
- Manage products (CRUD operations, categories, availability)
- Manage orders (CRUD operations, update statuses, workflow management)
- Handle customer inquiries (reply, mark as resolved)
- View analytics (sales, cake popularity)
- Role-based authentication (secure login with JWT)

---

## Technology Stack
- **Frontend:** React + Tailwind CSS
- **Backend:** Spring Boot (REST APIs)
- **Database:** MySQL / PostgreSQL

---

## Non-Functional Requirements
- Responsive & mobile-friendly design
- Secure login and authentication
- User-friendly UI/UX
- Cash on Delivery payment method
- Local or cloud image storage

---

## Project Team
- IT24100315 - M.K.M. Pehesara
- IT24100487 - R.M.A. Priyashan
- IT24101492 - E.J.M.S. De Silva
- IT24101512 - Ashen Bandara

---

## Client
- **Name:** Mrs. Thushani Pigera  
- **Business:** Home-based cake business specializing in birthday, wedding, and celebration cakes.

---

## Setup Instructions (Optional Placeholder)
```bash
# Clone the repository
git clone https://github.com/ashenbandara02/cakify.git

# Navigate to the project directory
cd from-oven-to-order

# Frontend Setup
cd frontend
npm install
npm start

# Backend Setup
cd backend
./mvnw spring-boot:run
