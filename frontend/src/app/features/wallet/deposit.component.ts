import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavbarComponent } from '../../layout/navbar.component';
import { WalletService } from '../../core/services/wallet.service';
import { Wallet } from '../../models/wallet.model';

@Component({
  selector: 'app-deposit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  templateUrl: './deposit.component.html'
})
export class DepositComponent implements OnInit {
  depositForm: FormGroup;
  wallet: Wallet | null = null;
  loading = false;
  success = '';
  error = '';

  constructor(private fb: FormBuilder, private walletService: WalletService) {
    this.depositForm = this.fb.group({
      amount: [100, [Validators.required, Validators.min(10)]],
      txHash: ['']
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
    if (this.depositForm.invalid) return;

    this.loading = true;
    this.success = '';
    this.error = '';

    this.walletService.deposit(this.depositForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          this.wallet = response.data;
          this.success = 'Deposit successful! Your account has been credited.';
        } else {
          this.error = response.message;
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Deposit failed';
        this.loading = false;
      }
    });
  }
}
