import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../../layout/navbar.component';

@Component({
  selector: 'app-games',
  standalone: true,
  imports: [CommonModule, NavbarComponent, RouterOutlet],
  templateUrl: './games.component.html'
})
export class GamesComponent {
  constructor(private router: Router) {}

  goPractice() {
  this.router.navigate(['/practice-game']);
}

goCash() {
  this.router.navigate(['/cash-game']);
}
}
