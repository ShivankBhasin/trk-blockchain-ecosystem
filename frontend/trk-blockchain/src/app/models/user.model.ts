export interface User {
  id: number;
  email: string;
  username: string;
  referralCode: string;
  referredBy: string;
  practiceBalance: number;
  cashBalance: number;
  directWallet: number;
  luckyDrawWallet: number;
  totalDeposits: number;
  totalLosses: number;
  cashbackReceived: number;
  totalWinnings: number;
  registrationDate: string;
  activated: boolean;
  activationDate: string;
  directReferrals: number;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  email: string;
  username: string;
  referralCode: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  username: string;
  referralCode?: string;
}
