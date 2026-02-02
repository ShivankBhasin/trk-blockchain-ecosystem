import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavbarComponent } from '../../layout/navbar.component';
import { LuckyDrawService } from '../../core/services/lucky-draw.service';
import { LuckyDrawInfo } from '../../models/lucky-draw.model';

@Component({
  selector: 'app-lucky-draw',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  template: './lucky-draw.component.html'
})
export class LuckyDrawComponent implements OnInit {
  draw: LuckyDrawInfo | null = null;
  ticketForm: FormGroup;
  loading = true;
  buying = false;
  success = '';
  error = '';

  constructor(
    private fb: FormBuilder,
    private luckyDrawService: LuckyDrawService
  ) {
    this.ticketForm = this.fb.group({
      quantity: [1, [Validators.required, Validators.min(1), Validators.max(100)]]
    });
  }

  ngOnInit() {
    this.loadDraw();
  }

  loadDraw() {
    this.luckyDrawService.getCurrentDraw().subscribe({
      next: (response) => {
        if (response.success) {
          this.draw = response.data;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  buyTicket() {
    if (this.ticketForm.invalid) return;

    this.buying = true;
    this.success = '';
    this.error = '';

    this.luckyDrawService.buyTicket(this.ticketForm.value.quantity).subscribe({
      next: (response) => {
        if (response.success) {
          this.draw = response.data;
          this.success = response.message;
        } else {
          this.error = response.message;
        }
        this.buying = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Purchase failed';
        this.buying = false;
      }
    });
  }
}
