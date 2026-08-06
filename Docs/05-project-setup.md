# EyesOfPriestess — Project Setup Guide
## The Attunement Ritual: Development Environment Setup

**Version:** 1.0  
**Prerequisites:** Docker, Docker Compose, Java 21, Node.js 20+, Maven 3.9+

---

## 1. Prerequisites Installation

### 1.1 Java 21 (LTS)
```bash
# macOS (Homebrew)
brew install openjdk@21

# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# Verify
java -version  # Should show Java 21
```

### 1.2 Maven 3.9+
```bash
# macOS
brew install maven

# Ubuntu/Debian
sudo apt install maven

# Verify
mvn -version
```

### 1.3 Node.js 20+ & npm
```bash
# Using nvm (recommended)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 20
nvm use 20

# Verify
node -v  # v20.x.x
npm -v
```

### 1.4 Docker & Docker Compose
```bash
# macOS
brew install docker docker-compose

# Ubuntu/Debian
sudo apt install docker.io docker-compose

# Verify
docker --version
docker-compose --version
```

---

## 2. Project Initialization — The Founding

### 2.1 Create Project Structure
```bash
mkdir eyesofpriestess && cd eyesofpriestess
git init

# Create directory structure
mkdir -p backend/{the-veil,seal-service,vault-service,covenant-service,communion-service,judgment-service}
mkdir -p frontend
mkdir -p docs
```

### 2.2 Backend: Initialize Quarkus Sanctums

For each sanctum, run:

```bash
# The Veil (Gateway)
cd backend/the-veil
mvn io.quarkus.platform:quarkus-maven-plugin:3.8.0:create   -DprojectGroupId=com.eyesofpriestess   -DprojectArtifactId=the-veil   -Dextensions="resteasy-reactive,vertx,smallrye-jwt,redis-client"

# Seal Sanctum (Auth)
cd backend/seal-service
mvn io.quarkus.platform:quarkus-maven-plugin:3.8.0:create   -DprojectGroupId=com.eyesofpriestess   -DprojectArtifactId=seal-service   -Dextensions="resteasy-reactive,hibernate-reactive-panache,postgresql-reactive,smallrye-jwt,bcrypt,redis-client,messaging-rabbitmq"

# Vault Sanctum (Wallet)
cd backend/vault-service
mvn io.quarkus.platform:quarkus-maven-plugin:3.8.0:create   -DprojectGroupId=com.eyesofpriestess   -DprojectArtifactId=vault-service   -Dextensions="resteasy-reactive,hibernate-reactive-panache,postgresql-reactive,redis-client,messaging-rabbitmq"

# Covenant Sanctum (Room/Escrow)
cd backend/covenant-service
mvn io.quarkus.platform:quarkus-maven-plugin:3.8.0:create   -DprojectGroupId=com.eyesofpriestess   -DprojectArtifactId=covenant-service   -Dextensions="resteasy-reactive,hibernate-reactive-panache,postgresql-reactive,redis-client,messaging-rabbitmq,scheduler"

# Communion Sanctum (Chat)
cd backend/communion-service
mvn io.quarkus.platform:quarkus-maven-plugin:3.8.0:create   -DprojectGroupId=com.eyesofpriestess   -DprojectArtifactId=communion-service   -Dextensions="resteasy-reactive,hibernate-reactive-panache,postgresql-reactive,websockets"

# Judgment Sanctum (Dispute)
cd backend/judgment-service
mvn io.quarkus.platform:quarkus-maven-plugin:3.8.0:create   -DprojectGroupId=com.eyesofpriestess   -DprojectArtifactId=judgment-service   -Dextensions="resteasy-reactive,hibernate-reactive-panache,postgresql-reactive,messaging-rabbitmq"
```

### 2.3 Frontend: Initialize The Sanctum (SvelteKit)
```bash
cd frontend

# Create SvelteKit project
npm create svelte@latest .
# Choose: Skeleton project, TypeScript, ESLint, Prettier, Playwright, Vitest

# Install dependencies
npm install

# Install additional packages
npm install -D tailwindcss postcss autoprefixer
npm install -D @sveltejs/adapter-node
npm install lucide-svelte svelte-sonner formsnap zod date-fns chart.js svelte-chartjs
npm install -D @types/chart.js

# Initialize Tailwind
npx tailwindcss init -p

# Initialize shadcn-svelte
npx shadcn-svelte@latest init
```

