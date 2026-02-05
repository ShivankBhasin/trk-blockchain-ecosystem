import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Web3Service, WalletState, Wallets } from '../core/services/web3.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css' 
})
export class NavbarComponent implements OnInit {
  walletState: WalletState | null = null;
  wallets: Wallets | null = null;

  constructor(
    private web3Service: Web3Service,
    private router: Router
  ) {}

  private isRegistered(): boolean {
  return localStorage.getItem('is_registered') === 'true';
}

openDashboard() {
  if (!this.isRegistered()) {
    alert('To view Dashboard please register first.');
    return;
  }
  this.router.navigate(['/dashboard']);
}

openGame() {
  if (!this.isRegistered()) {
    alert('To play games please register first.');
    return;
  }
  this.router.navigate(['/game']);
}

openReferral() {
  if (!this.isRegistered()) {
    alert('To access referrals please register first.');
    return;
  }
  this.router.navigate(['/referral']);
}

openIncome() {
  if (!this.isRegistered()) {
    alert('To view Income details please register first.');
    return;
  }
  this.router.navigate(['/income']);
}

openLuckyDraw() {
  if (!this.isRegistered()) {
    alert('To participate in the Lucky Draw please register first.');
    return;
  }
  this.router.navigate(['/lucky-draw']);
}

  ngOnInit() {
    this.web3Service.walletState$.subscribe(async state => {
      this.walletState = state;
      if (state.isConnected) {
        this.wallets = await this.web3Service.getUserWallets();
      }
    });
  }

  // disconnect(event: Event) {
  //   event.preventDefault();
  //   this.web3Service.disconnect();
  //   this.router.navigate(['/connect']);
  // }

  disconnect() {
  localStorage.clear();
  window.location.href = '/connect';
}

  shortenAddress(address: string | null | undefined): string {
    if (!address) return '';
    return `${address.slice(0, 6)}...${address.slice(-4)}`;
  }

  async refreshBalances() {
    await this.web3Service.refreshBalances();
    this.wallets = await this.web3Service.getUserWallets();
  }

  errorMessage = '';

showError(msg:string){
  this.errorMessage = msg;
  setTimeout(() => this.errorMessage = '', 3000);
}
}
