import { Injectable } from '@angular/core';
import { BrowserProvider, Contract, JsonRpcSigner, formatEther, parseEther } from 'ethers';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// Contract ABIs (simplified)
const TRK_BLOCKCHAIN_ABI = [
  "function register(address _referrer) external",
  "function deposit(uint256 _amount) external",
  "function playGame(uint256 _betAmount, uint8 _selectedNumber, bool _isPractice) external",
  "function withdraw(uint256 _amount) external",
  "function claimCashback() external",
  "function convertPracticeToCash() external",
  "function getUserInfo(address _user) external view returns (tuple(bool isRegistered, address referrer, uint256 registrationTime, uint256 totalDeposited, uint256 totalWithdrawn, uint256 directReferrals, uint256 teamSize, bool isActivated, bool isPremiumActivated, uint256 totalLosses, uint256 cashbackReceived, uint256 lastCashbackTime))",
  "function getUserWallets(address _user) external view returns (tuple(uint256 cashBalance, uint256 practiceBalance, uint256 directWallet, uint256 luckyDrawWallet))",
  "function getDirectReferrals(address _user) external view returns (address[])",
  "function getGameHistory(address _user, uint256 _limit) external view returns (tuple(uint256 timestamp, uint8 selectedNumber, uint8 winningNumber, uint256 betAmount, bool isWin, bool isPractice)[])",
  "function getReferralLink(address _user) external pure returns (string)",
  "function totalUsers() external view returns (uint256)",
  "function totalGamesPlayed() external view returns (uint256)",
  "function totalVolume() external view returns (uint256)",
  "event UserRegistered(address indexed user, address indexed referrer, uint256 timestamp)",
  "event Deposit(address indexed user, uint256 amount, uint256 timestamp)",
  "event GamePlayed(address indexed user, bool isPractice, uint256 betAmount, uint8 selected, uint8 winning, bool isWin, uint256 payout)",
  "event Withdrawal(address indexed user, uint256 amount, uint256 fee, uint256 timestamp)"
];

const TRK_LUCKY_DRAW_ABI = [
  "function buyTickets(uint256 _quantity) external",
  "function getCurrentDraw() external view returns (uint256 drawId, uint256 ticketsSold, uint256 ticketsRemaining, uint256 startTime, bool isComplete)",
  "function getUserTickets(uint256 _drawId, address _user) external view returns (uint256)",
  "function getDrawWinners(uint256 _drawId) external view returns (address[])",
  "event TicketPurchased(address indexed user, uint256 drawId, uint256 ticketCount, uint256 totalCost)",
  "event DrawCompleted(uint256 indexed drawId, uint256 endTime)",
  "event PrizeAwarded(uint256 indexed drawId, address indexed winner, uint256 rank, uint256 amount)"
];

