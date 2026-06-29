# ShopSmart

A production-style Spring Boot e-commerce backend that demonstrates secure authentication, scalable REST API design, database optimization, and containerized deployment.

This project was built to simulate real-world backend development practices rather than focusing only on CRUD functionality. It includes JWT authentication, role-based authorization, Google OAuth 2.0 login, Docker support, image upload optimization, pagination, and PostgreSQL integration.

---

## Features

### Authentication & Security

* JWT-based authentication
* Two step verification with email
* Access Token + Refresh Token authentication flow
* Secure HTTP-only cookie storage
* Spring Security filter chain configuration
* BCrypt password hashing
* Google OAuth 2.0 Login
* Role-based authorization (Admin/User)

### Product Management

* Create, update, delete, and view products
* Image upload support
* Users can manage only their own products
* Administrators can manage all products
* Paginated product listing
* Product search using JPQL with DTO projections

### User Management

* User registration and login
* Request validation
* Global exception handling
* Authentication and authorization middleware

### Shopping Cart

* One-to-one relationship between User and Cart
* Add and remove products from cart
* Persistent cart management

### Database & Performance

* PostgreSQL database
* Hibernate / Spring Data JPA
* Lazy loading for image data using LOB fetch strategies
* DTO projections to reduce unnecessary data fetching
* Pagination for scalable API responses

### Deployment

* Dockerized backend
* Environment variable configuration
* Production-ready project structure
* Logging for monitoring and debugging

---

## Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring MVC
* Spring Security
* Spring Data JPA
* Hibernate

### Database

* PostgreSQL

### Authentication

* JWT
* OAuth 2.0 (Google)
* BCrypt

### Build & Deployment

* Maven
* Docker

### Other

* Lombok
* Validation API
* REST APIs
* DTO Pattern

---

## Project Structure

```
src
├── controller
├── dto
├── entity
├── repository
├── service
│   ├── interfaces
│   └── implementation
├── security
├── configuration
├── exception
├── util
└── ShopSmartApplication
```

The project follows a layered architecture that separates responsibilities between controllers, services, repositories, entities, DTOs, and security configuration, making the codebase easier to maintain and extend.

---

## Security Features

* Stateless JWT authentication
* Refresh token mechanism
* Secure HTTP-only cookies
* Password encryption with BCrypt
* Role-based endpoint protection
* Spring Security authorization filters
* Google OAuth 2.0 authentication

---

## API Features

* RESTful API design
* CRUD operations
* Request validation
* Global exception handling
* Pagination
* Search functionality
* Image upload support
* DTO-based responses

---

## Database Design

The application uses PostgreSQL with Hibernate/JPA for persistence.

Key relationships include:

* User ↔ Cart (One-to-One)
* User ↔ Products (One-to-Many)
* Product image stored using LOB with lazy fetching to improve performance.

---

## Getting Started

### Prerequisites

* Java 26
* Maven
* PostgreSQL
* Docker

### Clone the Repository

```bash
git clone https://github.com/arpan-v/shopsmart_backend.git
cd shopsmart
```

### Export Environment Variables

1. Go to `src/java/com/arpan/backend/resources`

2. Open the file application.properties

3. Export all the env's to the machiene

### Run the Application

Using Maven:

```bash
mvn spring-boot:run
```

Or build and run:

```bash
mvn clean package
java -jar target/shopsmart.jar
```

### Run with Docker

```bash
docker build -t shopsmart .

docker run -p 8080:8080 shopsmart
```

---

## Future Improvements

* Redis caching
* Payment gateway integration
* Elasticsearch for advanced product search
* Order management
* API documentation with Swagger/OpenAPI
* CI/CD using GitHub Actions
* AWS deployment (ECR, ECS/EKS, RDS)

---

## What This Project Demonstrates

This project showcases practical backend engineering concepts commonly used in production applications, including:

* Secure authentication and authorization
* REST API development
* Layered Spring Boot architecture
* Database design with JPA/Hibernate
* Performance optimization techniques
* Docker-based deployment
* Clean code organization
* Exception handling and validation
* Scalable backend development practices

---

## License

This project is intended for educational and portfolio purposes.
