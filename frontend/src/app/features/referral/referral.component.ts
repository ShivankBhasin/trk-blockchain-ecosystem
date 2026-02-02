import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../layout/navbar.component';
import { ReferralService } from '../../core/services/referral.service';
import { ReferralInfo } from '../../models/referral.model';

@Component({
  selector: 'app-referral',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  template: './referral.component.html'
})
export class ReferralComponent implements OnInit {
  referralInfo: ReferralInfo | null = null;
  loading = true;
  copied = false;

  constructor(private referralService: ReferralService) {}

  ngOnInit() {
    this.loadReferralInfo();
  }

  loadReferralInfo() {
    this.referralService.getReferralInfo().subscribe({
      next: (response) => {
        if (response.success) {
          this.referralInfo = response.data;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  copyLink(input: HTMLInputElement) {
    navigator.clipboard.writeText(input.value);
    this.copied = true;
    setTimeout(() => this.copied = false, 2000);
  }
}
