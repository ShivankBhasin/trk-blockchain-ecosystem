import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarInnerComponent } from '../../shared/navbar-inner/navbar-inner.component';

@Component({
  selector: 'app-terms',
  standalone: true,
  imports: [CommonModule, NavbarInnerComponent],
  templateUrl: './terms.component.html'
})
export class TermsComponent {}
