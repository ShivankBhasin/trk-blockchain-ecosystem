import { Injectable } from '@angular/core';
import { Router, CanActivate } from '@angular/router';
import { Web3Service } from '../services/web3.service';
import { map, take } from 'rxjs/operators';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WalletGuard implements CanActivate {
  constructor(
    private web3Service: Web3Service,
    private router: Router
  ) {}

  canActivate(): Observable<boolean> {
    return this.web3Service.walletState$.pipe(
      take(1),
      map(state => {
        if (state.isConnected) {
          return true;
        }
        this.router.navigate(['/connect']);
        return false;
      })
    );
  }
}
