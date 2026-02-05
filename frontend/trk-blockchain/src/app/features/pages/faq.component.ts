import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarInnerComponent } from '../../shared/navbar-inner/navbar-inner.component';

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [CommonModule, NavbarInnerComponent],
  templateUrl: './faq.component.html'
})
export class FaqComponent {

  open = -1;

  toggle(i: number) {
    this.open = this.open === i ? -1 : i;
  }

}
