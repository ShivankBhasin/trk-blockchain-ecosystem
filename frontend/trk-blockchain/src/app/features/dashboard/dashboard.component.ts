import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../../layout/navbar.component';
import { SidebarComponent } from '../../layout/sidebar.component';
import { Web3Service, WalletState, UserInfo, Wallets, GameResult } from '../../core/services/web3.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent, SidebarComponent],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  walletState: WalletState | null = null;
  userInfo: UserInfo | null = null;
  wallets: Wallets | null = null;
  gameHistory: GameResult[] = [];
  referralLink = '';
  loading = true;

  constructor(private web3Service: Web3Service) {}

  async ngOnInit() {

  this.web3Service.walletState$.subscribe(state => {
    this.walletState = state;
  });

  this.loading = true;

  this.userInfo = await this.web3Service.getUserInfo();
  this.wallets = await this.web3Service.getUserWallets();
  this.gameHistory = await this.web3Service.getGameHistory();
  this.referralLink = await this.web3Service.getReferralLink();

  this.loading = false;
}

  // ngOnInit() {
  //   this.web3Service.walletState$.subscribe(state => {
  //     this.walletState = state;
  //     if (state.isConnected) {
  //       this.loadDashboard();
  //     }
  //   });
  // }

  // async loadDashboard() {
  //   this.loading = true;
  //   try {
  //     this.userInfo = await this.web3Service.getUserInfo();
  //     this.wallets = await this.web3Service.getUserWallets();
  //     this.gameHistory = await this.web3Service.getGameHistory(5);
  //     this.referralLink = await this.web3Service.getReferralLink();
  //   } catch (error) {
  //     console.error('Error loading dashboard:', error);
  //   } finally {
  //     this.loading = false;
  //   }
  // }

  getCashbackProgress(): number {
    if (!this.userInfo) return 0;
    const losses = parseFloat(this.userInfo.totalLosses);
    const received = parseFloat(this.userInfo.cashbackReceived);
    if (losses === 0) return 0;
    return (received / losses) * 100;
  }

async claimCashback() {
  await this.web3Service.claimCashback();

  this.userInfo = await this.web3Service.getUserInfo();
  this.wallets = await this.web3Service.getUserWallets();
  this.gameHistory = await this.web3Service.getGameHistory();
  this.referralLink = await this.web3Service.getReferralLink();
}

  // async claimCashback() {
  //   try {
  //     await this.web3Service.claimCashback();
  //     await this.loadDashboard();
  //   } catch (error: any) {
  //     alert(error.message || 'Failed to claim cashback');
  //   }
  // }

  copyReferralLink() {
    navigator.clipboard.writeText(this.referralLink);
    alert('Referral link copied!');
  }

  shortenAddress(address: string | null | undefined): string {
    if (!address) return '';
    return `${address.slice(0, 6)}...${address.slice(-4)}`;
  }

  // Helper for template to access parseFloat
  parseFloat(value: string): number {
    return parseFloat(value) || 0;
  }
}
