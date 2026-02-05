import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { Web3Service, WalletState, UserInfo } from '../../core/services/web3.service';

@Component({
  selector: 'app-connect-wallet',
  standalone: true,
  imports: [
    CommonModule,
  ],
  templateUrl: './connect-wallet.component.html'
})
export class ConnectWalletComponent implements OnInit {

  // ===========================
  // EXISTING STATE (UNCHANGED)
  // ===========================

  walletState: WalletState | null = null;
  userInfo: UserInfo | null = null;
  loading = false;
  connecting = false;
  registering = false;
  error = '';
  referrerAddress: string | null = null;
  isMetaMaskInstalled = false;

  // ===========================
  // UI STATE (NEW)
  // ===========================

  showWalletModal = false;

  constructor(
    private web3Service: Web3Service,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {

    // MetaMask detection
    this.checkMetaMask();
    setTimeout(() => this.checkMetaMask(), 500);
    setTimeout(() => this.checkMetaMask(), 1500);

    // Referral param
    this.route.queryParams.subscribe(params => {
      if (params['ref']) {
        this.referrerAddress = params['ref'];
      }
    });

    // Wallet state
    this.web3Service.walletState$.subscribe(state => {
      this.walletState = state;
    });

  }

  // ===========================
  // CORE LOGIC (UNCHANGED)
  // ===========================

  private checkMetaMask() {
    if (typeof window !== 'undefined') {
      this.isMetaMaskInstalled = !!(window as any).ethereum;
    }
  }

  async connectWallet() {
    this.connecting = true;
    this.error = '';

    try {
      await this.web3Service.connectWallet();

      setTimeout(() => {
        this.router.navigate(['/register']);
      }, 2000);

    } catch (err: any) {
      this.error = err.message || 'Failed to connect wallet';
    } finally {
      this.connecting = false;
      this.showWalletModal = false;   // close popup after connect
    }
  }

  async checkUserRegistration() {
    this.loading = true;

    try {
      this.userInfo = await this.web3Service.getUserInfo();

      if (this.userInfo?.isRegistered) {
        this.router.navigate(['/dashboard']);
      }

    } catch (err) {
      console.error('Error checking registration:', err);
    } finally {
      this.loading = false;
    }
  }

  async register() {
    this.registering = true;
    this.error = '';

    try {
      await this.web3Service.register();
      this.router.navigate(['/dashboard']);

    } catch (err: any) {
      this.error = err.message || 'Registration failed';
    } finally {
      this.registering = false;
    }
  }

  // ===========================
  // UI HELPERS (SAFE)
  // ===========================

  openWalletModal() {
    this.showWalletModal = true;
  }

  closeWalletModal() {
    this.showWalletModal = false;
  }

  openInstall(url: string) {
    window.open(url, '_blank');
  }

}