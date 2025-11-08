# Authentication Files Explanation

This document explains the authentication-related files in the Incometer Expense Tracker project.

## Config Files

### SecurityConfig.java
This is the main security configuration class for Spring Security. It:
- Disables CSRF for stateless API
- Configures CORS with a custom source
- Sets up whitelisted URLs for auth endpoints and Swagger
- Uses stateless session management
- Adds JWT filter before username/password filter
- Configures OAuth2 login with custom handlers and cookie-based request repository
- Enables OAuth2 resource server with JWT

### JwtAuthenticationFilter.java
A custom filter that intercepts requests to authenticate users via JWT tokens stored in cookies. It:
- Extracts the "token" cookie from the request
- Validates the JWT and sets the authentication context if valid
- Uses UserDetailsService to load user details

### OAuth2AuthenticationSuccessHandler.java
Handles successful OAuth2 authentication. It:
- Determines the target redirect URL from cookie or config
- Generates a JWT token for the authenticated user
- Redirects to the frontend with the token as a query parameter
- Clears authentication attributes and cookies

### OAuth2AuthenticationFailureHandler.java
Handles OAuth2 authentication failures. It:
- Retrieves the redirect URI from cookie
- Appends error message to the redirect URL
- Redirects to the frontend with error details
- Cleans up cookies

### HttpCookieOAuth2AuthorizationRequestRepository.java
Manages OAuth2 authorization requests using HTTP cookies. It:
- Serializes/deserializes authorization requests to/from cookies
- Stores redirect URI in a separate cookie
- Handles cookie expiration and cleanup

## Models

### AuthenticationResponse.java
A simple DTO for authentication responses containing access and refresh tokens.

### LoginRequest.java
DTO for login requests with email and password.

### RefreshRequest.java
DTO for token refresh requests containing the refresh token.

### RegisterRequest.java
DTO for user registration with name, email, and password.

### user/GoogleOAuth2UserInfo.java
Implements OAuth2UserInfo for Google OAuth2 provider, extracting id, name, and email from attributes.

### user/OAuth2UserInfo.java
Abstract class defining the interface for OAuth2 user information extraction.

### user/OAuth2UserInfoFactory.java
Factory class to create appropriate OAuth2UserInfo instances based on the registration ID (currently only Google).

### user/UserPrincipal.java
Implements UserDetails, OAuth2User, and OidcUser. Represents the authenticated user principal with:
- User details for Spring Security
- OAuth2 attributes
- Custom fields like UUID and name

## Services

### AuthService.java
Simple service to get authenticated user from JWT token.

### AuthenticationService.java
Handles user authentication and registration for JWT-based auth. It:
- Registers new users with password encoding
- Authenticates users via email/password
- Generates and sets JWT cookies
- Handles token refresh

### CustomOAuth2UserService.java
Extends DefaultOAuth2UserService to handle OAuth2 user loading. It:
- Processes OAuth2 user info
- Registers new users or updates existing ones
- Creates UserPrincipal instances

### CustomOidcUserService.java
Similar to CustomOAuth2UserService but for OIDC (OpenID Connect) users.

### JwtService.java
Handles JWT token operations:
- Token generation with custom claims (UUID, name)
- Token validation and parsing
- Refresh token generation
- Claim extraction

### RegistrationService.java
Handles WhatsApp-based user registration by phone number.

### UserService.java
Manages user CRUD operations, including getting current user, updating profile, etc.

## Entities

### Users.java
JPA entity representing users with fields for authentication:
- UUID id, name, email, phone, password, role, provider
- Implements UserDetails for Spring Security
- Custom equals/hashCode for Hibernate proxies

## Enums

### AuthProvider.java
Enum for authentication providers: local, google.

### Role.java
Enum for user roles: USER, ADMIN.

# How OIDC and OAuth2 Work in Conjunction

OpenID Connect (OIDC) is built on top of OAuth 2.0, extending it with identity verification capabilities. Here's how they work together:

