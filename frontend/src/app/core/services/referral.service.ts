import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/api-response.model';
import { ReferralInfo, Referral } from '../../models/referral.model';

@Injectable({
  providedIn: 'root'
})
export class ReferralService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getReferralInfo(): Observable<ApiResponse<ReferralInfo>> {
    return this.http.get<ApiResponse<ReferralInfo>>(`${this.apiUrl}/referral`);
  }

  getTeam(): Observable<ApiResponse<Referral[]>> {
    return this.http.get<ApiResponse<Referral[]>>(`${this.apiUrl}/referral/team`);
  }

  getReferralsByLevel(level: number): Observable<ApiResponse<Referral[]>> {
    return this.http.get<ApiResponse<Referral[]>>(`${this.apiUrl}/referral/level/${level}`);
  }
}