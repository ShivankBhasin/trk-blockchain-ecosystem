import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavbarComponent } from '../../layout/navbar.component';
import { WalletService } from '../../core/services/wallet.service';
import { Wallet } from '../../models/wallet.model';

@Component({
  selector: 'app-withdraw',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  templateUrl: './withdraw.component.html'
})
export class WithdrawComponent implements OnInit {
  withdrawForm: FormGroup;
  wallet: Wallet | null = null;
  loading = false;
  success = '';
  error = '';

  constructor(private fb: FormBuilder, private walletService: WalletService) {
    this.withdrawForm = this.fb.group({
      amount: [100, [Validators.required, Validators.min(5), Validators.max(5000)]],
      walletAddress: ['', Validators.required]
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
    if (this.withdrawForm.invalid) return;

    this.loading = true;
    this.success = '';
    this.error = '';

    this.walletService.withdraw(this.withdrawForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          this.wallet = response.data;
          this.success = 'Withdrawal initiated successfully!';
        } else {
          this.error = response.message;
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Withdrawal failed';
        this.loading = false;
      }
    });
  }
}
