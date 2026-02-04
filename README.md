# TRK Blockchain - Fully Decentralized Gaming Platform

A fully decentralized blockchain-based gaming platform on Binance Smart Chain (BSC). All business logic runs on-chain through smart contracts - no centralized backend required.

## Quick Start

### Prerequisites
- Node.js 18+
- MetaMask, Trust Wallet, or any Web3 wallet
- BNB for gas fees
- USDT (BEP20) for gameplay

### Run Frontend Only (Recommended)
```bash
cd frontend/trk-blockchain
npm install
npm start
```

Frontend: http://localhost:4200

### Deploy Smart Contracts (for development)
```bash
cd contracts
npm install
npx hardhat compile
npx hardhat run scripts/deploy.js --network bscTestnet
```

## Architecture

This is a **fully decentralized application (dApp)** where all business logic runs on Binance Smart Chain:

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER WALLET                              │
│           (MetaMask / Trust Wallet / WalletConnect)              │
└─────────────────────────────────────────────────────────────────┘
                              │
                    Web3 / ethers.js
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     ANGULAR FRONTEND                             │
│                    (Static Web App)                              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  Web3Service: Contract Interactions via ethers.js       │    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │  Features: Connect | Dashboard | Games | Wallet         │    │
│  │            Referral | Income | Lucky Draw               │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                    Smart Contract Calls
                      (BSC Network)
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   BINANCE SMART CHAIN                            │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  TRKBlockchain.sol - Main Game Contract                 │    │
│  │  • User registration & referral tree                    │    │
│  │  • 4 wallet types (Cash, Practice, Direct, LuckyDraw)   │    │
│  │  • 8X multiplier game (12.5% win rate)                  │    │
│  │  • 15-level commission distribution                     │    │
│  │  • Cashback protection (0.5% daily)                     │    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │  TRKLuckyDraw.sol - Lottery Contract                    │    │
│  │  • 10,000 tickets per draw                              │    │
│  │  • 1,000 winners (70,000 USDT prize pool)               │    │
│  │  • VRF randomness for fair selection                    │    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │  TRKClubIncome.sol - Rank & Club Rewards                │    │
│  │  • 6 ranks (Star to Crown)                              │    │
│  │  • 8% daily turnover distribution                       │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  USDT (BEP20) Token Contract                            │    │
│  │  0x55d398326f99059fF775485246999027B3197955 (Mainnet)   │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

## Project Structure

```
blockchain/
├── frontend/                    # Angular 18 dApp
│   └── trk-blockchain/
│       ├── src/app/
│       │   ├── core/services/
│       │   │   └── web3.service.ts    # All blockchain interactions
│       │   ├── features/              # UI components
│       │   └── layout/                # Navigation & sidebar
│       └── src/environments/          # Contract addresses
├── contracts/                   # Solidity smart contracts
│   ├── TRKBlockchain.sol        # Main game contract
│   ├── TRKLuckyDraw.sol         # Lottery contract
│   ├── TRKClubIncome.sol        # Club rewards contract
│   ├── IERC20.sol               # USDT interface
│   ├── scripts/deploy.js        # Deployment script
│   └── hardhat.config.js        # Hardhat configuration
├── backend/                     # OPTIONAL: Legacy indexer (see backend/README.md)
└── README.md
```

## Features (All On-Chain)

### Gaming System
- **8X Multiplier Game**: Pick 1-8, win 8X your bet (12.5% probability)
- **Payout Split**: 2X to Direct Wallet (withdrawable) + 6X compounds to Cash Balance
- **Practice Mode**: 100 USDT free practice balance (first 100,000 users)
- **Cash Mode**: Real USDT gameplay

