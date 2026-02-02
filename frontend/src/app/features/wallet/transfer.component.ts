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
  templateUrl: './transfer.component.html'
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
