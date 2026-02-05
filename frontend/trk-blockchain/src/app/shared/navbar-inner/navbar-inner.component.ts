import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar-inner',
  standalone: true,
  imports:[RouterModule],
  templateUrl: './navbar-inner.component.html'
})
export class NavbarInnerComponent {

  openWallet(){
    document.dispatchEvent(new CustomEvent('open-wallet-modal'));
  }

}
