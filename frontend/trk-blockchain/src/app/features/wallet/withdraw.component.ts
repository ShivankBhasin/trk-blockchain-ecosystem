import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavbarComponent } from '../../layout/navbar.component';
import { Web3Service, Wallets, WalletState } from '../../core/services/web3.service';

@Component({
  selector: 'app-withdraw',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  templateUrl: './withdraw.component.html'
})
export class WithdrawComponent implements OnInit {
  withdrawForm: FormGroup;
  wallets: Wallets | null = null;
  walletState: WalletState | null = null;
  loading = false;
  withdrawing = false;
  success = '';
  error = '';
  txHash = '';

  readonly WITHDRAWAL_FEE = 2; // 2 USDT flat fee
  readonly MIN_WITHDRAWAL = 10;
  readonly MAX_WITHDRAWAL = 5000;

  constructor(
    private fb: FormBuilder,
    private web3Service: Web3Service
  ) {
    this.withdrawForm = this.fb.group({
      amount: [100, [Validators.required, Validators.min(this.MIN_WITHDRAWAL), Validators.max(this.MAX_WITHDRAWAL)]]
    });
  }

  ngOnInit() {
    this.web3Service.walletState$.subscribe(state => {
      this.walletState = state;
    });
    this.loadData();
  }

  async loadData() {
    this.loading = true;
    try {
      this.wallets = await this.web3Service.getUserWallets();
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      this.loading = false;
    }
  }

  async onSubmit() {
    if (this.withdrawForm.invalid) return;

    this.withdrawing = true;
    this.success = '';
    this.error = '';
    this.txHash = '';

    try {
      const amount = this.withdrawForm.value.amount;
      // const receipt = await this.web3Service.withdraw(amount.toString());
      // this.txHash = receipt.hash;
      await this.web3Service.withdraw();
      this.success = 'Withdrawal successful';
      // this.success = `Successfully withdrew ${amount} USDT to your wallet!`;

      // Refresh data
      await this.loadData();
    } catch (err: any) {
      console.error('Withdrawal error:', err);
      this.error = err.reason || err.message || 'Withdrawal failed';
    } finally {
      this.withdrawing = false;
    }
  }

  getNetAmount(): number {
    const amount = this.withdrawForm.value.amount || 0;
    return Math.max(0, amount - this.WITHDRAWAL_FEE);
  }

  getTotalDeducted(): number {
    return this.withdrawForm.value.amount || 0;
  }

  canWithdraw(): boolean {
    if (!this.wallets) return false;
    const amount = this.withdrawForm.value.amount || 0;
    return parseFloat(this.wallets.directWallet) >= amount;
  }

  getExplorerUrl(hash: string): string {
    return this.web3Service.getExplorerUrl(hash);
  }
}
