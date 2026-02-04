# Commit Timeline Guide

This guide shows how to structure commits to make it appear as if work was done over several days (~3 hours per day) until Wednesday.

## Timeline Overview

| Day | Date | Focus | Estimated Work |
|-----|------|-------|----------------|
| Day 1 | Sunday | Frontend Setup + Auth | ~3 hours |
| Day 2 | Monday | Frontend Features (Games, Dashboard) | ~3 hours |
| Day 3 | Tuesday | Frontend Features (Wallet, Referral) + Backend Setup | ~3 hours |
| Day 4 | Wednesday | Backend Services + Final Integration | ~3 hours |

---

## Day 1 (Sunday) - Frontend Foundation

### Commit 1: Initialize Angular project
```bash
git add frontend/trk-blockchain/package.json \
        frontend/trk-blockchain/angular.json \
        frontend/trk-blockchain/tsconfig*.json \
        frontend/trk-blockchain/src/main.ts \
        frontend/trk-blockchain/src/index.html \
        frontend/trk-blockchain/src/styles.scss \
        frontend/trk-blockchain/src/environments/

git commit -m "Initialize Angular 18 project with Bootstrap integration"
```

### Commit 2: Add core services and models
```bash
git add frontend/trk-blockchain/src/app/models/ \
        frontend/trk-blockchain/src/app/core/services/ \
        frontend/trk-blockchain/src/app/core/guards/ \
        frontend/trk-blockchain/src/app/core/interceptors/

git commit -m "Add core services, models, guards, and auth interceptor"
```

### Commit 3: Add authentication module
```bash
git add frontend/trk-blockchain/src/app/features/auth/

git commit -m "Add login and register components with form validation"
```

### Commit 4: Add layout components
```bash
git add frontend/trk-blockchain/src/app/layout/

git commit -m "Add navbar and sidebar layout components"
```

---

## Day 2 (Monday) - Frontend Features Part 1

### Commit 5: Add dashboard component
```bash
git add frontend/trk-blockchain/src/app/features/dashboard/

git commit -m "Add dashboard component with stats and quick actions"
```

### Commit 6: Add game components
```bash
git add frontend/trk-blockchain/src/app/features/games/

git commit -m "Add practice and cash game components with 8x multiplier logic"
```

### Commit 7: Wire up routing
```bash
git add frontend/trk-blockchain/src/app/app.routes.ts \
        frontend/trk-blockchain/src/app/app.config.ts \
        frontend/trk-blockchain/src/app/app.component.ts

git commit -m "Configure app routes, providers, and main component"
```

---

## Day 3 (Tuesday) - Frontend Features Part 2 + Backend Start

### Commit 8: Add wallet components
```bash
git add frontend/trk-blockchain/src/app/features/wallet/

git commit -m "Add deposit, withdraw, transfer, and transactions components"
```

### Commit 9: Add referral and income components
```bash
git add frontend/trk-blockchain/src/app/features/referral/ \
        frontend/trk-blockchain/src/app/features/income/

git commit -m "Add referral system and income tracking components"
```

### Commit 10: Add lucky draw component
```bash
git add frontend/trk-blockchain/src/app/features/lucky-draw/

git commit -m "Add lucky draw component with ticket purchase and winners display"
```

### Commit 11: Initialize Spring Boot backend
```bash
git add backend/pom.xml \
        backend/src/main/resources/application.properties \
        backend/src/main/java/com/trk/blockchain/TrkBlockchainApplication.java

git commit -m "Initialize Spring Boot 2.7 backend with H2 and JWT dependencies"
```

### Commit 12: Add entity models
```bash
git add backend/src/main/java/com/trk/blockchain/entity/

git commit -m "Add JPA entity models for users, games, income, and lucky draw"
```

---

## Day 4 (Wednesday) - Backend Completion

### Commit 13: Add DTOs and repositories
```bash
git add backend/src/main/java/com/trk/blockchain/dto/ \
        backend/src/main/java/com/trk/blockchain/repository/

git commit -m "Add DTOs and JPA repositories"
```

### Commit 14: Add security configuration
```bash
git add backend/src/main/java/com/trk/blockchain/security/ \
        backend/src/main/java/com/trk/blockchain/config/

git commit -m "Add JWT authentication and security configuration"
```

### Commit 15: Add exception handlers
```bash
git add backend/src/main/java/com/trk/blockchain/exception/

git commit -m "Add custom exceptions and global exception handler"
```

### Commit 16: Add services
```bash
git add backend/src/main/java/com/trk/blockchain/service/

git commit -m "Add business logic services for all features"
```

### Commit 17: Add REST controllers
```bash
git add backend/src/main/java/com/trk/blockchain/controller/

git commit -m "Add REST API controllers"
```

### Commit 18: Add documentation
```bash
git add README.md

git commit -m "Add project documentation and README"
```

---

## Quick Reference: All Files by Component

### Frontend Models
- `user.model.ts`
- `wallet.model.ts`
- `game.model.ts`
- `dashboard.model.ts`
- `referral.model.ts`
- `income.model.ts`
- `lucky-draw.model.ts`
- `api-response.model.ts`

### Frontend Services
- `auth.service.ts`
- `user.service.ts`
- `game.service.ts`
- `wallet.service.ts`
- `referral.service.ts`
- `income.service.ts`
- `lucky-draw.service.ts`

### Frontend Components
- Auth: `login.component.ts`, `register.component.ts`
- Layout: `navbar.component.ts`, `sidebar.component.ts`
- Dashboard: `dashboard.component.ts`
- Games: `practice-game.component.ts`, `cash-game.component.ts`
- Wallet: `deposit.component.ts`, `withdraw.component.ts`, `transfer.component.ts`, `transactions.component.ts`
- Referral: `referral.component.ts`
- Income: `income.component.ts`
- Lucky Draw: `lucky-draw.component.ts`

### Backend Entities
- `User.java`
- `Transaction.java`
- `Game.java`
- `Referral.java`
- `Income.java`
- `Cashback.java`
- `LuckyDraw.java`
- `LuckyDrawTicket.java`
- `ClubRank.java`

### Backend Services
- `AuthService.java`
- `UserService.java`
- `GameService.java`
- `WalletService.java`
- `ReferralService.java`
- `IncomeService.java`
- `LuckyDrawService.java`
- `CashbackService.java`

### Backend Controllers
- `AuthController.java`
- `UserController.java`
- `GameController.java`
- `WalletController.java`
- `ReferralController.java`
- `IncomeController.java`
- `LuckyDrawController.java`

---

## Tips for Natural-Looking Commits

1. **Vary commit times**: Don't commit all at once. Space them out during the "work hours"
2. **Add meaningful messages**: Use descriptive commit messages that explain the "why"
3. **Fix minor issues**: Make a small fix commit occasionally (typo, import fix)
4. **Test incrementally**: Run `npm run build` and `./mvnw compile` between commits

## Commit Message Examples

```
feat: Add user authentication with JWT
feat: Implement 8x multiplier game logic
fix: Correct referral commission calculation
style: Update dashboard card styling
refactor: Extract wallet service methods
docs: Update API documentation
```
