# TRK Blockchain - Optional Backend Indexer

## Status: OPTIONAL

**This backend is NOT required for the dApp to function.** All core functionality (gaming, deposits, withdrawals, referrals, cashback, lucky draw) runs entirely on-chain through smart contracts.

## When to Use This Backend

The backend can optionally be used for:

1. **Event Indexing**: Cache blockchain events for faster queries
2. **Analytics Dashboard**: Aggregate statistics that would be expensive to compute on-chain
3. **Historical Data**: Store processed event history for reporting
4. **Legacy Migration**: If transitioning from the centralized version

## Current State

This backend was originally built for a centralized version of the platform. It includes:

- JWT-based authentication (replaced by wallet connection)
- H2 in-memory database (replaced by blockchain state)
- REST APIs for games, wallet, referrals (replaced by smart contract calls)

## Converting to Indexer (If Needed)

To use this as a blockchain event indexer:

### 1. Add Web3j Dependency

```xml
<!-- In pom.xml -->
<dependency>
    <groupId>org.web3j</groupId>
    <artifactId>core</artifactId>
    <version>4.10.0</version>
</dependency>
```

### 2. Create Event Listener Service

```java
@Service
public class BlockchainIndexerService {

    @Value("${bsc.rpc.url}")
    private String rpcUrl;

    @Value("${contract.address}")
    private String contractAddress;

    @PostConstruct
    public void startIndexing() {
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));

        // Subscribe to contract events
        EthFilter filter = new EthFilter(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST,
            contractAddress
        );

        web3j.ethLogFlowable(filter).subscribe(log -> {
            // Process and store events
            processEvent(log);
        });
    }

    private void processEvent(Log log) {
        // Decode and store event data
    }
}
```

### 3. Update Database Schema

Convert entities to store blockchain events:

```java
@Entity
public class GameEvent {
    @Id
    private String transactionHash;
    private String player;
    private BigDecimal betAmount;
    private Integer selectedNumber;
    private Integer winningNumber;
    private Boolean isWin;
    private LocalDateTime blockTimestamp;
}
```

### 4. Create Read-Only APIs

```java
@RestController
@RequestMapping("/api/v2/indexed")
public class IndexedDataController {

    @GetMapping("/games/{address}")
    public List<GameEvent> getGameHistory(@PathVariable String address) {
        // Return cached events from database
    }

    @GetMapping("/stats/platform")
    public PlatformStats getPlatformStats() {
        // Return aggregated statistics
    }
}
```

## Running the Backend

If you need to run the backend for indexing purposes:

```bash
cd backend
./mvnw spring-boot:run
```

The backend runs on http://localhost:8080

## Environment Variables

```properties
# application.properties
bsc.rpc.url=https://bsc-dataseed.binance.org/
contract.trk=0x... # TRKBlockchain contract address
contract.luckydraw=0x... # TRKLuckyDraw contract address
contract.club=0x... # TRKClubIncome contract address
```

## Important Notes

1. **Do NOT use for authentication** - Users authenticate via their Web3 wallet
2. **Do NOT use for game logic** - All games are played on-chain
3. **Do NOT use for balance queries** - Read directly from smart contracts
4. **Only use for caching/analytics** - To improve UX with pre-computed data

## API Endpoints (Legacy - Not Recommended)

These endpoints are from the centralized version and should NOT be used with the decentralized dApp:

| Endpoint | Status | Replacement |
|----------|--------|-------------|
| POST /api/auth/register | DEPRECATED | Wallet connection |
| POST /api/auth/login | DEPRECATED | Wallet signature |
| GET /api/user/me | DEPRECATED | Contract: getUserInfo() |
| POST /api/game/play | DEPRECATED | Contract: playGame() |
| GET /api/wallet | DEPRECATED | Contract: getUserWallets() |
| POST /api/wallet/deposit | DEPRECATED | Contract: deposit() |
| POST /api/wallet/withdraw | DEPRECATED | Contract: withdraw() |

## Recommended Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Frontend      │ ──► │   Smart         │ ──► │   BSC Network   │
│   (Angular)     │     │   Contracts     │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                │
                        Event Logs
                                │
                                ▼
                        ┌─────────────────┐
                        │   Indexer       │  ◄── OPTIONAL
                        │   (This Backend)│
                        └─────────────────┘
                                │
                                ▼
                        ┌─────────────────┐
                        │   PostgreSQL    │  ◄── For analytics
                        │   (Replace H2)  │
                        └─────────────────┘
```

## License

Proprietary - All rights reserved
