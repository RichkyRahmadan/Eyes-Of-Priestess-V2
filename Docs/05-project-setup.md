# EyesOfPriestess — Project Setup Guide

> **Version:** 1.0  
> **Platform:** Website (Web Application)  
> **Design System:** Warm Editorial (Cream Canvas + Coral Accent)

---

## 1. Prerequisites

| Tool | Version | Purpose |
|---|---|---|
| Java | 21 (LTS) | Quarkus backend |
| Maven | 3.9+ | Java dependency management |
| Node.js | 20+ (LTS) | SvelteKit frontend |
| pnpm | 9+ | Package manager (recommended) |
| Docker | 24+ | Containerization |
| Docker Compose | 2.20+ | Multi-container orchestration |
| PostgreSQL CLI | 15+ | Database management (optional) |
| Redis CLI | 7+ | Cache inspection (optional) |

---

## 2. Repository Initialization

```bash
# Create monorepo directory
mkdir eyesofpriestess && cd eyesofpriestess
git init

# Create directory structure
mkdir -p backend/{gateway,auth-service,wallet-service,room-service,chat-service,dispute-service}
mkdir -p frontend/src/{routes,lib/{components,stores,api,websocket,types,utils},static/{fonts,images}}
mkdir -p docs
```

---

## 3. Backend Setup (Quarkus)

### 3.1 Generate Gateway Service

```bash
cd backend/gateway

# Using Quarkus CLI
quarkus create app com.eyesofpriestess:gateway:1.0.0-SNAPSHOT   --extensions="resteasy-reactive,rest-client-reactive-jackson,smallrye-jwt,smallrye-openapi,vertx"

# Or using Maven
mvn io.quarkus.platform:quarkus-maven-plugin:3.12.0:create   -DprojectGroupId=com.eyesofpriestess   -DprojectArtifactId=gateway   -Dextensions="resteasy-reactive,rest-client-reactive-jackson,smallrye-jwt,smallrye-openapi,vertx"
```

**Required extensions for Gateway:**
- `resteasy-reactive` — REST endpoint handler
- `rest-client-reactive-jackson` — HTTP client to downstream services
- `smallrye-jwt` — JWT validation
- `smallrye-openapi` — OpenAPI/Swagger docs
- `vertx` — WebSocket proxy support
- `micrometer-registry-prometheus` — Metrics

### 3.2 Generate Auth Service

```bash
cd backend/auth-service

quarkus create app com.eyesofpriestess:auth-service:1.0.0-SNAPSHOT   --extensions="resteasy-reactive,hibernate-reactive-panache,reactive-pg-client,smallrye-jwt,smallrye-context-propagation,argon2,smallrye-reactive-messaging-rabbitmq"
```

**Required extensions:**
- `resteasy-reactive` — REST endpoints
- `hibernate-reactive-panache` — Reactive ORM
- `reactive-pg-client` — PostgreSQL reactive driver
- `smallrye-jwt` — JWT generation & validation
- `smallrye-context-propagation` — Context across reactive chains
- `argon2` (via dependency) — Password hashing
- `smallrye-reactive-messaging-rabbitmq` — Event publishing

### 3.3 Generate Wallet Service

```bash
cd backend/wallet-service

quarkus create app com.eyesofpriestess:wallet-service:1.0.0-SNAPSHOT   --extensions="resteasy-reactive,hibernate-reactive-panache,reactive-pg-client,smallrye-reactive-messaging-rabbitmq,smallrye-context-propagation"
```

### 3.4 Generate Room Service

```bash
cd backend/room-service

quarkus create app com.eyesofpriestess:room-service:1.0.0-SNAPSHOT   --extensions="resteasy-reactive,hibernate-reactive-panache,reactive-pg-client,smallrye-reactive-messaging-rabbitmq,smallrye-context-propagation"
```

### 3.5 Generate Chat Service

```bash
cd backend/chat-service

quarkus create app com.eyesofpriestess:chat-service:1.0.0-SNAPSHOT   --extensions="resteasy-reactive,hibernate-reactive-panache,reactive-pg-client,vertx,smallrye-reactive-messaging-rabbitmq"
```

### 3.6 Generate Dispute Service

```bash
cd backend/dispute-service

quarkus create app com.eyesofpriestess:dispute-service:1.0.0-SNAPSHOT   --extensions="resteasy-reactive,hibernate-reactive-panache,reactive-pg-client,smallrye-reactive-messaging-rabbitmq"
```

### 3.7 Common `pom.xml` Dependencies (add to all services)

```xml
<!-- In each service pom.xml, add under <dependencies> -->

<!-- Validation -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-validator</artifactId>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
</dependency>

<!-- Health Checks -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-health</artifactId>
</dependency>

<!-- Config -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-config-yaml</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>

<!-- Argon2 (add to auth-service only) -->
<dependency>
    <groupId>de.mkammerer</groupId>
    <artifactId>argon2-jvm</artifactId>
    <version>2.11</version>
</dependency>

<!-- Redis Client (add to auth-service and gateway) -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-redis-client</artifactId>
</dependency>
```

