# Running TRK Blockchain Application

## Prerequisites

### Backend
- Java 21 (OpenJDK recommended)
- Maven 3.8+ (included via wrapper)

### Frontend
- Node.js 18+
- npm 9+

## Quick Start

### 1. Start the Backend

```bash
cd /Users/rakshit.bhasin/Desktop/blockchain/backend

# Set Java Home (macOS with Homebrew)
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home

# Run the application
./mvnw spring-boot:run
```

The backend will start on **http://localhost:8080**

### 2. Start the Frontend

Open a new terminal:

```bash
cd /Users/rakshit.bhasin/Desktop/blockchain/frontend/trk-blockchain

# Install dependencies (first time only)
npm install

# Start development server
npm start
```

The frontend will start on **http://localhost:4200**

## Access the Application

1. Open your browser and go to: **http://localhost:4200**
2. Register a new account
3. Start playing!

## Default Test Account

Register with any email/password. Each new user gets:
- 100 USDT Practice Balance
- Unique Referral Code

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/auth/register | POST | Register new user |
| /api/auth/login | POST | User login |
| /api/user/dashboard | GET | Dashboard data |
| /api/game/play | POST | Play a game |
| /api/wallet | GET | Wallet balances |
| /api/wallet/deposit | POST | Deposit USDT |
| /api/referral/info | GET | Referral info |
| /api/income/overview | GET | Income streams |
| /api/lucky-draw/current | GET | Current draw |

## Database Console

Access H2 Database Console at: **http://localhost:8080/h2-console**

- JDBC URL: `jdbc:h2:mem:trkdb`
- Username: `sa`
- Password: *(leave empty)*

## Stopping the Application

- **Backend**: Press `Ctrl+C` in the backend terminal
- **Frontend**: Press `Ctrl+C` in the frontend terminal

## Troubleshooting

### Backend won't start
- Ensure Java 21 is installed: `java -version`
- Check JAVA_HOME is set correctly
- Port 8080 might be in use: `lsof -i :8080`

### Frontend won't start
- Ensure Node.js 18+ is installed: `node -v`
- Delete `node_modules` and reinstall: `rm -rf node_modules && npm install`
- Port 4200 might be in use: `lsof -i :4200`

### CORS errors
- Ensure backend is running before frontend
- Backend must be on port 8080
- Frontend must be on port 4200

## Production Build

### Backend
```bash
cd backend
./mvnw clean package -DskipTests
java -jar target/blockchain-1.0.0.jar
```

### Frontend
```bash
cd frontend/trk-blockchain
npm run build
# Output in dist/trk-blockchain/
```
