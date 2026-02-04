import { User } from './user.model';
import { Wallet } from './wallet.model';

export interface Dashboard {
  user: User;
  wallet: Wallet;
  totalIncome: number;
  totalGamesPlayed: number;
  gamesWon: number;
  gamesLost: number;
  winRate: number;
  directReferrals: number;
  totalTeamSize: number;
  daysUntilExpiry: number;
  isPracticeExpiring: boolean;
  cashback: CashbackInfo;
}

export interface CashbackInfo {
  active: boolean;
  totalLosses: number;
  dailyRate: number;
  totalReceived: number;
  maxCapping: number;
  remaining: number;
}
