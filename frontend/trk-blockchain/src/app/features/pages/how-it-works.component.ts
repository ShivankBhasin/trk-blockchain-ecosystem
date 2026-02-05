import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarInnerComponent } from '../../shared/navbar-inner/navbar-inner.component';

@Component({
  selector: 'app-how-it-works',
  standalone: true,
  imports: [CommonModule, NavbarInnerComponent],
  templateUrl: './how-it-works.component.html'
})
export class HowItWorksComponent {}
