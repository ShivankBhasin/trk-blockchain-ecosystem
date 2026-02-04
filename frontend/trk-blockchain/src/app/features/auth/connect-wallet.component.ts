import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { Web3Service, WalletState, UserInfo } from '../../core/services/web3.service';

@Component({
  selector: 'app-connect-wallet',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './connect-wallet.component.html'
})
export class ConnectWalletComponent implements OnInit {
  walletState: WalletState | null = null;
  userInfo: UserInfo | null = null;
  loading = false;
  connecting = false;
  registering = false;
  error = '';
  referrerAddress: string | null = null;
  isMetaMaskInstalled = false;

  constructor(
    private web3Service: Web3Service,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // Check MetaMask installation (with delay for async injection)
    this.checkMetaMask();
    setTimeout(() => this.checkMetaMask(), 500);
    setTimeout(() => this.checkMetaMask(), 1500);

    // Check for referrer in URL
    this.route.queryParams.subscribe(params => {
      if (params['ref']) {
        this.referrerAddress = params['ref'];
      }
    });

    // Subscribe to wallet state
    this.web3Service.walletState$.subscribe(async state => {
      this.walletState = state;
      if (state.isConnected && state.address) {
        await this.checkUserRegistration();
      }
    });
  }

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
        this.router.navigate(['/dashboard']);
      }, 2000);
    } catch (err: any) {
      this.error = err.message || 'Failed to connect wallet';
    } finally {
      this.connecting = false;
    }
  }

  async checkUserRegistration() {
    this.loading = true;
    try {
      this.userInfo = await this.web3Service.getUserInfo();
      if (this.userInfo?.isRegistered) {
        // User is registered, redirect to dashboard
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
      // await this.web3Service.register(this.referrerAddress || undefined);
      await this.web3Service.register();
      // After registration, redirect to dashboard
      this.router.navigate(['/dashboard']);
    } catch (err: any) {
      this.error = err.message || 'Registration failed';
    } finally {
      this.registering = false;
    }
  }

}
