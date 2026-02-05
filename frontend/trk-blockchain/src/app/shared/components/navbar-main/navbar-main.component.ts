import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar-main',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar-main.component.html'
})
export class NavbarMainComponent {

  openWallet(){
    document.dispatchEvent(new CustomEvent('open-wallet-modal'));
  }

}
