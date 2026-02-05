import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavbarComponent } from '../../layout/navbar.component';
import { Web3Service, Wallets } from '../../core/services/web3.service';

interface DrawInfo {
  drawId: number;
  ticketsSold: number;
  ticketsRemaining: number;
  startTime: Date;
  isComplete: boolean;
}

@Component({
  selector: 'app-lucky-draw',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  templateUrl: './lucky-draw.component.html'
})
export class LuckyDrawComponent implements OnInit {

  draw: DrawInfo | null = null;
  wallets: Wallets | null = null;

  userTickets = 0;

  ticketForm: FormGroup;

  loading = true;
  buying = false;

  success = '';
  error = '';

  prizeStructure = [
    { rank: '1st', prize: 10000, winners: 1 },
    { rank: '2nd', prize: 5000, winners: 1 },
    { rank: '3rd', prize: 4000, winners: 1 },
    { rank: '4th-10th', prize: 1000, winners: 7 },
    { rank: '11th-50th', prize: 300, winners: 40 },
    { rank: '51st-100th', prize: 120, winners: 50 },
    { rank: '101st-500th', prize: 40, winners: 400 },
    { rank: '501st-1000th', prize: 20, winners: 500 }
  ];

  constructor(
    private fb: FormBuilder,
    private web3Service: Web3Service
  ) {
    this.ticketForm = this.fb.group({
      quantity: [1, [Validators.required, Validators.min(1), Validators.max(100)]]
    });
  }

  async ngOnInit() {

    // Fake draw
    this.draw = {
      drawId: 1,
      ticketsSold: 2350,
      ticketsRemaining: 7650,
      startTime: new Date(),
      isComplete: false
    };

    this.wallets = await this.web3Service.getUserWallets();
    this.loading = false;
  }

 async buyTicket() {

  if (this.ticketForm.invalid) return;

  // 🔒 Block if no balance
  if (!this.wallets || Number(this.wallets.luckyDrawWallet) <= 0) {
    this.error = 'Insufficient Lucky Draw balance';
    return;
  }

  this.buying = true;
  this.success = '';
  this.error = '';

  const quantity = this.ticketForm.value.quantity;

  // Fake blockchain delay
  await new Promise(res => setTimeout(res, 2000));

  // Fake success
  await this.web3Service.buyLuckyDrawTickets(quantity);

  this.userTickets += quantity;
  this.draw!.ticketsSold += quantity;
  this.draw!.ticketsRemaining -= quantity;

  this.success = `Successfully purchased ${quantity} ticket(s)!`;

  this.buying = false;
}

  // async buyTicket() {

  //   if (this.ticketForm.invalid) return;

  //   this.buying = true;
  //   this.success = '';
  //   this.error = '';

  //   const quantity = this.ticketForm.value.quantity;

  //   // Fake purchase
  //   await this.web3Service.buyLuckyDrawTickets(quantity);

  //   this.userTickets += quantity;
  //   this.draw!.ticketsSold += quantity;
  //   this.draw!.ticketsRemaining -= quantity;

  //   this.success = `Successfully purchased ${quantity} ticket(s)!`;

  //   this.wallets = await this.web3Service.getUserWallets();

  //   this.buying = false;
  // }

  getTotalCost(): number {
    return this.ticketForm.value.quantity * 10;
  }

  getProgressPercent(): number {
    if (!this.draw) return 0;
    return (this.draw.ticketsSold / 10000) * 100;
  }
}

// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
// import { NavbarComponent } from '../../layout/navbar.component';
// import { Web3Service, Wallets } from '../../core/services/web3.service';

// interface DrawInfo {
//   drawId: number;
//   ticketsSold: number;
//   ticketsRemaining: number;
//   startTime: Date;
//   isComplete: boolean;
// }

// @Component({
//   selector: 'app-lucky-draw',
//   standalone: true,
//   imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
//   templateUrl: './lucky-draw.component.html'
// })
// export class LuckyDrawComponent implements OnInit {
//   draw: DrawInfo | null = null;
//   wallets: Wallets | null = null;
//   userTickets = 0;
//   ticketForm: FormGroup;
//   loading = true;
//   buying = false;
//   success = '';
//   error = '';
//   txHash = '';

//   // Prize structure
//   prizeStructure = [
//     { rank: '1st', prize: 10000, winners: 1 },
//     { rank: '2nd', prize: 5000, winners: 1 },
//     { rank: '3rd', prize: 4000, winners: 1 },
//     { rank: '4th-10th', prize: 1000, winners: 7 },
//     { rank: '11th-50th', prize: 300, winners: 40 },
//     { rank: '51st-100th', prize: 120, winners: 50 },
//     { rank: '101st-500th', prize: 40, winners: 400 },
//     { rank: '501st-1000th', prize: 20, winners: 500 }
//   ];

//   constructor(
//     private fb: FormBuilder,
//     private web3Service: Web3Service
//   ) {
//     this.ticketForm = this.fb.group({
//       quantity: [1, [Validators.required, Validators.min(1), Validators.max(100)]]
//     });
//   }

//   ngOnInit() {
//     this.loadDraw();
//   }

//   async loadDraw() {
//     this.loading = true;
//     try {
//       this.draw = await this.web3Service.getCurrentDraw();
//       this.wallets = await this.web3Service.getUserWallets();
//       if (this.draw) {
//         this.userTickets = await this.web3Service.getUserTickets(this.draw.drawId);
//       }
//     } catch (error) {
//       console.error('Error loading draw:', error);
//     } finally {
//       this.loading = false;
//     }
//   }

//   async buyTicket() {
//     if (this.ticketForm.invalid) return;

//     this.buying = true;
//     this.success = '';
//     this.error = '';
//     this.txHash = '';

//     try {
//       const quantity = this.ticketForm.value.quantity;
//       const receipt = await this.web3Service.buyLuckyDrawTickets(quantity);

//       this.txHash = receipt.hash;
//       this.success = `Successfully purchased ${quantity} ticket(s)!`;

//       // Refresh data
//       await this.loadDraw();
//     } catch (err: any) {
//       console.error('Purchase error:', err);
//       this.error = err.reason || err.message || 'Purchase failed';
//     } finally {
//       this.buying = false;
//     }
//   }

//   getTotalCost(): number {
//     return this.ticketForm.value.quantity * 10;
//   }

//   getProgressPercent(): number {
//     if (!this.draw) return 0;
//     return (this.draw.ticketsSold / 10000) * 100;
//   }

//   getExplorerUrl(hash: string): string {
//     return this.web3Service.getExplorerUrl(hash);
//   }
// }