### 2.4 Configure Tailwind — The Priestess's Palette
```javascript
// frontend/tailwind.config.js
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{html,js,svelte,ts}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f5f3ff', 100: '#ede9fe', 200: '#ddd6fe',
          300: '#c4b5fd', 400: '#a78bfa', 500: '#8b5cf6',
          600: '#7c3aed', 700: '#6d28d9', 800: '#5b21b6', 900: '#4c1d95',
        },
        gold: { 400: '#fbbf24', 500: '#f59e0b', 600: '#d97706' },
        void: { 700: '#2d1b4e', 800: '#1a1025', 900: '#0f0a1a' },
        canvas: { DEFAULT: '#faf8ff', soft: '#f0ecfa', softer: '#f5f3ff' }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif']
      }
    }
  },
  plugins: []
};
```

### 2.5 Configure SvelteKit
```javascript
// frontend/svelte.config.js
import adapter from '@sveltejs/adapter-node';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

const config = {
  preprocess: vitePreprocess(),
  kit: { adapter: adapter({ out: 'build' }) }
};

export default config;
```

---

## 3. Docker Compose — The Sanctum Network

Create `docker-compose.yml` at project root:

```yaml
version: '3.8'

services:
  # The Archive (PostgreSQL)
  postgres:
    image: postgres:15-alpine
    container_name: eop-postgres
    environment:
      POSTGRES_DB: eyesofpriestess
      POSTGRES_USER: priestess
      POSTGRES_PASSWORD: originium_seal
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backend/init-scripts:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U priestess"]
      interval: 5s
      timeout: 5s
      retries: 5

  # The Crystal (Redis)
  redis:
    image: redis:7-alpine
    container_name: eop-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes

  # The Aether (RabbitMQ)
  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: eop-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: priestess
      RABBITMQ_DEFAULT_PASS: originium_seal
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

  # The Five Sanctums
  seal-service:
    build:
      context: ./backend/seal-service
      dockerfile: src/main/docker/Dockerfile.jvm
    container_name: eop-seal
    environment:
      QUARKUS_DATASOURCE_REACTIVE_URL: postgresql://postgres:5432/eyesofpriestess
      QUARKUS_DATASOURCE_USERNAME: priestess
      QUARKUS_DATASOURCE_PASSWORD: originium_seal
      QUARKUS_REDIS_HOSTS: redis://redis:6379
      MP_MESSAGING_OUTGOING_USER_BANNED_HOST: rabbitmq
      MP_MESSAGING_OUTGOING_USER_BANNED_PORT: 5672
      MP_MESSAGING_OUTGOING_USER_BANNED_USERNAME: priestess
      MP_MESSAGING_OUTGOING_USER_BANNED_PASSWORD: originium_seal
      JWT_SECRET: ${JWT_SECRET:-originium-256-bit-secret-key-change-in-production}
    ports:
      - "8081:8081"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_started
      rabbitmq:
        condition: service_started

  vault-service:
    build:
      context: ./backend/vault-service
      dockerfile: src/main/docker/Dockerfile.jvm
    container_name: eop-vault
    environment:
      QUARKUS_DATASOURCE_REACTIVE_URL: postgresql://postgres:5432/eyesofpriestess
      QUARKUS_DATASOURCE_USERNAME: priestess
      QUARKUS_DATASOURCE_PASSWORD: originium_seal
      QUARKUS_REDIS_HOSTS: redis://redis:6379
      MP_MESSAGING_INCOMING_PAYMENT_SUCCESS_HOST: rabbitmq
      MP_MESSAGING_INCOMING_PAYMENT_SUCCESS_PORT: 5672
      MP_MESSAGING_INCOMING_PAYMENT_SUCCESS_USERNAME: priestess
      MP_MESSAGING_INCOMING_PAYMENT_SUCCESS_PASSWORD: originium_seal
      MIDTRANS_SERVER_KEY: ${MIDTRANS_SERVER_KEY:-}
      MIDTRANS_CLIENT_KEY: ${MIDTRANS_CLIENT_KEY:-}
      XENDIT_API_KEY: ${XENDIT_API_KEY:-}
    ports:
      - "8082:8082"
    depends_on:
      - seal-service

  covenant-service:
    build:
      context: ./backend/covenant-service
      dockerfile: src/main/docker/Dockerfile.jvm
    container_name: eop-covenant
    environment:
      QUARKUS_DATASOURCE_REACTIVE_URL: postgresql://postgres:5432/eyesofpriestess
      QUARKUS_DATASOURCE_USERNAME: priestess
      QUARKUS_DATASOURCE_PASSWORD: originium_seal
      QUARKUS_REDIS_HOSTS: redis://redis:6379
      MP_MESSAGING_OUTGOING_ROOM_COMPLETED_HOST: rabbitmq
      MP_MESSAGING_OUTGOING_ROOM_COMPLETED_PORT: 5672
      MP_MESSAGING_OUTGOING_ROOM_COMPLETED_USERNAME: priestess
      MP_MESSAGING_OUTGOING_ROOM_COMPLETED_PASSWORD: originium_seal
    ports:
      - "8083:8083"
    depends_on:
      - seal-service

  communion-service:
    build:
      context: ./backend/communion-service
      dockerfile: src/main/docker/Dockerfile.jvm
    container_name: eop-communion
    environment:
      QUARKUS_DATASOURCE_REACTIVE_URL: postgresql://postgres:5432/eyesofpriestess
      QUARKUS_DATASOURCE_USERNAME: priestess
      QUARKUS_DATASOURCE_PASSWORD: originium_seal
    ports:
      - "8084:8084"
    depends_on:
      - seal-service

  judgment-service:
    build:
      context: ./backend/judgment-service
      dockerfile: src/main/docker/Dockerfile.jvm
    container_name: eop-judgment
    environment:
      QUARKUS_DATASOURCE_REACTIVE_URL: postgresql://postgres:5432/eyesofpriestess
      QUARKUS_DATASOURCE_USERNAME: priestess
      QUARKUS_DATASOURCE_PASSWORD: originium_seal
      MP_MESSAGING_INCOMING_ROOM_DISPUTED_HOST: rabbitmq
      MP_MESSAGING_INCOMING_ROOM_DISPUTED_PORT: 5672
      MP_MESSAGING_INCOMING_ROOM_DISPUTED_USERNAME: priestess
      MP_MESSAGING_INCOMING_ROOM_DISPUTED_PASSWORD: originium_seal
    ports:
      - "8085:8085"
    depends_on:
      - seal-service

  # The Veil (Gateway)
  the-veil:
    build:
      context: ./backend/the-veil
      dockerfile: src/main/docker/Dockerfile.jvm
    container_name: eop-veil
    environment:
      SEAL_SERVICE_URL: http://seal-service:8081
      VAULT_SERVICE_URL: http://vault-service:8082
      COVENANT_SERVICE_URL: http://covenant-service:8083
      COMMUNION_SERVICE_URL: http://communion-service:8084
      JUDGMENT_SERVICE_URL: http://judgment-service:8085
      QUARKUS_REDIS_HOSTS: redis://redis:6379
      JWT_SECRET: ${JWT_SECRET:-originium-256-bit-secret-key-change-in-production}
    ports:
      - "8080:8080"
    depends_on:
      - seal-service
      - vault-service
      - covenant-service
      - communion-service
      - judgment-service

  # The Sanctum (Frontend)
  the-sanctum:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: eop-sanctum
    environment:
      PUBLIC_API_URL: http://localhost:8080
      NODE_ENV: production
    ports:
      - "3000:3000"
    depends_on:
      - the-veil

volumes:
  postgres_data:
  redis_data:
  rabbitmq_data:
```