const USDT_ABI = [
  "function approve(address spender, uint256 amount) external returns (bool)",
  "function allowance(address owner, address spender) external view returns (uint256)",
  "function balanceOf(address account) external view returns (uint256)",
  "function transfer(address to, uint256 amount) external returns (bool)"
];

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
  private MOCK_MODE = true;
  private provider: BrowserProvider | null = null;
  private signer: JsonRpcSigner | null = null;
  private trkContract: Contract | null = null;
  private luckyDrawContract: Contract | null = null;
  private usdtContract: Contract | null = null;

  private walletState = new BehaviorSubject<WalletState>({
    isConnected: false,
    address: null,
    chainId: null,
    balance: null,
    usdtBalance: null
  });

  walletState$ = this.walletState.asObservable();

  // BSC Chain IDs
  private readonly BSC_MAINNET = 56;
  private readonly BSC_TESTNET = 97;

  constructor() {
    // this.checkExistingConnection();
    this.setupEventListeners();
  }

  // private async checkExistingConnection() {
  //   if (typeof window !== 'undefined' && (window as any).ethereum) {
  //     const accounts = await (window as any).ethereum.request({ method: 'eth_accounts' });
  //     if (accounts.length > 0) {
  //       await this.connectWallet();
  //     }
  //   }
  // }

  private setupEventListeners() {
    if (typeof window !== 'undefined' && (window as any).ethereum) {
      (window as any).ethereum.on('accountsChanged', (accounts: string[]) => {
        if (accounts.length === 0) {
          this.disconnect();
        } else {
          this.connectWallet();
        }
      });

      (window as any).ethereum.on('chainChanged', () => {
        window.location.reload();
      });
    }
  }

  async connectWallet(): Promise<boolean> {
  if (typeof window === 'undefined' || !(window as any).ethereum) {
    throw new Error('MetaMask not installed');
  }

  const accounts = await (window as any).ethereum.request({
    method: 'eth_requestAccounts'
  });

  const address = accounts[0];

  this.walletState.next({
    isConnected: true,
    address: address,
    chainId: 0,
    balance: "5.0",
    usdtBalance: "1000"
  });

  return true;
}

  // async connectWallet(): Promise<boolean> {
  //   if (typeof window === 'undefined' || !(window as any).ethereum) {
  //     throw new Error('MetaMask not installed. Please install MetaMask to use this app.');
  //   }

  //   try {
  //     this.provider = new BrowserProvider((window as any).ethereum);
  //     await this.provider.send('eth_requestAccounts', []);

  //     const network = await this.provider.getNetwork();
  //     const chainId = Number(network.chainId);

  //     // Check if on BSC
  //     if (chainId !== this.BSC_MAINNET && chainId !== this.BSC_TESTNET) {
  //       await this.switchToBSC();
  //     }

  //     this.signer = await this.provider.getSigner();
  //     const address = await this.signer.getAddress();
  //     const balance = await this.provider.getBalance(address);

  //     // Initialize contracts
  //     this.initializeContracts();

  //     // Get USDT balance
  //     let usdtBalance = '0';
  //     if (this.usdtContract) {
  //       const usdtBal = await this.usdtContract['balanceOf'](address);
  //       usdtBalance = formatEther(usdtBal);
  //     }

  //     this.walletState.next({
  //       isConnected: true,
  //       address,
  //       chainId,
  //       balance: formatEther(balance),
  //       usdtBalance
  //     });

  //     return true;
  //   } catch (error) {
  //     console.error('Failed to connect wallet:', error);
  //     throw error;
  //   }
  // }

  private async switchToBSC() {
    try {
      await (window as any).ethereum.request({
        method: 'wallet_switchEthereumChain',
        params: [{ chainId: '0x38' }] // BSC Mainnet
      });
    } catch (switchError: any) {
      // Chain not added, add it
      if (switchError.code === 4902) {
        await (window as any).ethereum.request({
          method: 'wallet_addEthereumChain',
          params: [{
            chainId: '0x38',
            chainName: 'BNB Smart Chain',
            nativeCurrency: {
              name: 'BNB',
              symbol: 'BNB',
              decimals: 18
            },
            rpcUrls: ['https://bsc-dataseed.binance.org/'],
            blockExplorerUrls: ['https://bscscan.com/']
          }]
        });
      } else {
        throw switchError;
      }
    }
  }

  private initializeContracts() {
    if (!this.signer) return;

    this.trkContract = new Contract(
      environment.contracts.trkBlockchain,
      TRK_BLOCKCHAIN_ABI,
      this.signer
    );

    this.luckyDrawContract = new Contract(
      environment.contracts.luckyDraw,
      TRK_LUCKY_DRAW_ABI,
      this.signer
    );

    this.usdtContract = new Contract(
      environment.contracts.usdt,
      USDT_ABI,
      this.signer
    );
  }

  disconnect() {
    this.provider = null;
    this.signer = null;
    this.trkContract = null;
    this.luckyDrawContract = null;
    this.usdtContract = null;

    this.walletState.next({
      isConnected: false,
      address: null,
      chainId: null,
      balance: null,
      usdtBalance: null
    });
  }

  // ============ USDT Approval Methods ============

  async needsApproval(amount: string): Promise<boolean> {
    if (!this.usdtContract) return true;
    const address = this.walletState.value.address;
    if (!address) return true;

    try {
      const amountWei = parseEther(amount);
      const allowance = await this.usdtContract['allowance'](address, environment.contracts.trkBlockchain);
      return allowance < amountWei;
    } catch (error) {
      console.error('Error checking allowance:', error);
      return true;
    }
  }

  async approveUSDT(amount: string): Promise<any> {
    if (!this.usdtContract) throw new Error('USDT contract not initialized');

    const amountWei = parseEther(amount);
    const tx = await this.usdtContract['approve'](environment.contracts.trkBlockchain, amountWei);
    return tx.wait();
  }

  // ============ Contract Methods ============

  async register() { return true; }
