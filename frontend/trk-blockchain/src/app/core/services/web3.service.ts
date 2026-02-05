import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface WalletState {
  isConnected: boolean;
  address: string | null;
  chainId: number | null;
  balance: string | null;
  usdtBalance: string | null;
}

export interface UserInfo {
  isRegistered: boolean;
  referrer: string;
  registrationTime: Date;
  totalDeposited: string;
  totalWithdrawn: string;
  directReferrals: number;
  teamSize: number;
  isActivated: boolean;
  isPremiumActivated: boolean;
  totalLosses: string;
  cashbackReceived: string;
  lastCashbackTime: Date;
}

export interface Wallets {
  cashBalance: string;
  practiceBalance: string;
  directWallet: string;
  luckyDrawWallet: string;
}

export interface GameResult {
  timestamp: Date;
  selectedNumber: number;
  winningNumber: number;
  betAmount: string;
  isWin: boolean;
  isPractice: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class Web3Service {

  // ===============================
  // STATE
  // ===============================

  private walletState = new BehaviorSubject<WalletState>({
    isConnected: false,
    address: null,
    chainId: null,
    balance: "0",
    usdtBalance: "0"
  });

  walletState$ = this.walletState.asObservable();

  constructor() {
    this.restoreSession();
    this.setupListeners();
  }

  // ===============================
  // METAMASK
  // ===============================

  private setupListeners() {
    const eth = (window as any).ethereum;
    if (!eth) return;

    eth.on('accountsChanged', (accounts: string[]) => {
      if (accounts.length === 0) {
        this.disconnect();
      } else {
        this.saveWallet(accounts[0]);
      }
    });
  }

  async connectWallet(): Promise<boolean> {
    const eth = (window as any).ethereum;

    if (!eth) {
      throw new Error('MetaMask not installed');
    }

    const accounts = await eth.request({
      method: 'eth_requestAccounts'
    });

    this.saveWallet(accounts[0]);
    return true;
  }

  private saveWallet(address: string) {
    localStorage.setItem('wallet_connected', 'true');
    localStorage.setItem('wallet_address', address);

    this.walletState.next({
      isConnected: true,
      address,
      chainId: 0,
      balance: "0",
      usdtBalance: "0"
    });
  }

  restoreSession() {
    const connected = localStorage.getItem('wallet_connected');
    const address = localStorage.getItem('wallet_address');

    if (connected === 'true' && address) {
      this.walletState.next({
        isConnected: true,
        address,
        chainId: 0,
        balance: "0",
        usdtBalance: "0"
      });
    }
  }

  disconnect() {
    localStorage.removeItem('wallet_connected');
    localStorage.removeItem('wallet_address');

    this.walletState.next({
      isConnected: false,
      address: null,
      chainId: null,
      balance: "0",
      usdtBalance: "0"
    });
  }

  // ===============================
  // REGISTER (DEV + REAL HOOK)
  // ===============================

 async register(referrer?: string): Promise<boolean> {

  console.log('Attempt real register with:', referrer);

  if (typeof window === 'undefined' || !(window as any).ethereum) {
    throw new Error('MetaMask not available');
  }

  const accounts = await (window as any).ethereum.request({
    method: 'eth_accounts'
  });

  const from = accounts[0];

  // 🔥 Trigger MetaMask popup (dummy tx)
  await (window as any).ethereum.request({
    method: 'eth_sendTransaction',
    params: [
      {
        from: from,
        to: from,       // sending to self
        value: '0x0'    // zero value
      }
    ]
  });

  return true;
}

  // ===============================
  // DASHBOARD MOCK DATA
  // ===============================

  async getUserInfo(): Promise<UserInfo> {
    return {
      isRegistered: true,
      referrer: 'DEV',
      registrationTime: new Date(),
      totalDeposited: "0",
      totalWithdrawn: "0",
      directReferrals: 0,
      teamSize: 0,
      isActivated: true,
      isPremiumActivated: true,
      totalLosses: "0",
      cashbackReceived: "0",
      lastCashbackTime: new Date()
    };
  }

  async getUserWallets(): Promise<Wallets> {
    return {
      cashBalance: "0",
      practiceBalance: "100",
      directWallet: "0",
      luckyDrawWallet: "0"
    };
  }

  async getGameHistory(): Promise<GameResult[]> {
    return [
      {
        timestamp: new Date(),
        selectedNumber: 3,
        winningNumber: 3,
        betAmount: "10",
        isWin: true,
        isPractice: false
      }
    ];
  }

  async getDirectReferrals(): Promise<string[]> {
    return [];
  }

  async getReferralLink(): Promise<string> {
    return `${window.location.origin}?ref=${this.walletState.value.address}`;
  }

  // ===============================
  // ACTIONS (MOCKED)
  // ===============================

  async deposit() { return true; }
  async playGame() { return true; }
  async withdraw() { return true; }
  async claimCashback() { return true; }
  async convertPracticeToCash() { return true; }
  async buyLuckyDrawTickets(quantity: number) { return true; }

  async getCurrentDraw() {
    return {
      drawId: 1,
      ticketsSold: 10,
      ticketsRemaining: 90,
      startTime: new Date(),
      isComplete: false
    };
  }

  async getUserTickets(drawId: number) {
    return 0;
  }

  // ===============================
  // HELPERS
  // ===============================

  shortenAddress(address: string): string {
    if (!address) return '';
    return `${address.slice(0,6)}...${address.slice(-4)}`;
  }

  getExplorerUrl(txHash: string): string {
    return '';
  }

  async refreshBalances() { }

  // ===============================
// USDT APPROVAL (DEV MODE)
// ===============================

async needsApproval(amount: string): Promise<boolean> {
  // In DEV MODE always assume already approved
  return false;
}

async approveUSDT(amount: string): Promise<boolean> {
  console.log('DEV approveUSDT:', amount);
  return true;
}

}