### 3.8 Application Properties Template (per service)

```yaml
# src/main/resources/application.yaml
quarkus:
  application:
    name: auth-service

  http:
    port: 8081
    cors:
      origins: "http://localhost:3000"
      methods: "GET,POST,PUT,DELETE,OPTIONS"
      headers: "accept,authorization,content-type,x-requested-with"

  datasource:
    reactive:
      url: postgresql://localhost:5432/eyesofpriestess
    username: eop
    password: eop_dev

  hibernate-orm:
    database:
      generation: none
    log:
      sql: true

  hibernate-orm-panache:
    active: true

  smallrye-jwt:
    sign:
      key:
        location: privateKey.pem
    verify:
      key:
        location: publicKey.pem
    issuer: https://eyesofpriestess.id
    expiration:
      default: 900  # 15 minutes

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

  redis:
    hosts: redis://localhost:6379

  log:
    level: INFO
    category:
      "io.quarkus": INFO
      "com.eyesofpriestess": DEBUG
```

### 3.9 Generate JWT Keys

```bash
# In auth-service/src/main/resources
openssl genrsa -out privateKey.pem 2048
openssl rsa -in privateKey.pem -pubout -out publicKey.pem

# Copy publicKey.pem to gateway/src/main/resources/
```

---

## 4. Frontend Setup (SvelteKit)

### 4.1 Initialize Project

```bash
cd frontend

# Create SvelteKit project
npm create svelte@latest .
# Select: Skeleton project, Yes TypeScript, Yes ESLint, Yes Prettier, Yes Playwright, No Vitest

# Install dependencies
pnpm install

# Install TailwindCSS
pnpm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p

# Install shadcn-svelte dependencies
pnpm install -D clsx tailwind-merge
pnpm install class-variance-authority
pnpm install bits-ui
pnpm install formsnap
pnpm install cmdk-sv
pnpm install mode-watcher

# Install additional packages
pnpm install lucide-svelte
pnpm install zod
pnpm install date-fns

# Install dev dependencies
pnpm install -D @tailwindcss/typography tailwindcss-animate
```

### 4.2 Tailwind Config

Copy the `tailwind.config.js` from `04-frontend-spec.md` Section 2.

### 4.3 Global CSS (`src/app.css`)

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --canvas: 250 249 245;
    --surface-card: 239 233 222;
    --surface-dark: 24 23 21;
    --primary: 204 120 92;
    --primary-active: 169 88 62;
    --ink: 20 20 19;
    --body: 61 61 58;
    --muted: 108 106 100;
    --on-primary: 255 255 255;
    --on-dark: 250 249 245;
    --hairline: 230 223 216;
    --success: 93 184 114;
    --warning: 212 160 23;
    --error: 198 69 69;
  }

  * {
    @apply border-hairline;
  }

  body {
    @apply bg-canvas text-ink font-sans antialiased;
  }

  h1, h2, h3 {
    @apply font-display;
  }

  /* Custom scrollbar for cream canvas */
  ::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }
  ::-webkit-scrollbar-track {
    background: transparent;
  }
  ::-webkit-scrollbar-thumb {
    background: theme('colors.hairline');
    border-radius: 4px;
  }
  ::-webkit-scrollbar-thumb:hover {
    background: theme('colors.muted-soft');
  }
}
```

### 4.4 Font Loading (`src/app.html`)

```html
<!DOCTYPE html>
<html lang="id">
  <head>
    <meta charset="utf-8" />
    <link rel="icon" href="%sveltekit.assets%/favicon.png" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@400;500;600&family=Inter:wght@400;500;600&family=JetBrains+Mono:wght@400&display=swap" rel="stylesheet">

    %sveltekit.head%
  </head>
  <body data-sveltekit-preload-data="hover">
    <div style="display: contents">%sveltekit.body%</div>
  </body>
</html>
```

### 4.5 Svelte Config (`svelte.config.js`)

```javascript
import adapter from "@sveltejs/adapter-auto";
import { vitePreprocess } from "@sveltejs/vite-plugin-svelte";

/** @type {import('@sveltejs/kit').Config} */
const config = {
  preprocess: vitePreprocess(),
  kit: {
    adapter: adapter(),
    alias: {
      "$lib": "./src/lib",
      "$lib/*": "./src/lib/*",
    },
  },
};

export default config;
```

### 4.6 Vite Config (`vite.config.ts`)

```typescript
import { sveltekit } from "@sveltejs/kit/vite";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [sveltekit()],
  server: {
    port: 3000,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/ws": {
        target: "ws://localhost:8080",
        ws: true,
      },
    },
  },
});
```

### 4.7 TypeScript Config (`tsconfig.json`)

Ensure paths are configured:
```json
{
  "extends": "./.svelte-kit/tsconfig.json",
  "compilerOptions": {
    "strict": true,
    "resolveJsonModule": true,
    "allowJs": true,
    "checkJs": true,
    "esModuleInterop": true,
    "forceConsistentCasingInFileNames": true,
    "skipLibCheck": true,
    "sourceMap": true,
    "moduleResolution": "bundler"
  }
}
```

---

## 5. Docker Compose (Development)

Create `docker-compose.yml` at project root:

```yaml
version: "3.8"

