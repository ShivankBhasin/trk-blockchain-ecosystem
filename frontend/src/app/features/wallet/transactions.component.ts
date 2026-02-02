import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../layout/navbar.component';
import { WalletService } from '../../core/services/wallet.service';
import { Transaction } from '../../models/wallet.model';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  template: './transactions.component.html'
})
export class TransactionsComponent implements OnInit {
  transactions: Transaction[] = [];
  loading = true;

  constructor(private walletService: WalletService) {}

  ngOnInit() {
    this.loadTransactions();
  }

  loadTransactions() {
    this.walletService.getTransactions().subscribe({
      next: (response) => {
        if (response.success) {
          this.transactions = response.data;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  getTypeBadgeClass(type: string): string {
    const classes: Record<string, string> = {
      'DEPOSIT': 'bg-success',
      'WITHDRAWAL': 'bg-warning',
      'GAME_WIN': 'bg-success',
      'GAME_LOSS': 'bg-danger',
      'REFERRAL_INCOME': 'bg-info',
      'CASHBACK': 'bg-primary',
      'TRANSFER': 'bg-secondary',
      'LUCKY_DRAW_ENTRY': 'bg-warning',
      'LUCKY_DRAW_WIN': 'bg-success'
    };
    return classes[type] || 'bg-secondary';
  }

  isPositive(type: string): boolean {
    return ['DEPOSIT', 'GAME_WIN', 'REFERRAL_INCOME', 'CASHBACK', 'LUCKY_DRAW_WIN'].includes(type);
  }
}
