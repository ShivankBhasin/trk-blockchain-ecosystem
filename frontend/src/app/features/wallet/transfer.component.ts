import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavbarComponent } from '../../layout/navbar.component';
import { WalletService } from '../../core/services/wallet.service';
import { Wallet } from '../../models/wallet.model';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  template: `
    <app-navbar></app-navbar>
    <div class="container py-4">
      <div class="row justify-content-center">
        <div class="col-md-6">
          <div class="card shadow">
            <div class="card-header bg-info text-white">
              <h4 class="mb-0">Transfer Between Wallets</h4>
            </div>
            <div class="card-body">
              <div *ngIf="success" class="alert alert-success">{{ success }}</div>
              <div *ngIf="error" class="alert alert-danger">{{ error }}</div>

              <div class="mb-4" *ngIf="wallet">
                <div class="row text-center">
                  <div class="col-6">
                    <div class="card bg-light">
                      <div class="card-body py-2">
                        <small class="text-muted">Practice</small>
                        <h6 class="mb-0">{{ wallet.practiceBalance | number:'1.2-2' }}</h6>
                      </div>
                    </div>
                  </div>
                  <div class="col-6">
                    <div class="card bg-light">
                      <div class="card-body py-2">
                        <small class="text-muted">Cash</small>
                        <h6 class="mb-0">{{ wallet.cashBalance | number:'1.2-2' }}</h6>
                      </div>
                    </div>
                  </div>
                  <div class="col-6 mt-2">
                    <div class="card bg-light">
                      <div class="card-body py-2">
                        <small class="text-muted">Direct</small>
                        <h6 class="mb-0">{{ wallet.directWallet | number:'1.2-2' }}</h6>
                      </div>
                    </div>
                  </div>
                  <div class="col-6 mt-2">
                    <div class="card bg-light">
                      <div class="card-body py-2">
                        <small class="text-muted">Lucky Draw</small>
                        <h6 class="mb-0">{{ wallet.luckyDrawWallet | number:'1.2-2' }}</h6>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <form [formGroup]="transferForm" (ngSubmit)="onSubmit()">
                <div class="mb-3">
                  <label class="form-label">From Wallet</label>
                  <select class="form-select" formControlName="fromWallet">
                    <option value="PRACTICE">Practice Balance</option>
                    <option value="CASH">Cash Balance</option>
                    <option value="DIRECT">Direct Wallet</option>
                    <option value="LUCKY_DRAW">Lucky Draw Wallet</option>
                  </select>
                </div>

                <div class="mb-3">
                  <label class="form-label">To Wallet</label>
                  <select class="form-select" formControlName="toWallet">
                    <option value="CASH">Cash Balance</option>
                    <option value="DIRECT">Direct Wallet</option>
                    <option value="LUCKY_DRAW">Lucky Draw Wallet</option>
                  </select>
                </div>

                <div class="mb-3">
                  <label class="form-label">Amount (USDT)</label>
                  <input type="number" class="form-control" formControlName="amount" min="1">
                </div>

                <div class="d-grid">
                  <button type="submit" class="btn btn-info btn-lg" [disabled]="loading || transferForm.invalid">
                    <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
                    Transfer
                  </button>
                </div>
              </form>

              <div class="alert alert-info mt-3 small">
                <strong>Note:</strong> Practice balance can only be transferred after depositing 100+ USDT total.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class TransferComponent implements OnInit {
  transferForm: FormGroup;
  wallet: Wallet | null = null;
  loading = false;
  success = '';
  error = '';

  constructor(private fb: FormBuilder, private walletService: WalletService) {
    this.transferForm = this.fb.group({
      fromWallet: ['DIRECT', Validators.required],
      toWallet: ['CASH', Validators.required],
      amount: [10, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit() {
    this.loadWallet();
  }

  loadWallet() {
    this.walletService.getWallet().subscribe({
      next: (response) => {
        if (response.success) {
          this.wallet = response.data;
        }
      }
    });
  }

  onSubmit() {
    if (this.transferForm.invalid) return;

    this.loading = true;
    this.success = '';
    this.error = '';

    this.walletService.transfer(this.transferForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          this.wallet = response.data;
          this.success = 'Transfer successful!';
        } else {
          this.error = response.message;
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Transfer failed';
        this.loading = false;
      }
    });
  }
}
