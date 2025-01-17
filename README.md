# Distributed Order Management System

A microservices-based order management system built with Spring Boot, implementing a distributed architecture for handling products, customers, and orders.

## Prerequisites

- Docker and Docker Compose
- Java 17
- Maven
- PostgreSQL (if running locally)
- Redis (if running locally)

## Running the Services

### Using Docker Compose

1. Clone the repository
2. Navigate to the root directory containing `docker-compose.yml`
3. Build and start the services:
```bash
docker-compose up --build
```

### Running Locally

1. Start PostgreSQL instances:
   - Product DB: Port 5432
   - Customer DB: Port 5433
   - Order DB: Port 5434

2. Start Redis on port 6379

3. Build and run each service:
```bash
# For each service (product, customer, order):
cd <service-directory>
mvn clean package
java -jar target/<service-name>.jar
```

## API Examples

### Product Service

1. Create Product
```http
POST http://localhost:8081/api/products
Content-Type: application/json

{
    "sku": "PROD-001",
    "name": "Sample Product",
    "description": "Product description",
    "price": 99.99,
    "stockQuantity": 100
}
```

2. Get Product
```http
GET http://localhost:8081/api/products/{id}
```

### Customer Service

1. Create Customer
```http
POST http://localhost:8082/api/customers
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "address": "123 Main St",
    "active": true
}
```

2. Get Customer
```http
GET http://localhost:8082/api/customers/{id}
```

### Order Service

1. Create Order
```http
POST http://localhost:8083/api/orders
Content-Type: application/json

{
    "customerId": 1,
    "items": [
        {
            "productId": 1,
            "quantity": 2,
            "unitPrice": 99.99
        }
    ],
    "totalAmount": 199.98,
    "notes": "Priority delivery"
}
```

2. Get Order
```http
GET http://localhost:8083/api/orders/{id}
```

3. Cancel Order
```http
POST http://localhost:8083/api/orders/{id}/cancel
```

## Error Handling

The services implement comprehensive error handling:

- 400 Bad Request: Invalid input data
- 404 Not Found: Resource not found
- 409 Conflict: Data conflict (e.g., duplicate entries)
- 429 Too Many Requests: Rate limit exceeded
- 500 Internal Server Error: Unexpected server errors

## Configuration

Each service has its own `application.properties` file with configurations for:
- Server ports
- Database connections
- Redis cache
- Rate limiting
- Circuit breaker
- Logging

## Monitoring

The services expose actuator endpoints for monitoring:
- Health check: `/actuator/health`
- Metrics: `/actuator/metrics`
- Info: `/actuator/info`
- Prometheus metrics: `/actuator/prometheus`




