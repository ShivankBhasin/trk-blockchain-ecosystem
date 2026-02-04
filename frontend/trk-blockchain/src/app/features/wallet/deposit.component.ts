import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavbarComponent } from '../../layout/navbar.component';
import { Web3Service, Wallets, UserInfo } from '../../core/services/web3.service';

@Component({
  selector: 'app-deposit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  templateUrl: './deposit.component.html'
})
export class DepositComponent implements OnInit {
  depositForm: FormGroup;
  wallets: Wallets | null = null;
  userInfo: UserInfo | null = null;
  loading = false;
  depositing = false;
  approving = false;
  success = '';
  error = '';
  txHash = '';
  needsApproval = true;

  constructor(
    private fb: FormBuilder,
    private web3Service: Web3Service
  ) {
    this.depositForm = this.fb.group({
      amount: [100, [Validators.required, Validators.min(10)]]
    });
  }

  ngOnInit() {
    this.loadData();
  }

  async loadData() {
    this.loading = true;
    try {
      this.wallets = await this.web3Service.getUserWallets();
      this.userInfo = await this.web3Service.getUserInfo();
      await this.checkAllowance();
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      this.loading = false;
    }
  }

  async checkAllowance() {
    try {
      const amount = this.depositForm.value.amount;
      this.needsApproval = await this.web3Service.needsApproval(amount.toString());
    } catch (error) {
      console.error('Error checking allowance:', error);
      this.needsApproval = true;
    }
  }

  async approveUSDT() {
    this.approving = true;
    this.error = '';
    this.success = '';

    try {
      const amount = this.depositForm.value.amount;
      await this.web3Service.approveUSDT(amount.toString());
      this.needsApproval = false;
      this.success = 'USDT approved! You can now deposit.';
    } catch (err: any) {
      console.error('Approval error:', err);
      this.error = err.reason || err.message || 'Approval failed';
    } finally {
      this.approving = false;
    }
  }

  async onSubmit() {
    if (this.depositForm.invalid) return;

    this.depositing = true;
    this.success = '';
    this.error = '';
    this.txHash = '';

    try {
      const amount = this.depositForm.value.amount;
      // const receipt = await this.web3Service.deposit(amount.toString());
      // this.txHash = receipt.hash;
      await this.web3Service.deposit();
      this.success = 'Deposit successful';

      // this.success = `Successfully deposited ${amount} USDT!`;

      // Refresh data
      await this.loadData();
    } catch (err: any) {
      console.error('Deposit error:', err);
      this.error = err.reason || err.message || 'Deposit failed';
    } finally {
      this.depositing = false;
    }
  }

  onAmountChange() {
    this.checkAllowance();
  }

  getExplorerUrl(hash: string): string {
    return this.web3Service.getExplorerUrl(hash);
  }
}
