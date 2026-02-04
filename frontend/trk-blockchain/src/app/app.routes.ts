import { Routes } from '@angular/router';
import { WalletGuard } from './core/guards/wallet.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/connect', pathMatch: 'full' },
  {
    path: 'connect',
    loadComponent: () => import('./features/auth/connect-wallet.component').then(m => m.ConnectWalletComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [WalletGuard]
  },
  {
    path: 'games/practice',
    loadComponent: () => import('./features/games/practice-game.component').then(m => m.PracticeGameComponent),
    canActivate: [WalletGuard]
  },
  {
    path: 'games/cash',
    loadComponent: () => import('./features/games/cash-game.component').then(m => m.CashGameComponent),
    canActivate: [WalletGuard]
  },
  {
    path: 'wallet/deposit',
    loadComponent: () => import('./features/wallet/deposit.component').then(m => m.DepositComponent),
    canActivate: [WalletGuard]
  },
  {
    path: 'wallet/withdraw',
    loadComponent: () => import('./features/wallet/withdraw.component').then(m => m.WithdrawComponent),
    canActivate: [WalletGuard]
  },
  {
    path: 'wallet/transactions',
    loadComponent: () => import('./features/wallet/transactions.component').then(m => m.TransactionsComponent),
    canActivate: [WalletGuard]
  },
  {
    path: 'referral',
    loadComponent: () => import('./features/referral/referral.component').then(m => m.ReferralComponent),
    canActivate: [WalletGuard]
  },
  {
    path: 'income',
    loadComponent: () => import('./features/income/income.component').then(m => m.IncomeComponent),
    canActivate: [WalletGuard]
  },
  {
    path: 'lucky-draw',
    loadComponent: () => import('./features/lucky-draw/lucky-draw.component').then(m => m.LuckyDrawComponent),
    canActivate: [WalletGuard]
  },
  { path: '**', redirectTo: '/connect' }
];
