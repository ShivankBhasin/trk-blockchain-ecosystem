# TRK Blockchain Real Cash Game Ecosystem

A comprehensive blockchain-based gaming platform built with Angular 18 and Spring Boot.

## Quick Start

```bash
# Start the entire application
./start.sh

# Stop the application
./stop.sh
```

The application will be available at:
- **Frontend**: http://localhost:4200
- **Backend**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console

## Project Structure

```
blockchain/
├── frontend/                    # Angular 18 frontend application
│   └── trk-blockchain/
│       ├── src/
│       │   ├── app/
│       │   │   ├── core/        # Services, guards, interceptors
│       │   │   ├── features/    # Feature modules
│       │   │   ├── layout/      # Layout components
│       │   │   └── models/      # TypeScript interfaces
│       │   └── environments/
│       └── angular.json
├── backend/                     # Spring Boot backend application
│   ├── src/main/java/com/trk/blockchain/
│   │   ├── controller/          # REST API endpoints
│   │   ├── service/             # Business logic
│   │   ├── repository/          # Data access
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Data transfer objects
│   │   ├── security/            # JWT authentication
│   │   ├── config/              # Configuration
│   │   └── exception/           # Custom exceptions
│   └── pom.xml
└── README.md
```

## Features

### Gaming System
- 8x multiplier game mechanics (12.5% win rate)
- Practice mode with non-withdrawable balance
- Cash game with real USDT

### 7 Income Streams
1. Winners 8X Income - From your game wins
2. Direct Level Income - 15 levels (5%, 2%, 1%, 0.5%)
3. Winner Level Income - 15 levels from team wins
4. Cashback Protection - 0.5% daily loss recovery
5. ROI on ROI - 15 levels from team cashback
6. Club Income - Platform turnover percentage
7. Lucky Draw Income - Jackpot wins

### Lucky Draw System
- 10,000 tickets per draw
- 1,000 winners per draw
- 70,000 USDT prize pool
- Prize structure: 10,000 to 20 USDT

### Referral System
- 15-level deep referral structure
- Commission on deposits and wins
- Activation requirements

### Wallet System
- Cash Balance
- Practice Balance
- Direct Wallet
- Lucky Draw Wallet
- BEP20 (BSC) integration

## Prerequisites

### Frontend
- Node.js 18+
- npm 9+

### Backend
- Java 21 (OpenJDK recommended)
- Maven 3.8+ (included via wrapper)

## Setup

### Option 1: Using Shell Scripts (Recommended)

```bash
# Start both frontend and backend
./start.sh

# Stop the application
./stop.sh
```

### Option 2: Manual Setup

#### Frontend

```bash
cd frontend/trk-blockchain
npm install
npm start
```

The frontend runs on http://localhost:4200

#### Backend

```bash
cd backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./mvnw spring-boot:run
```

The backend runs on http://localhost:8080

## Building for Production

### Frontend

```bash
cd frontend/trk-blockchain
npm run build
```

Output in `dist/trk-blockchain/`

### Backend

```bash
cd backend
./mvnw clean package -DskipTests
java -jar target/blockchain-1.0.0.jar
```

## API Endpoints

### Authentication
- POST /api/auth/register - Register new user
- POST /api/auth/login - User login

### User
- GET /api/user/me - Current user info
- GET /api/user/dashboard - Dashboard data

### Game
- POST /api/game/play - Play a game
- GET /api/game/history - Game history

### Wallet
- GET /api/wallet - Wallet balances
- POST /api/wallet/deposit - Deposit USDT
- POST /api/wallet/withdraw - Withdraw USDT
- POST /api/wallet/transfer - Transfer between wallets
- GET /api/wallet/transactions - Transaction history

### Referral
- GET /api/referral/info - Referral info and stats
- GET /api/referral/team - Team members

### Income
- GET /api/income/overview - Income overview
- GET /api/income/history - Income history

### Lucky Draw
- GET /api/lucky-draw/current - Current draw info
- POST /api/lucky-draw/buy - Buy tickets
- GET /api/lucky-draw/history - Draw history

## Technology Stack

### Frontend
- Angular 18
- Bootstrap 5
- RxJS
- TypeScript 5

### Backend
- Spring Boot 2.7.18
- Spring Security with JWT
- Spring Data JPA
- H2 Database (in-memory)
- Lombok
- Maven

## Security

- JWT-based authentication
- BCrypt password hashing
- CORS configured for frontend
- Protected API endpoints

## Database

Using H2 in-memory database for development.

Access H2 Console: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:trkdb
- Username: sa
- Password: (empty)

## Shell Scripts

| Script | Description | Platform |
|--------|-------------|----------|
| `start.sh` | Starts both backend and frontend servers | macOS/Linux |
| `stop.sh` | Stops all running application processes | macOS/Linux |
| `start.bat` | Starts both backend and frontend servers | Windows |
| `stop.bat` | Stops all running application processes | Windows |

---

## Deploying to Render (Cloud Hosting)

### Prerequisites
- GitHub account
- Render account (free tier available at [render.com](https://render.com))

### Step 1: Push to GitHub

```bash
# Initialize git (if not already done)
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit with Render deployment config"

# Add your GitHub remote
git remote add origin https://github.com/YOUR_USERNAME/trk-blockchain.git

# Push to GitHub
git push -u origin main
```

### Step 2: Deploy on Render

1. Go to [render.com](https://render.com) and sign in
2. Click **New** → **Blueprint**
3. Connect your GitHub account (if not already connected)
4. Select your `trk-blockchain` repository
5. Render will automatically detect the `render.yaml` file
6. Click **Apply** to create both services

### Step 3: Wait for Deployment

- **Frontend** (Static Site): Deploys in ~2-3 minutes
- **Backend** (Docker): First deploy takes ~5-10 minutes (building Docker image)

### Step 4: Access Your Application

After deployment, your app will be available at:
- **Frontend**: `https://trk-blockchain-frontend.onrender.com`
- **Backend API**: `https://trk-blockchain-api.onrender.com`

### Post-Deployment Configuration

If your service names differ from the defaults, update these files:

1. **Frontend API URL** - Edit `frontend/trk-blockchain/src/environments/environment.prod.ts`:
   ```typescript
   apiUrl: 'https://YOUR-BACKEND-NAME.onrender.com/api'
   ```

2. **Backend CORS** - In Render Dashboard → trk-blockchain-api → Environment:
   ```
   CORS_ALLOWED_ORIGINS=https://YOUR-FRONTEND-NAME.onrender.com
   ```

### Render Free Tier Limitations

| Limitation | Impact |
|------------|--------|
| **Sleep after 15 min idle** | First request after sleep takes 30-60 seconds |
| **512MB RAM** | JVM optimized with memory flags in Dockerfile |
| **750 instance hours/month** | Enough for 24/7 single service |
| **No persistent database** | Using H2 in-memory; data resets on restart |

### Upgrading for Production

For production use, consider:
1. **Render Starter plan** ($7/month) - No sleep, persistent disk
2. **External PostgreSQL** - Supabase, Neon, or PlanetScale (free tiers available)
3. **Custom domain** - Available on paid plans

---

## Project Files

| File | Description |
|------|-------------|
| `render.yaml` | Render Blueprint deployment configuration |
| `backend/Dockerfile` | Docker image for Spring Boot (memory optimized) |
| `backend/src/main/resources/application-prod.properties` | Production Spring Boot config |

## Additional Documentation

| File | Description |
|------|-------------|
| `RUN.md` | Detailed running instructions and troubleshooting |
| `COMMIT_GUIDE.md` | Git commit timeline for development history |

## License

Proprietary - All rights reserved
