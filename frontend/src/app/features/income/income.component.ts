import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../layout/navbar.component';
import { IncomeService } from '../../core/services/income.service';
import { IncomeOverview } from '../../models/income.model';

@Component({
  selector: 'app-income',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './income.component.html'
})
export class IncomeComponent implements OnInit {
  income: IncomeOverview | null = null;
  loading = true;

  constructor(private incomeService: IncomeService) {}

  ngOnInit() {
    this.loadIncome();
  }

  loadIncome() {
    this.incomeService.getIncomeOverview().subscribe({
      next: (response) => {
        if (response.success) {
          this.income = response.data;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
