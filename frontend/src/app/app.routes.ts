import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent),
    canActivate: [guestGuard]
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register.component').then(m => m.RegisterComponent),
    canActivate: [guestGuard]
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'games/practice',
    loadComponent: () => import('./features/games/practice-game.component').then(m => m.PracticeGameComponent),
    canActivate: [authGuard]
  },
  {
    path: 'games/cash',
    loadComponent: () => import('./features/games/cash-game.component').then(m => m.CashGameComponent),
    canActivate: [authGuard]
  },
  {
    path: 'wallet/deposit',
    loadComponent: () => import('./features/wallet/deposit.component').then(m => m.DepositComponent),
    canActivate: [authGuard]
  },
  {
    path: 'wallet/withdraw',
    loadComponent: () => import('./features/wallet/withdraw.component').then(m => m.WithdrawComponent),
    canActivate: [authGuard]
  },
  {
    path: 'wallet/transfer',
    loadComponent: () => import('./features/wallet/transfer.component').then(m => m.TransferComponent),
    canActivate: [authGuard]
  },
  {
    path: 'wallet/transactions',
    loadComponent: () => import('./features/wallet/transactions.component').then(m => m.TransactionsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'referral',
    loadComponent: () => import('./features/referral/referral.component').then(m => m.ReferralComponent),
    canActivate: [authGuard]
  },
  {
    path: 'income',
    loadComponent: () => import('./features/income/income.component').then(m => m.IncomeComponent),
    canActivate: [authGuard]
  },
  {
    path: 'lucky-draw',
    loadComponent: () => import('./features/lucky-draw/lucky-draw.component').then(m => m.LuckyDrawComponent),
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '/dashboard' }
];
