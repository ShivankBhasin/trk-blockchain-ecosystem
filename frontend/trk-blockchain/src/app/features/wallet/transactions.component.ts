import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../layout/navbar.component';
import { Web3Service, WalletState, GameResult } from '../../core/services/web3.service';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './transactions.component.html'
})
export class TransactionsComponent implements OnInit {
  walletState: WalletState | null = null;
  gameHistory: GameResult[] = [];
  loading = true;

  constructor(private web3Service: Web3Service) {}

  ngOnInit() {
    this.web3Service.walletState$.subscribe(state => {
      this.walletState = state;
    });
    this.loadTransactions();
  }

  async loadTransactions() {
    this.loading = true;
    try {
      // Load recent game history from contract
      // this.gameHistory = await this.web3Service.getGameHistory(20);
      this.gameHistory = await this.web3Service.getGameHistory();
    } catch (error) {
      console.error('Error loading transactions:', error);
    } finally {
      this.loading = false;
    }
  }

  getExplorerUrl(): string {
    if (!this.walletState?.address) return '#';
    return this.web3Service.getExplorerUrl(this.walletState.address);
  }

  getExplorerAddressUrl(): string {
    if (!this.walletState?.address) return '#';
    // Get base URL without /tx/ suffix for address lookup
    const baseUrl = this.web3Service.getExplorerUrl('').replace('/tx/', '/address/');
    return `${baseUrl}${this.walletState.address}`;
  }
}
