import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavbarComponent } from '../../layout/navbar.component';
import { GameService } from '../../core/services/game.service';
import { WalletService } from '../../core/services/wallet.service';
import { GameResponse } from '../../models/game.model';
import { Wallet } from '../../models/wallet.model';

@Component({
  selector: 'app-practice-game',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  template: './practice-game.component.html'
})
export class PracticeGameComponent implements OnInit {
  gameForm: FormGroup;
  wallet: Wallet | null = null;
  result: GameResponse | null = null;
  loading = false;
  selectedNumber: number | null = null;
  numbers = [1, 2, 3, 4, 5, 6, 7, 8];

  constructor(
    private fb: FormBuilder,
    private gameService: GameService,
    private walletService: WalletService
  ) {
    this.gameForm = this.fb.group({
      betAmount: [10, [Validators.required, Validators.min(1)]]
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

  selectNumber(num: number) {
    this.selectedNumber = num;
  }

  playGame() {
    if (!this.selectedNumber || this.gameForm.invalid) return;

    this.loading = true;
    this.result = null;

    this.gameService.playGame({
      gameType: 'PRACTICE',
      betAmount: this.gameForm.value.betAmount,
      selectedNumber: this.selectedNumber
    }).subscribe({
      next: (response) => {
        if (response.success) {
          this.result = response.data;
          if (this.wallet) {
            this.wallet.practiceBalance = response.data.newBalance;
          }
        }
        this.loading = false;
      },
      error: (err) => {
        this.result = { message: err.error?.message || 'Game failed' } as GameResponse;
        this.loading = false;
      }
    });
  }
}
