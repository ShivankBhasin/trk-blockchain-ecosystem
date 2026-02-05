import { Routes } from '@angular/router';

import { ConnectWalletComponent } from './features/auth/connect-wallet.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { GamesComponent } from './features/games/games.component';

import { PracticeGameComponent } from './features/games/practice-game.component';
import { CashGameComponent } from './features/games/cash-game.component';

import { ReferralComponent } from './features/referral/referral.component';
import { IncomeComponent } from './features/income/income.component';
import { LuckyDrawComponent } from './features/lucky-draw/lucky-draw.component';

import { AboutComponent } from './features/pages/about.component';
import { HowItWorksComponent } from './features/pages/how-it-works.component';
import { FaqComponent } from './features/pages/faq.component';
import { TermsComponent } from './features/pages/terms.component';

import { PreDashboardRegisterComponent } from './features/pages/pre-dashboard-register/pre-dashboard-register.component';

export const routes: Routes = [

  { path: '', component: ConnectWalletComponent },

  { path: 'register', component: PreDashboardRegisterComponent },

  { path: 'dashboard', component: DashboardComponent },

  { path: 'games', component: GamesComponent },

  // ✅ THESE TWO LINES ARE MANDATORY
  { path: 'practice-game', component: PracticeGameComponent },
  { path: 'cash-game', component: CashGameComponent },

  { path: 'referral', component: ReferralComponent },
  { path: 'income', component: IncomeComponent },
  { path: 'lucky-draw', component: LuckyDrawComponent },

  { path: 'about', component: AboutComponent },
  { path: 'how-it-works', component: HowItWorksComponent },
  { path: 'faq', component: FaqComponent },
  { path: 'terms', component: TermsComponent },

  { path: '**', redirectTo: '' }
];
