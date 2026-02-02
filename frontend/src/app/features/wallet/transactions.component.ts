import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../layout/navbar.component';
import { WalletService } from '../../core/services/wallet.service';
import { Transaction } from '../../models/wallet.model';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  template: `
    <app-navbar></app-navbar>
    <div class="container py-4">
      <div class="card shadow">
        <div class="card-header bg-secondary text-white">
          <h4 class="mb-0">Transaction History</h4>
        </div>
        <div class="card-body">
          <div *ngIf="loading" class="text-center py-5">
            <div class="spinner-border text-primary"></div>
          </div>

          <div *ngIf="!loading && transactions.length === 0" class="text-center py-5 text-muted">
            No transactions yet
          </div>

          <div class="table-responsive" *ngIf="!loading && transactions.length > 0">
            <table class="table table-hover">
              <thead class="table-light">
                <tr>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Wallet</th>
                  <th>Amount</th>
                  <th>Description</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let tx of transactions">
                  <td>{{ tx.timestamp | date:'short' }}</td>
                  <td>
                    <span class="badge" [class]="getTypeBadgeClass(tx.type)">
                      {{ tx.type | titlecase }}
                    </span>
                  </td>
                  <td>{{ tx.walletType }}</td>
                  <td [class]="isPositive(tx.type) ? 'text-success' : 'text-danger'">
                    {{ isPositive(tx.type) ? '+' : '-' }}{{ tx.amount | number:'1.2-2' }} USDT
                  </td>
                  <td>{{ tx.description }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `
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