### 7 Income Streams
| Stream | Description | Payout |
|--------|-------------|--------|
| 1. Winners 8X | From your game wins | 8X bet amount |
| 2. Direct Level | From team deposits | 5%, 2%, 1%, 0.5% (15 levels) |
| 3. Winner Level | From team wins | 5%, 2%, 1%, 0.5% (15 levels) |
| 4. Cashback Protection | Daily loss recovery | 0.5% of losses daily |
| 5. ROI on ROI | From team cashback | 10% (5 levels) |
| 6. Club Income | Platform turnover share | 8% pool (rank-based) |
| 7. Lucky Draw | Lottery winnings | Up to 10,000 USDT |

### Lucky Draw System
- 10,000 tickets at 10 USDT each
- 1,000 winners per draw
- Prize structure: 10,000 → 20 USDT
- Automatic execution when sold out

### Referral System
- 15-level deep structure
- Commission on deposits AND wins
- Unlock levels by direct referrals:
  - Level 1: 1 direct (5%)
  - Level 2: 2 directs (2%)
  - Levels 3-5: 3-5 directs (1%)
  - Levels 6-10: 6-10 directs (0.5%)
  - Levels 11-15: 10 directs + Premium (0.5%)

### Club Income Ranks
| Rank | Title | Volume Required | Pool Share |
|------|-------|-----------------|------------|
| 1 | Star | $10,000 | 10% |
| 2 | Silver | $50,000 | 15% |
| 3 | Gold | $250,000 | 20% |
| 4 | Platinum | $1,000,000 | 20% |
| 5 | Diamond | $5,000,000 | 17.5% |
| 6 | Crown | $10,000,000 | 17.5% |

## Smart Contracts

### TRKBlockchain.sol
Main contract handling:
- User registration with referral
- Deposit/withdrawal of USDT
- Game logic (8X multiplier)
- 15-level commission distribution
- Cashback claims

### TRKLuckyDraw.sol
Lottery contract:
- Ticket purchases
- Winner selection with VRF
- Prize distribution

### TRKClubIncome.sol
Club rewards:
- Rank qualification
- Daily turnover distribution
- Volume tracking

## Deployment

### BSC Testnet
```bash
cd contracts
cp .env.example .env
# Add your PRIVATE_KEY to .env
npx hardhat run scripts/deploy.js --network bscTestnet
```

### BSC Mainnet
```bash
npx hardhat run scripts/deploy.js --network bscMainnet
```

After deployment, update contract addresses in:
- `frontend/trk-blockchain/src/environments/environment.ts` (testnet)
- `frontend/trk-blockchain/src/environments/environment.prod.ts` (mainnet)

## Security Features

- **Self-Custody**: Users control their own wallets
- **No Admin Keys**: Game logic is immutable
- **Provably Fair**: VRF for random number generation
- **Transparent**: All transactions verifiable on BSCScan
- **No Centralized Database**: All data on blockchain

## Technology Stack

### Frontend
- Angular 18 (Standalone Components)
- ethers.js v6 (Web3 interactions)
- Bootstrap 5 (UI)
- TypeScript 5

### Smart Contracts
- Solidity 0.8.19
- Hardhat (Development & Deployment)
- OpenZeppelin (Security patterns)

### Blockchain
- Binance Smart Chain (BEP20)
- USDT as gaming currency

## Why Decentralized?

| Aspect | Centralized | Decentralized (TRK) |
|--------|-------------|---------------------|
| **Trust** | Trust the operator | Trust the code |
| **Funds** | Held by operator | Self-custody wallet |
| **Transparency** | Hidden logic | Open source contracts |
| **Censorship** | Can ban users | Permissionless |
| **Downtime** | Server dependent | Blockchain uptime |
| **Fairness** | Hope it's fair | Provably fair (VRF) |

## Backend (Optional)

The `backend/` folder contains an optional Spring Boot service that can be used as:
- **Event Indexer**: Cache blockchain events for faster queries
- **Analytics**: Off-chain data aggregation
- **Legacy Support**: If migrating from centralized version

**The backend is NOT required for the dApp to function.** All core functionality runs on-chain.

See `backend/README.md` for details on running the optional indexer.

## License

Proprietary - All rights reserved