async deposit() { return true; }
async playGame() { return true; }
async withdraw() { return true; }
async claimCashback() { return true; }
async convertPracticeToCash() { return true; }

  // async register(referrerAddress?: string): Promise<any> {
  //   if (!this.trkContract) throw new Error('Contract not initialized');
  //   const referrer = referrerAddress || '0x0000000000000000000000000000000000000000';
  //   const tx = await this.trkContract['register'](referrer);
  //   return tx.wait();
  // }

  // async deposit(amount: string): Promise<any> {
  //   if (!this.trkContract || !this.usdtContract) throw new Error('Contract not initialized');

  //   const amountWei = parseEther(amount);

    // Approve USDT spending
  //   const approveTx = await this.usdtContract['approve'](environment.contracts.trkBlockchain, amountWei);
  //   await approveTx.wait();

  //   // Deposit
  //   const tx = await this.trkContract['deposit'](amountWei);
  //   return tx.wait();
  // }

  // async playGame(betAmount: string, selectedNumber: number, isPractice: boolean): Promise<any> {
  //   if (!this.trkContract) throw new Error('Contract not initialized');
  //   const amountWei = parseEther(betAmount);
  //   const tx = await this.trkContract['playGame'](amountWei, selectedNumber, isPractice);
  //   return tx.wait();
  // }

  // async withdraw(amount: string): Promise<any> {
  //   if (!this.trkContract) throw new Error('Contract not initialized');
  //   const amountWei = parseEther(amount);
  //   const tx = await this.trkContract['withdraw'](amountWei);
  //   return tx.wait();
  // }

  // async claimCashback(): Promise<any> {
  //   if (!this.trkContract) throw new Error('Contract not initialized');
  //   const tx = await this.trkContract['claimCashback']();
  //   return tx.wait();
  // }

  // async convertPracticeToCash(): Promise<any> {
  //   if (!this.trkContract) throw new Error('Contract not initialized');
  //   const tx = await this.trkContract['convertPracticeToCash']();
  //   return tx.wait();
  // }

  // ============ View Methods ============

  async getUserInfo(): Promise<UserInfo | null> {
  return {
    isRegistered: true,
    referrer: "0x0000000000000000000000000000000000000000",
    registrationTime: new Date(),
    totalDeposited: "120",
    totalWithdrawn: "20",
    directReferrals: 4,
    teamSize: 12,
    isActivated: true,
    isPremiumActivated: true,
    totalLosses: "50",
    cashbackReceived: "10",
    lastCashbackTime: new Date()
  };
}

  // async getUserInfo(address?: string): Promise<UserInfo | null> {
  //   if (!this.trkContract) return null;
  //   const addr = address || this.walletState.value.address;
  //   if (!addr) return null;

  //   try {
  //     const info = await this.trkContract['getUserInfo'](addr);
  //     return {
  //       isRegistered: info.isRegistered,
  //       referrer: info.referrer,
  //       registrationTime: new Date(Number(info.registrationTime) * 1000),
  //       totalDeposited: formatEther(info.totalDeposited),
  //       totalWithdrawn: formatEther(info.totalWithdrawn),
  //       directReferrals: Number(info.directReferrals),
  //       teamSize: Number(info.teamSize),
  //       isActivated: info.isActivated,
  //       isPremiumActivated: info.isPremiumActivated,
  //       totalLosses: formatEther(info.totalLosses),
  //       cashbackReceived: formatEther(info.cashbackReceived),
  //       lastCashbackTime: new Date(Number(info.lastCashbackTime) * 1000)
  //     };
  //   } catch (error) {
  //     console.error('Error getting user info:', error);
  //     return null;
  //   }
  // }

  async getUserWallets(): Promise<Wallets | null> {
  return {
    cashBalance: "300",
    practiceBalance: "100",
    directWallet: "80",
    luckyDrawWallet: "20"
  };
}

  // async getUserWallets(address?: string): Promise<Wallets | null> {
  //   if (!this.trkContract) return null;
  //   const addr = address || this.walletState.value.address;
  //   if (!addr) return null;

  //   try {
  //     const wallets = await this.trkContract['getUserWallets'](addr);
  //     return {
  //       cashBalance: formatEther(wallets.cashBalance),
  //       practiceBalance: formatEther(wallets.practiceBalance),
  //       directWallet: formatEther(wallets.directWallet),
  //       luckyDrawWallet: formatEther(wallets.luckyDrawWallet)
  //     };
  //   } catch (error) {
  //     console.error('Error getting wallets:', error);
  //     return null;
  //   }
  // }

  async getGameHistory(): Promise<GameResult[]> {
  return [
    {
      timestamp: new Date(),
      selectedNumber: 3,
      winningNumber: 3,
      betAmount: "10",
      isWin: true,
      isPractice: false
    },
    {
      timestamp: new Date(),
      selectedNumber: 5,
      winningNumber: 2,
      betAmount: "5",
      isWin: false,
      isPractice: true
    }
  ];
}

  // async getGameHistory(limit: number = 10): Promise<GameResult[]> {
  //   if (!this.trkContract) return [];
  //   const address = this.walletState.value.address;
  //   if (!address) return [];

  //   try {
  //     const history = await this.trkContract['getGameHistory'](address, limit);
  //     return history.map((game: any) => ({
  //       timestamp: new Date(Number(game.timestamp) * 1000),
  //       selectedNumber: Number(game.selectedNumber),
  //       winningNumber: Number(game.winningNumber),
  //       betAmount: formatEther(game.betAmount),
  //       isWin: game.isWin,
  //       isPractice: game.isPractice
  //     }));
  //   } catch (error) {
  //     console.error('Error getting game history:', error);
  //     return [];
  //   }
  // }

  async getDirectReferrals(): Promise<string[]> {
    if (!this.trkContract) return [];
    const address = this.walletState.value.address;
    if (!address) return [];

    try {
      return await this.trkContract['getDirectReferrals'](address);
    } catch (error) {
      console.error('Error getting referrals:', error);
      return [];
    }
  }

  async getReferralLink(): Promise<string> {
  return `${window.location.origin}?ref=${this.walletState.value.address}`;
}

  // async getReferralLink(): Promise<string> {
  //   if (!this.trkContract) return '';
  //   const address = this.walletState.value.address;
  //   if (!address) return '';

  //   try {
  //     return await this.trkContract['getReferralLink'](address);
  //   } catch (error) {
  //     return `${window.location.origin}?ref=${address}`;
  //   }
  // }

  // ============ Lucky Draw Methods ============

  async buyLuckyDrawTickets(quantity: number): Promise<any> {
    if (!this.luckyDrawContract || !this.usdtContract) throw new Error('Contract not initialized');

    const totalCost = parseEther((quantity * 10).toString()); // 10 USDT per ticket

    // Approve USDT spending
    const approveTx = await this.usdtContract['approve'](environment.contracts.luckyDraw, totalCost);
    await approveTx.wait();

    // Buy tickets
    const tx = await this.luckyDrawContract['buyTickets'](quantity);
    return tx.wait();
  }

  async getCurrentDraw(): Promise<any> {
    if (!this.luckyDrawContract) return null;

    try {
      const draw = await this.luckyDrawContract['getCurrentDraw']();
      return {
        drawId: Number(draw.drawId),
        ticketsSold: Number(draw.ticketsSold),
        ticketsRemaining: Number(draw.ticketsRemaining),
        startTime: new Date(Number(draw.startTime) * 1000),
        isComplete: draw.isComplete
      };
    } catch (error) {
      console.error('Error getting current draw:', error);
      return null;
    }
  }

  async getUserTickets(drawId: number): Promise<number> {
    if (!this.luckyDrawContract) return 0;
    const address = this.walletState.value.address;
    if (!address) return 0;

    try {
      const tickets = await this.luckyDrawContract['getUserTickets'](drawId, address);
      return Number(tickets);
    } catch (error) {
      return 0;
    }
  }

  // ============ Utility Methods ============

  shortenAddress(address: string): string {
    if (!address) return '';
    return `${address.slice(0, 6)}...${address.slice(-4)}`;
  }

  getExplorerUrl(txHash: string): string {
    const chainId = this.walletState.value.chainId;
    if (chainId === this.BSC_MAINNET) {
      return `https://bscscan.com/tx/${txHash}`;
    } else if (chainId === this.BSC_TESTNET) {
      return `https://testnet.bscscan.com/tx/${txHash}`;
    }
    return '';
  }

  async refreshBalances() {
    if (!this.provider || !this.walletState.value.address) return;

    const address = this.walletState.value.address;
    const balance = await this.provider.getBalance(address);

    let usdtBalance = '0';
    if (this.usdtContract) {
      const usdtBal = await this.usdtContract['balanceOf'](address);
      usdtBalance = formatEther(usdtBal);
    }

    this.walletState.next({
      ...this.walletState.value,
      balance: formatEther(balance),
      usdtBalance
    });
  }
}
