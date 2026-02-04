import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Web3Service, WalletState, Wallets } from '../core/services/web3.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html'
})
export class NavbarComponent implements OnInit {
  walletState: WalletState | null = null;
  wallets: Wallets | null = null;

  constructor(
    private web3Service: Web3Service,
    private router: Router
  ) {}

  ngOnInit() {
    this.web3Service.walletState$.subscribe(async state => {
      this.walletState = state;
      if (state.isConnected) {
        this.wallets = await this.web3Service.getUserWallets();
      }
    });
  }

  disconnect(event: Event) {
    event.preventDefault();
    this.web3Service.disconnect();
    this.router.navigate(['/connect']);
  }

  shortenAddress(address: string | null | undefined): string {
    if (!address) return '';
    return `${address.slice(0, 6)}...${address.slice(-4)}`;
  }

  async refreshBalances() {
    await this.web3Service.refreshBalances();
    this.wallets = await this.web3Service.getUserWallets();
  }
}
