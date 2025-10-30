# ExpenseTracker Docker Setup

This application has been dockerized using a lean, multi-stage build approach.

## Docker Configuration

### Dockerfile Features:

- **Multi-stage build** with Maven for compilation and Amazon Corretto Alpine for runtime
- **Layered JAR extraction** for optimal Docker layer caching
- **JVM optimization flags** (`-XX:+UseContainerSupport`, `-XX:MaxRAMPercentage=75.0`)
- **Alpine-based images** for minimal size
- **ARM64/AMD64 support** for Apple Silicon and Intel Macs
- **Host networking mode** to resolve external database connectivity issues

### Image Size:

- Final image: ~1.62GB (includes JRE, application, and all dependencies)
- Base runtime: Amazon Corretto 21 Alpine (~160MB)

## Prerequisites

- Docker and Docker Compose installed
- Your `.env` file with Supabase credentials:
    - `DB_URL` - Your Supabase PostgreSQL JDBC URL
    - `DB_PASSWORD` - Your database password
    - `GEMINI_API_KEY` - Your Gemini API key

## Running the Application

### Using Docker Compose (Recommended):

```bash
# Build and start the application
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build

# View logs
docker-compose logs -f app

# Stop the application
docker-compose down
```

### Using Docker directly:

```bash
# Build the image
docker build -t expense-tracker .

# Run the container with environment variables
docker run --network host -p 8080:8080 \
  --env-file .env \
  expense-tracker
```

## Accessing the Application

Once running, the application will be available at:

- http://localhost:8080

## Troubleshooting

### Connection Issues

If you see "Network unreachable" errors, ensure your `.env` file has the correct Supabase credentials.

### Rebuilding

To rebuild from scratch:

```bash
docker-compose build --no-cache
docker-compose up
```

### View Container Logs

```bash
docker-compose logs -f app
```

## Production Deployment

For production, consider:

1. Using Docker secrets instead of `.env` files
2. Setting up health checks in docker-compose.yml
3. Using Docker volumes for persistent data (if needed)
4. Configuring restart policies
5. Using a reverse proxy (nginx) for SSL termination
