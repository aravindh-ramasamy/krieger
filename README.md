# Document and Author Management Web Application

This project is a web application for managing documents and authors. It provides RESTful APIs for CRUD operations on authors and documents, secures the APIs using JWT-based authentication, and uses RabbitMQ. The project also includes Docker setup for containerization of services PostgreSQL and RabbitMQ.

## Features

- **CRUD Operations for Authors and Documents:** Add, update, delete, and view authors and documents.
- **JWT-Based Authentication and Authorization:** Secure access to API using JWT Tokens.
- **RabbitMQ Integration:** If author deleted, queue deletes corresponding documents.
- **API Documentation:** Interactive Swagger UI for API endpoints.
- **Dockerization:** Docker support for PostgreSQL and RabbitMQ using `docker-compose`.

## Tech Stack

- **Backend:** Spring Boot (Spring Web, Spring Security, Spring Data JPA, RabbitMQ)
- **Database:** PostgreSQL
- **Message Broker:** RabbitMQ
- **API Documentation:** Swagger
- **Security:** JWT Authentication
- **Testing:** JUnit, Mockito
- **Containerization:** Docker, Docker Compose

## Prerequisites

- Java 17
- Maven 3.8.1
- Docker and Docker Compose
- RabbitMQ

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/AravindhRamasamy/Krieger.git
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run with Docker Compose

```bash
docker-compose up --build
```

This command will bring up PostgreSQL and RabbitMQ in Docker containers, as well as the Spring Boot application.

### 4. Access the Application

- **Swagger API Documentation:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### 5. Run Locally (without Docker)

Alternatively, you can run the application without Docker. In that case, make sure PostgreSQL and RabbitMQ are installed and running on your local machine.

- Update `application.properties` for the PostgreSQL connection details.
- Start the Spring Boot application:

```bash
mvn spring-boot:run
```

## API Endpoints

### Authentication

- **Login:**
  - **URL:** `POST /auth/login`
  - **Request Body:**
    ```json
    {
      "username": "defaultUser",
      "password": "password"
    }
    ```
  - **Response:**
    ```json
    {
      "token": "jwt-token-here"
    }
    ```

### Get Author by ID

- **URL:** `GET /authors/{id}`
- **Response:**
  ```json
  {
    "msg": "Author retrieved successfully",
    "status": "SUCCESS",
    "code": 200,
    "data": {
      "id": 1,
      "firstName": "string1",
      "lastName": "string1",
      "documents": []
    }
  }
  ```

### Add Author

- **URL:** `POST /authors`
- **Request Body:**
  ```json
  {
    "firstName": "John",
    "lastName": "Doe"
  }
  ```
- **Response:**
  ```json
  {
    "msg": "Author created successfully",
    "status": "SUCCESS",
    "code": 201,
    "data": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe"
    }
  }
  ```

### Add Document

- **URL:** `POST /documents`
- **Request Body:**
  ```json
  {
    "title": "string",
    "body": "string",
    "authorID": 1,
    "reference": "string"
  }
  ```

- **Response:**
  ```json
  {
    "msg": "Document created successfully",
    "status": "SUCCESS",
    "code": 201,
    "data": {
      "id": 1,
      "title": "string",
      "body": "string",
      "references": "string"
    }
  }
  ```

### Get Document by ID

- **URL:** `GET /documents/{id}`
- **Response:**
  ```json
  {
    "msg": "Document retrieved successfully",
    "status": "SUCCESS",
    "code": 200,
    "data": {
      "id": 1,
      "title": "Document Title",
      "body": "Document body content...",
      "references": "Reference information"
    }
  }
  ```

### Delete Author by ID

- **URL:** `DELETE /authors/{id}`
- **Response:**
  ```json
  {
    "msg": "Author deleted successfully",
    "status": "SUCCESS",
    "code": 200
  }
  ```

## Event-Driven Architecture

The application publishes events to RabbitMQ when authors or documents are created, updated, or deleted. A consumer listens to these events and performs actions accordingly.

### Example:

- When an author is deleted, the consumer deletes all documents associated with that author.

## Running Unit Tests

To run the tests for the application, use:

```bash
mvn test
```

This will execute unit tests for services, controllers, and other components.

## Docker Setup

### Docker Compose

The `docker-compose.yml` file sets up the following services:
- **PostgreSQL:** For data storage
- **RabbitMQ:** For message queuing

To bring up the containers, run:

```bash
docker-compose up --build
```

### Dockerfile

The project includes a `Dockerfile` for containerizing the Spring Boot application. This allows you to run the entire application in a Docker container.

WorkFlow:

- Users log in using the /authenticate endpoint, receive a JWT token, and use it for secure access to protected API endpoints.
- Spring Security with JWT ensures only authenticated users can access the services.
- Users can add, view, update, and delete authors using the /authors endpoints.
- Events are published to RabbitMQ when authors are created, updated, or deleted.
- Users can add, view, update, and delete documents using the /documents endpoints.
- Each document is linked to an author and includes references.
- When an author is deleted, an event is sent to RabbitMQ.
- A consumer listens for the event and deletes all documents associated with that author.
- Custom exceptions and a global exception handler provide meaningful error messages for invalid requests or server errors.
- Swagger provides an interactive API documentation interface accessible via /swagger-ui.html for testing and viewing API endpoints.
- Unit tests are implemented using JUnit and Mockito to ensure the functionality of the core features (author, document services, JWT, etc.).
- The application, PostgreSQL, and RabbitMQ are containerized using Docker and can be run via docker-compose.