---

## 4. Environment Variables — The Seals

Create `.env` at project root:

```bash
# The Archive
POSTGRES_DB=eyesofpriestess
POSTGRES_USER=priestess
POSTGRES_PASSWORD=originium_seal

# The Crystal
REDIS_PASSWORD=originium_seal

# The Aether
RABBITMQ_USER=priestess
RABBITMQ_PASSWORD=originium_seal

# Sacred Seal (Generate strong secret for production)
JWT_SECRET=originium-256-bit-secret-key-min-32-characters

# Offering Channels (Sandbox)
MIDTRANS_SERVER_KEY=SB-Mid-server-your-sandbox-key
MIDTRANS_CLIENT_KEY=SB-Mid-client-your-sandbox-key
XENDIT_API_KEY=xnd_development_your_key

# The Sanctum
APP_NAME=EyesOfPriestess
APP_ENV=development
APP_DEBUG=true
```

---

## 5. Running the Sanctum

### 5.1 Start Infrastructure Only
```bash
docker-compose up -d postgres redis rabbitmq
```

### 5.2 Start All Sanctums
```bash
docker-compose up --build
```

### 5.3 Start Individual Sanctums (Development)

**Terminal 1 - Seal:**
```bash
cd backend/seal-service
mvn quarkus:dev
# http://localhost:8081
```

