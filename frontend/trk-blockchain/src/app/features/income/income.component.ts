import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../layout/navbar.component';
import { Web3Service, UserInfo, Wallets } from '../../core/services/web3.service';

interface IncomeOverview {
  totalIncome: string;
  winnersIncome: string;
  directLevelIncome: string;
  winnerLevelIncome: string;
  cashbackIncome: string;
  roiOnRoiIncome: string;
  clubIncome: string;
  luckyDrawIncome: string;
}

@Component({
  selector: 'app-income',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './income.component.html'
})
export class IncomeComponent implements OnInit {
  userInfo: UserInfo | null = null;
  wallets: Wallets | null = null;
  income: IncomeOverview | null = null;
  loading = true;

  // Income stream descriptions
  incomeStreams = [
    {
      name: 'Winners 8X Income',
      key: 'winnersIncome',
      color: 'success',
      description: 'Win 8X your bet (2X to Direct Wallet + 6X compounds)',
      icon: '🎮'
    },
    {
      name: 'Direct Level Income',
      key: 'directLevelIncome',
      color: 'primary',
      description: 'Earn 5-0.5% from team deposits (15 levels)',
      icon: '👥'
    },
    {
      name: 'Winner Level Income',
      key: 'winnerLevelIncome',
      color: 'info',
      description: 'Earn 5-0.5% from team wins (15 levels)',
      icon: '🏆'
    },
    {
      name: 'Cashback Protection',
      key: 'cashbackIncome',
      color: 'warning',
      description: 'Recover 0.5% of losses daily (activates at 100 USDT loss)',
      icon: '🛡️'
    },
    {
      name: 'ROI on ROI',
      key: 'roiOnRoiIncome',
      color: 'secondary',
      description: 'Earn 10% of team cashback claims (5 levels)',
      icon: '📈'
    },
    {
      name: 'Club Income',
      key: 'clubIncome',
      color: 'danger',
      description: 'Share 8% of daily platform turnover (rank-based)',
      icon: '💎'
    },
    {
      name: 'Lucky Draw Income',
      key: 'luckyDrawIncome',
      color: 'dark',
      description: 'Win up to 10,000 USDT in weekly draws',
      icon: '🎰'
    }
  ];

  constructor(private web3Service: Web3Service) {}

  ngOnInit() {
    this.loadIncome();
  }

  async loadIncome() {
    this.loading = true;
    try {
      this.userInfo = await this.web3Service.getUserInfo();
      this.wallets = await this.web3Service.getUserWallets();

      // Calculate income from available data
      // In a full implementation, these would come from contract events or a subgraph
      if (this.userInfo && this.wallets) {
        const cashbackReceived = parseFloat(this.userInfo.cashbackReceived) || 0;
        const directWallet = parseFloat(this.wallets.directWallet) || 0;
        const luckyDrawWallet = parseFloat(this.wallets.luckyDrawWallet) || 0;

        this.income = {
          totalIncome: (directWallet + cashbackReceived + luckyDrawWallet).toFixed(2),
          winnersIncome: directWallet.toFixed(2), // From game wins
          directLevelIncome: '0.00', // Would need event tracking
          winnerLevelIncome: '0.00', // Would need event tracking
          cashbackIncome: cashbackReceived.toFixed(2),
          roiOnRoiIncome: '0.00', // Would need event tracking
          clubIncome: '0.00', // From club contract
          luckyDrawIncome: luckyDrawWallet.toFixed(2)
        };
      }
    } catch (error) {
      console.error('Error loading income:', error);
    } finally {
      this.loading = false;
    }
  }

  getIncomeValue(key: string): string {
    if (!this.income) return '0.00';
    return (this.income as any)[key] || '0.00';
  }
}
