<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/bharaths44/incometer-backend">
    <img src="logo.svg" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Incometer - Expense Tracker</h3>

  <p align="center">
    A modern expense tracking application built with Spring Boot, featuring WhatsApp bot integration, OAuth2 authentication, and AI-powered transaction extraction using Google Gemini.
  </p>
</div>

## Built With

<p align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/google%20gemini-4285F4?style=for-the-badge&logo=google%20gemini&logoColor=white" alt="Google Gemini">
  <img src="https://img.shields.io/badge/Twilio-F22F46?style=for-the-badge&logo=Twilio&logoColor=white" alt="Twilio">
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white" alt="Supabase">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
  <img src="https://img.shields.io/badge/Deployed%20on-Koyeb-000000?style=for-the-badge&logo=koyeb" alt="Koyeb">
</p>

## Features

- **Dual Authentication System**: JWT-based authentication and OAuth2 with Google
- **WhatsApp Bot Integration**: Natural language transaction input via WhatsApp
- **AI-Powered Transaction Extraction**: Google Gemini AI for parsing transaction details from text
- **Comprehensive Transaction Management**: Track income and expenses with categories and payment methods
- **Analytics and Budgeting**: User statistics, budget tracking, and financial insights
- **RESTful API**: Full CRUD operations for all entities
- **Database Migrations**: Liquibase-managed schema changes
- **Docker Support**: Containerized deployment with multi-stage builds

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.5.6
- **Database**: PostgreSQL (production) / H2 (testing)
- **Authentication**: JWT (jjwt 0.11.5) + OAuth2 (Google)
- **AI Integration**: Google Gemini API
- **Build Tool**: Maven
- **Containerization**: Docker with Amazon Corretto 21 Alpine
- **ORM**: Spring Data JPA with Hibernate
- **Testing**: JUnit 5, Mockito
- **Documentation**: SpringDoc OpenAPI (Swagger)

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+ (for local development)
- Docker and Docker Compose (for containerized deployment)
- Google Cloud account (for Gemini API and OAuth2)

## Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ExpenseTracker
   ```

2. **Set up environment variables**
   
   Create a `.env` file in the root directory with the following variables:
   ```env
   DB_URL=jdbc:postgresql://localhost:5432/incometer
   DB_PASSWORD=your_postgres_password
   GEMINI_API_KEY=your_google_gemini_api_key
   GOOGLE_CLIENT_ID=your_google_oauth_client_id
   GOOGLE_CLIENT_SECRET=your_google_oauth_client_secret
   ```

3. **Database Setup**
   
   - Create a PostgreSQL database named `incometer`
   - The application will automatically run Liquibase migrations on startup

4. **Build the application**
   ```bash
   mvn clean install
   ```

## 🚀 Running the Application

### Local Development
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Docker Deployment
```bash
docker-compose up --build
```

Or manually:
```bash
docker build -t incometer-app .
docker run --network=host --env-file .env incometer-app
```

## 📖 API Documentation

Once the application is running, access the Swagger UI at:
`http://localhost:8080/swagger-ui/index.html`

### Key Endpoints

- **Authentication**: `/api/v1/auth/register`, `/api/v1/auth/authenticate`
- **OAuth2**: `/oauth2/authorize/google`
- **Transactions**: `/api/v1/transactions`
- **Categories**: `/api/v1/categories`
- **Budgets**: `/api/v1/budgets`
- **Analytics**: `/api/v1/analytics`
- **WhatsApp Webhook**: `/api/v1/whatsapp/webhook`

## 🧪 Testing

Run the test suite:
```bash
mvn test
```

Tests use H2 in-memory database with PostgreSQL compatibility mode.

## 🏗 Project Structure

```
com.bharath.incometer/
├── config/          # Security, CORS, JWT, OAuth2 configurations
├── controllers/     # REST API endpoints
├── entities/        # JPA entities with DTOs/
├── enums/           # TransactionType, Role, AuthProvider, etc.
├── service/         # Business logic with bot/ subdirectory
├── repository/      # Spring Data JPA repositories
├── utils/           # Utility classes for AI extraction, fuzzy matching
└── exceptions/      # Custom exception handlers
```

## Authentication

### JWT Authentication
- Register: `POST /api/v1/auth/register`
- Login: `POST /api/v1/auth/authenticate`

### OAuth2 Google Login
- Authorize: `GET /oauth2/authorize/google`
- Callback: `GET /oauth2/callback/google`

## WhatsApp Bot

The application includes a WhatsApp bot for natural language transaction input:

- **Registration**: Send `register <name>` to register your phone number
- **Add Transaction**: Send natural language messages like "expense 50 food cash"
- **Confirmation**: Bot confirms transactions and handles category matching

## Database Migrations

Schema changes are managed through Liquibase:

- **Baseline**: `src/main/resources/db/changelog/generated-initial.yaml`
- **Master Changelog**: `src/main/resources/db/changelog/db.changelog-master.xml`
- **New Migrations**: Add new YAML files in `db/changelog/` and include in master XML

### Adding New Migrations

1. Create `src/main/resources/db/changelog/vX-description.yaml`
2. Define changeset with unique ID and author
3. Include in `db.changelog-master.xml`

### Development Guidelines

- Follow the existing package structure
- Use DTOs for all API responses
- Add validation annotations on DTOs
- Write unit tests for new services
- Update Liquibase migrations for schema changes
- Ensure all endpoints are secured appropriately


## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
