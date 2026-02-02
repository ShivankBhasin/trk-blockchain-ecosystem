export interface Wallet {
  practiceBalance: number;
  cashBalance: number;
  directWallet: number;
  luckyDrawWallet: number;
  totalBalance: number;
}

export interface DepositRequest {
  amount: number;
  txHash?: string;
}

export interface WithdrawRequest {
  amount: number;
  walletAddress: string;
}

export interface TransferRequest {
  fromWallet: string;
  toWallet: string;
  amount: number;
}

export interface Transaction {
  id: number;
  userId: number;
  type: string;
  amount: number;
  walletType: string;
  timestamp: string;
  description: string;
  txHash: string;
}