## OAuth 2.0 Basics
OAuth 2.0 is an authorization framework that allows third-party applications to obtain limited access to a user's resources without exposing credentials. It uses tokens (access tokens) to grant permissions.

## OIDC Extension
OIDC adds an identity layer to OAuth 2.0. While OAuth focuses on "what can you do?", OIDC answers "who are you?". It introduces the **ID Token** (a JWT) alongside OAuth's access token.

## Combined Flow
1. **Authorization Request**: Client redirects user to provider (e.g., Google) with scopes like `openid profile email`.
2. **User Authentication**: Provider authenticates the user.
3. **Consent**: User grants permissions.
4. **Token Response**: Provider returns:
   - **Access Token**: For API access (OAuth).
   - **ID Token**: Contains user identity claims (OIDC).
   - **Refresh Token**: For renewing access (optional).
5. **User Info Endpoint**: Client can fetch additional user info using the access token.

## In Your Project
- `CustomOAuth2UserService`: Handles general OAuth2 flows, extracting user attributes.
- `CustomOidcUserService`: Specifically for OIDC, processes ID tokens and claims for identity verification.
- Both create `UserPrincipal` instances, integrating OAuth2 attributes and OIDC identity info.
- The system supports dual auth: JWT for sessions, OAuth2/OIDC for login.

This allows seamless authentication via providers like Google, combining authorization (OAuth2) with identity (OIDC).

# Authentication Flow Diagrams

## JWT-Based Authentication Flow

1. **User Registration/Login Request** → `AuthController` (REST endpoint)
2. **Validation and Processing** → `AuthenticationService.register()` or `authenticate()`
3. **Password Encoding** → `PasswordEncoder` (configured in `ApplicationConfig`)
4. **Token Generation** → `JwtService.generateToken()` and `generateRefreshToken()`
5. **Response with Tokens** → `AuthenticationResponse` (DTO)
6. **Cookie Setting** → HTTP response with "token" cookie
7. **Subsequent Requests** → `JwtAuthenticationFilter.doFilterInternal()`
8. **Token Validation** → `JwtService.isTokenValid()` and `extractUsername()`
9. **User Loading** → `UserDetailsService.loadUserByUsername()` (implemented by `UserService`)
10. **Authentication Context** → Sets `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`
11. **Token Refresh** → `AuthenticationService.refresh()` → `JwtService` → new tokens

## OAuth2/OIDC Authentication Flow

1. **OAuth Initiation** → User redirected to `/oauth2/authorize/google` (configured in `SecurityConfig`)
2. **Authorization Request Storage** → `HttpCookieOAuth2AuthorizationRequestRepository.saveAuthorizationRequest()`
3. **Provider Authentication** → External OAuth2/OIDC provider (e.g., Google)
4. **Callback Handling** → Redirect to `/oauth2/callback/google`
5. **Success Handler** → `OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess()`
6. **User Processing** → `CustomOAuth2UserService.loadUser()` or `CustomOidcUserService.loadUser()`
7. **User Info Extraction** → `OAuth2UserInfoFactory.getOAuth2UserInfo()` → `GoogleOAuth2UserInfo`
8. **User Registration/Update** → Checks/creates `Users` entity via `UsersRepository`
9. **Principal Creation** → `UserPrincipal.create()` with attributes
10. **JWT Generation** → `JwtService.generateToken(UserPrincipal)`
11. **Redirect with Token** → Frontend URL with `?token=...`
12. **Frontend Stores Token** → Sets cookie or local storage
13. **API Requests** → Same as JWT flow: `JwtAuthenticationFilter` → `JwtService` → `UserService`

## Shared Components
- **User Entity** → `Users.java` (persisted via `UsersRepository`)
- **Security Context** → Managed by `SecurityConfig` and Spring Security
- **CORS Handling** → `CorsConfig.java`
- **Error Handling** → Custom exceptions in `exceptions/` package

This flow ensures dual authentication support, with OAuth2/OIDC for initial login and JWT for session management.
