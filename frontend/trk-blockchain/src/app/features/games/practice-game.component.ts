import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavbarComponent } from '../../layout/navbar.component';
import { Web3Service, Wallets, GameResult } from '../../core/services/web3.service';

@Component({
  selector: 'app-practice-game',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  templateUrl: './practice-game.component.html'
})
export class PracticeGameComponent implements OnInit {

  gameForm: FormGroup;
  wallets: Wallets | null = null;
  result: GameResult | null = null;

  loading = false;
  playing = false;

  selectedNumber: number | null = null;
  numbers = [1,2,3,4,5,6,7,8];

  error = '';

  constructor(
    private fb: FormBuilder,
    private web3Service: Web3Service
  ) {
    this.gameForm = this.fb.group({
      betAmount: [10, [Validators.required, Validators.min(1)]]
    });
  }

  async ngOnInit() {
    this.wallets = await this.web3Service.getUserWallets();
  }

  selectNumber(num: number) {
    this.selectedNumber = num;
    this.result = null;
    this.error = '';
  }

  async playGame() {

    if (!this.selectedNumber || this.gameForm.invalid) return;

    this.playing = true;
    this.result = null;
    this.error = '';

    const betAmount = this.gameForm.value.betAmount.toString();

    // Fake play
    await this.web3Service.playGame();

    // Random win / loss
    const winningNumber = Math.floor(Math.random() * 8) + 1;
    const isWin = winningNumber === this.selectedNumber;

    this.result = {
      timestamp: new Date(),
      selectedNumber: this.selectedNumber,
      winningNumber: winningNumber,
      betAmount: betAmount,
      isWin: isWin,
      isPractice: true
    };

    // Refresh fake wallet
    this.wallets = await this.web3Service.getUserWallets();

    this.playing = false;
  }

  parseFloat(value: string): number {
    return parseFloat(value) || 0;
  }
}

// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
// import { NavbarComponent } from '../../layout/navbar.component';
// import { Web3Service, Wallets, GameResult } from '../../core/services/web3.service';

// @Component({
//   selector: 'app-practice-game',
//   standalone: true,
//   imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
//   templateUrl: './practice-game.component.html'
// })
// export class PracticeGameComponent implements OnInit {
//   gameForm: FormGroup;
//   wallets: Wallets | null = null;
//   result: GameResult | null = null;
//   loading = false;
//   playing = false;
//   selectedNumber: number | null = null;
//   numbers = [1, 2, 3, 4, 5, 6, 7, 8];
//   error = '';
//   txHash = '';

//   constructor(
//     private fb: FormBuilder,
//     private web3Service: Web3Service
//   ) {
//     this.gameForm = this.fb.group({
//       betAmount: [10, [Validators.required, Validators.min(1)]]
//     });
//   }

//   ngOnInit() {
//     this.loadWallet();
//   }

//   async loadWallet() {
//     this.loading = true;
//     try {
//       this.wallets = await this.web3Service.getUserWallets();
//     } catch (error) {
//       console.error('Error loading wallet:', error);
//     } finally {
//       this.loading = false;
//     }
//   }

//   selectNumber(num: number) {
//     this.selectedNumber = num;
//     this.result = null;
//     this.error = '';
//   }

//   async playGame() {
//     if (!this.selectedNumber || this.gameForm.invalid) return;

//     this.playing = true;
//     this.result = null;
//     this.error = '';
//     this.txHash = '';

//     try {
//       const betAmount = this.gameForm.value.betAmount.toString();
//       const receipt = await this.web3Service.playGame(betAmount, this.selectedNumber, true);

//       this.txHash = receipt.hash;

//       // Refresh wallet and get latest game result
//       await this.loadWallet();
//       const history = await this.web3Service.getGameHistory(1);
//       if (history.length > 0) {
//         this.result = history[0];
//       }
//     } catch (err: any) {
//       console.error('Game error:', err);
//       this.error = err.reason || err.message || 'Transaction failed';
//     } finally {
//       this.playing = false;
//     }
//   }

//   getExplorerUrl(hash: string): string {
//     return this.web3Service.getExplorerUrl(hash);
//   }

//   // Helper for template to access parseFloat
//   parseFloat(value: string): number {
//     return parseFloat(value) || 0;
//   }
// }