**Terminal 2 - Vault:**
```bash
cd backend/vault-service
mvn quarkus:dev
# http://localhost:8082
```

**Terminal 3 - Covenant:**
```bash
cd backend/covenant-service
mvn quarkus:dev
# http://localhost:8083
```

**Terminal 4 - Communion:**
```bash
cd backend/communion-service
mvn quarkus:dev
# http://localhost:8084
```

**Terminal 5 - Judgment:**
```bash
cd backend/judgment-service
mvn quarkus:dev
# http://localhost:8085
```

**Terminal 6 - The Veil:**
```bash
cd backend/the-veil
mvn quarkus:dev
# http://localhost:8080
```

**Terminal 7 - The Sanctum:**
```bash
cd frontend
npm run dev
# http://localhost:5173
```

### 5.4 Access Points

| Sanctum | URL | Notes |
|---------|-----|-------|
| The Sanctum | http://localhost:5173 | SvelteKit dev |
| The Veil | http://localhost:8080 | API Gateway |
| Seal | http://localhost:8081 | Direct access |
| Vault | http://localhost:8082 | Direct access |
| Covenant | http://localhost:8083 | Direct access |
| Communion | http://localhost:8084 | Direct access |
| Judgment | http://localhost:8085 | Direct access |
| Aether Mgmt | http://localhost:15672 | priestess/originium_seal |
| Archive | localhost:5432 | priestess/originium_seal |
| Crystal | localhost:6379 | No auth (dev) |

---

## 6. Database Migration — The Attunement Ritual

### 6.1 Create Migration
Create `backend/init-scripts/01-init-archives.sql`:

```sql
-- Create archives (schemas)
CREATE SCHEMA IF NOT EXISTS seal;
CREATE SCHEMA IF NOT EXISTS vault;
CREATE SCHEMA IF NOT EXISTS covenant;
CREATE SCHEMA IF NOT EXISTS communion;
CREATE SCHEMA IF NOT EXISTS judgment;

-- Enable UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Helper function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

### 6.2 Run Migration
```bash
# Auto-run on docker-compose up (via init-scripts)
# Or manually:
docker exec -i eop-postgres psql -U priestess -d eyesofpriestess < backend/init-scripts/01-init-archives.sql
```

---

## 7. Testing

### 7.1 Backend
```bash
cd backend/seal-service
mvn test
mvn verify  # with coverage
mvn package -Pnative -DskipTests  # native image
```

### 7.2 Frontend
```bash
cd frontend
npm run test       # Vitest
npm run test:e2e   # Playwright
npm run build      # build check
```

---

## 8. Production Build

### 8.1 Backend
```bash
# JVM mode
cd backend/the-veil
mvn package -DskipTests

# Native mode
cd backend/the-veil
mvn package -Pnative -DskipTests
```

### 8.2 Frontend
```bash
cd frontend
npm run build
```

### 8.3 Production Deploy
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up --build -d
```

---

## 9. Troubleshooting

### 9.1 Port Conflict
```bash
lsof -i :8080
kill -9 <PID>
```

### 9.2 Archive Connection Failed
```bash
docker ps | grep postgres
docker logs eop-postgres
docker-compose down -v && docker-compose up -d postgres
```

### 9.3 Crystal Connection
```bash
redis-cli ping  # Should return PONG
```

### 9.4 Seal Secret Mismatch
Ensure `JWT_SECRET` identical across seal-service and the-veil.

### 9.5 CORS Issues
```properties
# the-veil/src/main/resources/application.properties
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:5173,http://localhost:3000
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=Authorization,Content-Type,Covenant-Key
```

---

## 10. Useful Commands

```bash
docker ps                          # View sanctums
docker logs -f eop-veil          # View Veil logs
docker logs -f eop-seal          # View Seal logs
docker-compose restart the-veil  # Restart Veil
docker-compose down -v --rmi all # Clean everything
docker exec eop-postgres pg_dump -U priestess eyesofpriestess > backup.sql
docker exec -i eop-postgres psql -U priestess -d eyesofpriestess < backup.sql
```

---

*Next: Read `06-feature-specifications.md` for the complete feature chronicles.*
