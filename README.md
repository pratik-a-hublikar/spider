# Spider

A modern, full-stack identity and access management system built with Spring Boot and React.

## 🎯 Overview

Spider is a comprehensive microservices-based application designed to handle user authentication, authorization, and identity management. The project features a robust backend API and an intuitive React-based frontend, all containerized for easy deployment.

## 📋 Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Database Setup](#database-setup)
- [Shipmnts Integration](#shipmnts-integration)
- [API Documentation](#api-documentation)
- [Features](#features)
- [Development](#development)
- [Docker Deployment](#docker-deployment)
- [Contributing](#contributing)
- [License](#license)

## 🛠 Tech Stack

### Backend
- **Framework**: Spring Boot 3.5.7
- **Language**: Java 21
- **Build Tool**: Maven
- **Database**: MySQL/PostgreSQL with Liquibase migrations
- **Caching**: Redis (via Redisson)
- **Authentication**: JWT (JSON Web Tokens)
- **API Documentation**: Swagger/OpenAPI
- **ORM**: Spring Data JPA
- **Validation**: Jakarta Validation
- **Security**: Spring Security

### Frontend
- **Framework**: React 19
- **Build Tool**: Vite
- **UI Library**: Material-UI (MUI)
- **State Management**: Redux Toolkit
- **HTTP Client**: Axios
- **Routing**: React Router v7
- **Styling**: Emotion

## 📁 Project Structure

```
spider/
├── common/                          # Shared utilities and models
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/spider/
│       └── test/java/
│
├── identity/                        # Identity & Access Management Service
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/spider/
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-local.yml
│       │       └── db/changelog/    # Liquibase migrations
│       └── test/java/
│
├── ui/react-auth-skeleton/          # React Frontend
│   ├── src/
│   │   ├── components/              # Reusable React components
│   │   ├── pages/                   # Page components
│   │   ├── features/                # Redux features (auth, user, role, etc.)
│   │   ├── services/                # API service layer
│   │   ├── config/                  # Route and access control config
│   │   ├── layouts/                 # Layout components
│   │   ├── theme/                   # Material-UI theme configuration
│   │   └── constants/               # Application constants
│   ├── scripts/
│   │   └── shipmnts-authenticate.mjs  # Authentication script for Shipmnts
│   ├── package.json
│   └── vite.config.js
│
├── master-sql/
│   └── create-master-user.sql       # Initial database setup
│
├── .env                             # Environment variables
├── docker-compose.yml               # Local development environment
├── Dockerfile                       # Production container image
└── pom.xml                          # Root Maven configuration
```

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21**: [Download JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.8.9+**: [Download Maven](https://maven.apache.org/download.cgi)
- **Node.js 18+**: [Download Node.js](https://nodejs.org/)
- **Docker**: [Download Docker Desktop](https://www.docker.com/products/docker-desktop)
- **Git**: [Download Git](https://git-scm.com/)
- **Database**: MySQL 8.0+ or PostgreSQL 12+ (can use Docker)
- **Redis**: [Download Redis](https://redis.io/download) (can use Docker)
- **Chrome/Chromium**: Required for Shipmnts authentication (Playwright uses browser automation)

## 🚀 Installation

### Clone the Repository

```bash
git clone <repository-url>
cd spider
```

### Backend Setup

#### Build Backend Modules

```bash
# Build the entire project
mvn clean install

# Or build specific modules
mvn clean install -DskipTests
```

#### Frontend Setup

```bash
cd ui/react-auth-skeleton

# Install dependencies
npm install

# Or using yarn
yarn install
```

## ▶️ Running the Application

### Option 1: Using Docker Compose (Recommended for Development)

```bash
# Start all services (database, Redis, backend, frontend)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Option 2: Manual Local Development

#### 1. Start Database and Redis

```bash
# Using Docker
docker run -d --name mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:8.0
docker run -d --name redis -p 6379:6379 redis:latest
```

#### 2. Start Backend Service

```bash
cd identity

# Run with Maven
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# Or build and run JAR
mvn clean package
java -jar target/identity-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

The backend API will be available at: `http://localhost:8080`

#### 3. Start Frontend Application

```bash
cd ui/react-auth-skeleton

# Development server
npm run dev

# Or
yarn dev
```

The frontend will be available at: `http://localhost:5173`

## 🗄️ Database Setup

### Initial Database Creation

```bash
# Execute the master user creation script
mysql -u root -p < master-sql/create-master-user.sql
```

### Database Migrations

Database migrations are managed with Liquibase. Migration files are located in:
```
identity/src/main/resources/db/changelog/
```

Migrations are automatically applied when the application starts.

### Configuration

Update the database configuration in `identity/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spider
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
```

## 🔐 Shipmnts Integration

The Spider application includes integration with Shipmnts for accessing shipment data. The authentication flow uses browser-based OAuth.

### Configuration

Set the following environment variables in `.env`:

```env
# Shipmnts API Credentials
SHIPMNTS_CLIENT_ID=your_client_id_here
SHIPMNTS_EMAIL=your_email@example.com
SHIPMNTS_PASSWORD=your_password_here
SHIPMNTS_ORGANIZATION_ID=your_org_id_here  # Optional
```

### Authentication Flow

There are two ways to generate and manage the Shipmnts refresh token:

#### Option 1: Automatic Authentication via API (Recommended)

Call the authentication endpoint to automatically generate a refresh token:

```bash
# First, ensure you have the correct credentials in .env
# Then call:
POST /api/v1/identity/authenticate
Authorization: Bearer <your-jwt-token>
```

This endpoint will:
1. Execute the Shipmnts authentication script
2. Open a Chrome browser window
3. Navigate through the Shipmnts login flow
4. Capture and save the refresh token to `.env`
5. Return a success message

**Note**: After authentication, restart the backend service so it loads the new token.

#### Option 2: Manual Authentication via npm Script

```bash
cd ui/react-auth-skeleton

# Run the authentication script
npm run shipmnts:authenticate

# Or manually:
npm run shipmnts:authenticate -- --headless=true
```

### Using the /fetch API

Once authenticated, you can fetch Shipmnts user profile data:

```bash
GET /api/v1/identity/fetch
Authorization: Bearer <your-jwt-token>
```

Response example:
```json
{
  "data": {
    "id": "...",
    "first_name": "John",
    "last_name": "Doe",
    "email": "john.doe@example.com",
    "company_account": {
      "id": "...",
      "registered_name": "...",
      "display_name": "..."
    }
  }
}
```

### Troubleshooting Authentication

1. **"Timed out waiting for Shipmnts authentication"**
   - The browser window may be waiting for CAPTCHA or MFA
   - Check the opened Chrome window for any prompts
   - Complete them manually within 5 minutes

2. **"Shipmnts refresh token is blank"**
   - Ensure credentials in `.env` are correct
   - Verify the Shipmnts client ID is valid
   - Try running the authentication again

3. **"Chrome/Chromium not found"**
   - Ensure Chrome is installed on your system
   - The authentication script uses Playwright which needs access to Chrome
   - On Linux: `sudo apt-get install chromium-browser`
   - On macOS: Chrome should be auto-detected
   - On Windows: Chrome should be auto-detected

## 📚 API Documentation

Once the backend is running, access the interactive API documentation:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## ✨ Features

### Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- User management
- Permission management
- Secure password handling
- Shipmnts OAuth integration

### User Interface
- Responsive design with Material-UI
- Protected routes
- Dynamic menu system
- User dashboard
- Authentication flow
- Sales performance tracking
- Multi-language support (i18n)

### Backend Services
- Identity management microservice
- RESTful API endpoints
- Data validation
- Error handling
- Request logging
- Actuator endpoints for monitoring
- Shipmnts data integration

### Performance
- Redis caching layer
- Database connection pooling
- Optimized queries

## 🔧 Development

### Project Structure Best Practices

- **Controllers**: Handle HTTP requests and responses
- **Services**: Business logic implementation
- **Repositories**: Data access layer (Spring Data JPA)
- **DTOs**: Data transfer objects for API contracts
- **Entities**: JPA entity models
- **Exceptions**: Custom exception handling

### Code Quality

```bash
# Run tests
mvn clean test

# Run backend with specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Environment Profiles

- **local**: Local development with mock services
- **dev**: Development environment
- **prod**: Production environment

## 🐳 Docker Deployment

### Build Docker Image

```bash
# Build the Docker image
docker build -t spider:latest .

# Tag for registry
docker tag spider:latest your-registry/spider:latest

# Push to registry
docker push your-registry/spider:latest
```

### Docker Compose Services

The `docker-compose.yml` includes:
- **MySQL Database**: Port 3306
- **Redis Cache**: Port 6379
- **Spring Boot Backend**: Port 8080
- **React Frontend**: Port 5173 (via Nginx or dev server)

### Production Deployment

```bash
# Using docker-compose for full stack
docker-compose -f docker-compose.yml up -d

# Scale services
docker-compose up -d --scale identity=3
```

## 📝 Configuration Files

### Backend Configuration

- `identity/src/main/resources/application.yml`: Main configuration
- `identity/src/main/resources/application-local.yml`: Local development overrides
- `identity/src/main/resources/i18n/`: Internationalization resources

### Frontend Configuration

- `ui/react-auth-skeleton/vite.config.js`: Vite build configuration
- `ui/react-auth-skeleton/src/config/routesConfig.jsx`: Route definitions
- `ui/react-auth-skeleton/src/config/routeAccessConfig.js`: Access control rules
- `ui/react-auth-skeleton/src/theme/theme.js`: Material-UI theme

## 🔐 Security Features

- JWT token-based authentication
- Password encryption with Spring Security
- Role-based access control
- Input validation
- SQL injection prevention (via JPA)
- CORS configuration
- HTTPS support (in production)
- OAuth 2.0 integration with Shipmnts

## 🐛 Troubleshooting

### Connection Issues

```bash
# Check if services are running
docker ps

# Check backend logs
docker-compose logs identity

# Restart services
docker-compose restart
```

### Database Issues

```bash
# Reset database
docker-compose down -v
docker-compose up -d
```

### Port Already in Use

```bash
# Change ports in docker-compose.yml or use:
docker-compose down
lsof -ti:8080 | xargs kill -9  # Kill process on port 8080
```

### Node.js Script Execution Issues

```bash
# Ensure Node.js is properly installed
node --version
npm --version

# Clear npm cache if issues persist
npm cache clean --force

# Reinstall dependencies
cd ui/react-auth-skeleton
rm -rf node_modules package-lock.json
npm install
```

## 📖 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev)
- [Material-UI Documentation](https://mui.com/material-ui/getting-started/)
- [Liquibase Documentation](https://www.liquibase.org/get-started)
- [JWT Introduction](https://jwt.io/introduction)
- [Playwright Documentation](https://playwright.dev/)

## 🤝 Contributing

1. Create a feature branch (`git checkout -b feature/amazing-feature`)
2. Commit your changes (`git commit -m 'Add amazing feature'`)
3. Push to the branch (`git push origin feature/amazing-feature`)
4. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Last Updated**: September 2026  
**Version**: 0.0.1-SNAPSHOT

For more information or questions, please open an issue in the repository.