services:
  postgres:
    image: postgres:15-alpine
    container_name: eop-postgres
    environment:
      POSTGRES_DB: eyesofpriestess
      POSTGRES_USER: eop
      POSTGRES_PASSWORD: eop_dev
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U eop -d eyesofpriestess"]
      interval: 5s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: eop-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes

  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: eop-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

  minio:
    image: minio/minio:latest
    container_name: eop-minio
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    volumes:
      - minio_data:/data
    command: server /data --console-address ":9001"

volumes:
  postgres_data:
  redis_data:
  rabbitmq_data:
  minio_data:
```

---

## 6. Database Initialization

Create `init-scripts/001-init-schemas.sql`:

```sql
-- Create schemas
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS wallet;
CREATE SCHEMA IF NOT EXISTS room;
CREATE SCHEMA IF NOT EXISTS chat;
CREATE SCHEMA IF NOT EXISTS dispute;

-- Create admin user (password: Admin123! — change in production)
-- Run after tables are created via migration tools
```

**Recommended:** Use Flyway or Liquibase for migration management. Add to each Quarkus service:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-flyway</artifactId>
</dependency>
```

---

## 7. Environment Variables

### 7.1 Backend (.env template)

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=eyesofpriestess
DB_USER=eop
DB_PASSWORD=eop_dev

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASS=guest

# JWT
JWT_ISSUER=https://eyesofpriestess.id
JWT_EXPIRATION=900

# Payment Gateway (Midtrans Sandbox)
MIDTRANS_SERVER_KEY=your-sandbox-server-key
MIDTRANS_CLIENT_KEY=your-sandbox-client-key
MIDTRANS_IS_PRODUCTION=false

# Or Xendit
XENDIT_API_KEY=your-xendit-api-key
XENDIT_CALLBACK_TOKEN=your-callback-token

# MinIO / S3
S3_ENDPOINT=http://localhost:9000
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin
S3_BUCKET=eyesofpriestess
```

### 7.2 Frontend (.env template)

```bash
PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
PUBLIC_WS_URL=ws://localhost:8080/ws/chat
```

---

## 8. Running the Project

### 8.1 Start Infrastructure

```bash
# From project root
docker-compose up -d

# Verify
docker-compose ps
```

### 8.2 Start Backend Services

```bash
# Terminal 1: Auth Service
cd backend/auth-service
mvn quarkus:dev

# Terminal 2: Wallet Service
cd backend/wallet-service
mvn quarkus:dev

# Terminal 3: Room Service
cd backend/room-service
mvn quarkus:dev

# Terminal 4: Chat Service
cd backend/chat-service
mvn quarkus:dev

# Terminal 5: Dispute Service
cd backend/dispute-service
mvn quarkus:dev

# Terminal 6: Gateway
cd backend/gateway
mvn quarkus:dev
```

### 8.3 Start Frontend

```bash
cd frontend
pnpm dev
```

### 8.4 Access Points

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Auth Service (direct) | http://localhost:8081 |
| Wallet Service (direct) | http://localhost:8082 |
| Room Service (direct) | http://localhost:8083 |
| Chat Service (direct) | http://localhost:8084 |
| Dispute Service (direct) | http://localhost:8085 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |
| MinIO Console | http://localhost:9001 (minioadmin/minioadmin) |

---

## 9. Testing Setup

### 9.1 Backend Tests

```bash
# Run tests for a service
cd backend/auth-service
mvn test

# With coverage
mvn verify
```

### 9.2 Frontend Tests

```bash
cd frontend

# Unit tests (if using Vitest)
pnpm test

# E2E tests (Playwright)
pnpm test:e2e
```

---

## 10. Build for Production

### 10.1 Backend

```bash
# Build native executable (requires GraalVM)
cd backend/auth-service
mvn package -Pnative -DskipTests

# Or JVM mode
mvn package -DskipTests
```

### 10.2 Frontend

```bash
cd frontend
pnpm build
```

### 10.3 Docker Production

```bash
# Build all services
docker-compose -f docker-compose.prod.yml build

# Deploy
docker-compose -f docker-compose.prod.yml up -d
```

---

## 11. Troubleshooting

| Issue | Solution |
|---|---|
| `Connection refused` to PostgreSQL | Pastikan Docker container running: `docker-compose up -d postgres` |
| Port already in use | Ganti port di `application.yaml` atau hentikan service yang menggunakan port |
| JWT validation fails | Periksa public/private key pair, pastikan algoritma RS256 |
| CORS errors | Pastikan `quarkus.http.cors.origins` mencakup `http://localhost:3000` |
| WebSocket tidak connect | Periksa proxy config di Vite, pastikan token valid |
| Hibernate tidak generate schema | Gunakan Flyway/Liquibase, jangan andalkan `drop-and-create` di production |
| Redis connection timeout | Pastikan Redis container running dan tidak ada firewall block |

---

*End of Project Setup Guide*
