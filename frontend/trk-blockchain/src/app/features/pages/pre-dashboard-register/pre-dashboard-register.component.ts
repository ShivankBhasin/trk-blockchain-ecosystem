import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Web3Service } from '../../../core/services/web3.service';

@Component({
  selector: 'app-pre-dashboard-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pre-dashboard-register.component.html'
})
export class PreDashboardRegisterComponent {

  referralCode = '';
  loading = false;
  error = '';
  detectedReferrer = '';

  constructor(
    private web3Service: Web3Service,
    private router: Router
  ) {}

  // 🔹 Detect valid wallet address while typing
  onReferralChange() {
    if (this.referralCode.startsWith('0x') && this.referralCode.length === 42) {
      const short =
        this.referralCode.substring(0, 6) +
        '...' +
        this.referralCode.substring(38);

      this.detectedReferrer = short;
    } else {
      this.detectedReferrer = '';
    }
  }

  async register() {
    this.error = '';

    if (this.referralCode === 'TRK12345') {
  console.log('DEV MODE REGISTER SUCCESS');

  localStorage.setItem('is_registered', 'true');

  this.router.navigate(['/dashboard']);
  return;
}

    // ✅ DEV BACKDOOR
    if (this.referralCode === 'TRK12345') {
      console.log('DEV MODE REGISTER SUCCESS');
      this.router.navigate(['/dashboard']);
      return;
    }

    // ❗ Require valid wallet address for real register
    if (!this.referralCode.startsWith('0x') || this.referralCode.length !== 42) {
      this.error = 'Invalid referral address';
      return;
    }

    // 🔥 REAL BLOCKCHAIN REGISTER
    try {
      this.loading = true;

      await this.web3Service.register(this.referralCode);

      this.router.navigate(['/dashboard']);

    } catch (err: any) {
      console.error(err);
      this.error = err?.message || 'Registration failed';
    } finally {
      this.loading = false;
    }
  }

}
