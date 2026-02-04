import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Wallets } from '../core/services/web3.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent {
  @Input() wallet: Wallets | null = null;

  getTotalBalance(): number {
    if (!this.wallet) return 0;
    return (
      parseFloat(this.wallet.practiceBalance) +
      parseFloat(this.wallet.cashBalance) +
      parseFloat(this.wallet.directWallet) +
      parseFloat(this.wallet.luckyDrawWallet)
    );
  }
}
