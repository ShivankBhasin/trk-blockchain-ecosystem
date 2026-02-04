import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../layout/navbar.component';
import { Web3Service, UserInfo, WalletState } from '../../core/services/web3.service';

interface ReferralStats {
  directReferrals: number;
  teamSize: number;
  totalEarnings: string;
}

@Component({
  selector: 'app-referral',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './referral.component.html'
})
export class ReferralComponent implements OnInit {
  Math = Math; // Expose Math for template
  walletState: WalletState | null = null;
  userInfo: UserInfo | null = null;
  referralLink = '';
  referralStats: ReferralStats | null = null;
  loading = true;
  copied = false;

  // Commission structure per level
  commissionStructure = [
    { level: 1, deposit: 5, winner: 5, requirement: '1 Direct' },
    { level: 2, deposit: 2, winner: 2, requirement: '2 Directs' },
    { level: 3, deposit: 1, winner: 1, requirement: '3 Directs' },
    { level: 4, deposit: 1, winner: 1, requirement: '4 Directs' },
    { level: 5, deposit: 1, winner: 1, requirement: '5 Directs' },
    { level: 6, deposit: 0.5, winner: 0.5, requirement: '6 Directs' },
    { level: 7, deposit: 0.5, winner: 0.5, requirement: '7 Directs' },
    { level: 8, deposit: 0.5, winner: 0.5, requirement: '8 Directs' },
    { level: 9, deposit: 0.5, winner: 0.5, requirement: '9 Directs' },
    { level: 10, deposit: 0.5, winner: 0.5, requirement: '10 Directs' },
    { level: '11-15', deposit: 0.5, winner: 0.5, requirement: '10 Directs + Premium' }
  ];

  constructor(private web3Service: Web3Service) {}

  ngOnInit() {
    this.web3Service.walletState$.subscribe(state => {
      this.walletState = state;
    });
    this.loadReferralInfo();
  }

  async loadReferralInfo() {
    this.loading = true;
    try {
      this.userInfo = await this.web3Service.getUserInfo();
      this.referralLink = await this.web3Service.getReferralLink();

      if (this.userInfo) {
        this.referralStats = {
          directReferrals: this.userInfo.directReferrals,
          teamSize: this.userInfo.teamSize,
          totalEarnings: this.userInfo.totalDeposited // This would need a dedicated method in contract
        };
      }
    } catch (error) {
      console.error('Error loading referral info:', error);
    } finally {
      this.loading = false;
    }
  }

  copyLink() {
    navigator.clipboard.writeText(this.referralLink);
    this.copied = true;
    setTimeout(() => this.copied = false, 2000);
  }

  shortenAddress(address: string | null): string {
    if (!address) return '';
    return `${address.slice(0, 6)}...${address.slice(-4)}`;
  }

  getUnlockedLevels(): number {
    if (!this.userInfo) return 0;
    const directs = this.userInfo.directReferrals;
    if (this.userInfo.isPremiumActivated && directs >= 10) return 15;
    return Math.min(directs, 10);
  }

  // Helper to get numeric level for comparison (handles '11-15' as 11)
  getNumericLevel(level: number | string): number {
    return typeof level === 'number' ? level : 11;
  }

  isLevelUnlocked(level: number | string): boolean {
    return this.getNumericLevel(level) <= this.getUnlockedLevels();
  }
}
