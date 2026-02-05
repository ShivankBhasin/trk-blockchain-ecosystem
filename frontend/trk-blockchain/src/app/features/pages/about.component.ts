import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarInnerComponent } from '../../shared/navbar-inner/navbar-inner.component';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule, NavbarInnerComponent],
  templateUrl: './about.component.html'
})
export class AboutComponent {}
