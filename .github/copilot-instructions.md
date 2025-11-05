# Incometer - Expense Tracker AI Development Guide

## Project Overview

Spring Boot 3.5.6 expense tracking application with WhatsApp bot integration, OAuth2 authentication, and AI-powered
transaction extraction using Google Gemini.

## Core Architecture

### Tech Stack

- **Java 21** with Lombok for boilerplate reduction
- **Spring Boot 3.5.6**: Web, Security, JPA, OAuth2 Client/Resource Server
- **PostgreSQL** (production) / H2 (tests via `application-test.yml`)
- **JWT Authentication** (jjwt 0.11.5) + OAuth2 (Google)
- **Google Gemini AI** for NLP transaction extraction
- **Docker**: Multi-stage build with Amazon Corretto 21 Alpine

### Package Structure

```
com.bharath.incometer/
├── config/          # Security, CORS, JWT, OAuth2 handlers
├── controllers/     # REST endpoints (Auth, Transaction, WhatsApp, etc.)
├── entities/        # JPA entities with DTOs/ subdirectory
├── enums/          # TransactionType, Role, AuthProvider, etc.
├── service/        # Business logic with bot/ subdirectory
├── repository/     # Spring Data JPA repositories
└── utils/          # CategoryMatchingFuzzy, TransactionExtractionResult
```

## Key Patterns & Conventions

### 1. Entity-DTO Separation

- All entities have corresponding Request/Response DTOs in `entities/DTOs/`
- DTOs use Java records: `TransactionRequestDTO`, `TransactionResponseDTO`
- Services convert between entities and DTOs (e.g., `toDTO()` method in `TransactionService`)
- Validation uses Jakarta annotations on DTOs (`@NotNull`, `@Positive`)

### 2. Authentication Flow

**Dual auth system:**

- **JWT**: Standard username/password via `/api/v1/auth/register` and `/api/v1/auth/authenticate`
- **OAuth2**: Google OAuth via `/oauth2/authorize/google` → `/oauth2/callback/*`
    - Custom handlers: `OAuth2AuthenticationSuccessHandler`, `OAuth2AuthenticationFailureHandler`
    - Cookie-based request repository: `HttpCookieOAuth2AuthorizationRequestRepository`
    - JWT issued after OAuth success, frontend receives token via redirect

**Security whitelist** in `SecurityConfig.java`:

```java
"/api/v1/auth/**","/oauth2/**","/swagger-ui/**"
```

### 3. WhatsApp Bot Integration

**Message flow**: `WhatsAppController` → `WhatsAppMessageProcessor` → `TransactionMessageHandler` → `NLPService`

- **Registration**: `register <name>` via phone number
- **Transactions**: Natural language input → Gemini AI extraction → fuzzy category matching
- **Pending confirmations**: Stored in `Map<String, PendingCategory>` for "yes/no" responses

**Example**: "expense 50 food cash" → Gemini extracts structured data → matches "food" to existing category → creates
transaction

Key classes: `GeminiExtractionService`, `CategoryMatchingFuzzy`, `PaymentMethodFormatter`

### 4. Transaction Management

- **@Transactional** used consistently in service layer (read-only for queries)
- Lazy-loaded relationships (`FetchType.LAZY`) on `Transaction.user`, `Transaction.category`,
  `Transaction.paymentMethod`
- Custom `equals()`/`hashCode()` handling for Hibernate proxies in entities

### 5. Environment Configuration

**Dotenv pattern** in `IncometerApplication.java` loads `.env` before Spring context:

```java
System.setProperty("DB_URL",dotenv.get("DB_URL"));
	System.

setProperty("GEMINI_API_KEY",dotenv.get("GEMINI_API_KEY"));
```

Required vars: `DB_URL`, `DB_PASSWORD`, `GEMINI_API_KEY`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`

## Development Workflows

### Running Locally

```bash
# Ensure .env file exists with all required variables
mvn clean spring-boot:run

# Tests use H2 in-memory database (see application-test.yml)
mvn test
```

### Docker Deployment

```bash
# Host networking mode for Supabase/external DB connectivity
docker-compose up --build

# Manual build
docker build -t incometer-app .
docker run --network=host --env-file .env incometer-app
```

### Testing Strategy

- **MockitoExtension** for unit tests: `TransactionMessageHandlerTest`, `NLPServiceTest`
- Services mocked, not repositories (repository tests would require @DataJpaTest)
- Test profile uses H2 with PostgreSQL compatibility mode

## Integration Points

### External Dependencies

1. **Google Gemini API**: Transaction extraction from natural language

    - Client initialized in `GeminiExtractionService` constructor
    - Prompt engineering includes today's date for relative date parsing
    - Returns JSON with amount, category, payment_method, date

2. **PostgreSQL/Supabase**: Primary database

    - JPA `ddl-auto: create-drop` (development only!)
    - Schema managed by Hibernate, see `schema.sql`/`data.sql` for reference

3. **OAuth2 Provider**: Google login
    - Redirect URI: `http://localhost:8080/oauth2/callback/google`
    - Frontend callback: configured in `application.yml` as `authorizedRedirectUris`

### Cross-Component Communication

- Controllers → Services (dependency injection via constructor)
- Services → Repositories (Spring Data JPA)
- WhatsApp bot → Core services reuse transaction/category logic
- JWT filter (`JwtAuthenticationFilter`) intercepts all non-whitelisted requests

## Project-Specific Gotchas

1. **Lombok annotations**: Use on entities but configure annotation processors in pom.xml
2. **Hibernate proxies**: Entity `equals()`/`hashCode()` must check proxy classes (see `Transaction.java`)
3. **UUID for Users**: Auto-generated via `@GeneratedValue(strategy = GenerationType.UUID)`
4. **Phone number normalization**: Strip "whatsapp:" prefix in `WhatsAppController`
5. **Gemini API key**: Must be set before Spring context loads (in main method)
6. **CORS**: Configured in `CorsConfig` for frontend integration (likely React at localhost:3000)

## When Adding Features

- **New entities**: Add to `entities/`, create DTOs in `entities/DTOs/`, add repository in `repository/`
- **New endpoints**: Add controller, ensure security rules in `SecurityConfig`
- **WhatsApp commands**: Extend `TransactionMessageHandler` or `NLPService`
- **Auth changes**: Review both JWT and OAuth2 flows (dual system)
- **Database changes**: Consider migration strategy (currently using create-drop!)
