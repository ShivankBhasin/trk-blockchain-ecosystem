import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../../layout/navbar.component';
import { SidebarComponent } from '../../layout/sidebar.component';
import { UserService } from '../../core/services/user.service';
import { Dashboard } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent, SidebarComponent],
  template: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  dashboard: Dashboard | null = null;
  loading = true;

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.loadDashboard();
  }

  loadDashboard() {
    this.userService.getDashboard().subscribe({
      next: (response) => {
        if (response.success) {
          this.dashboard = response.data;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  getProgressPercent(): number {
    if (!this.dashboard?.cashback || this.dashboard.cashback.maxCapping === 0) return 0;
    return (this.dashboard.cashback.totalReceived / this.dashboard.cashback.maxCapping) * 100;
  }
}
