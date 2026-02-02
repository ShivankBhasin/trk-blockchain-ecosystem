import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/api-response.model';
import { LuckyDrawInfo, LuckyDraw } from '../../models/lucky-draw.model';

@Injectable({
  providedIn: 'root'
})
export class LuckyDrawService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getCurrentDraw(): Observable<ApiResponse<LuckyDrawInfo>> {
    return this.http.get<ApiResponse<LuckyDrawInfo>>(`${this.apiUrl}/lucky-draw`);
  }

  buyTicket(quantity: number = 1): Observable<ApiResponse<LuckyDrawInfo>> {
    return this.http.post<ApiResponse<LuckyDrawInfo>>(`${this.apiUrl}/lucky-draw/buy?quantity=${quantity}`, {});
  }

  getDrawHistory(): Observable<ApiResponse<LuckyDraw[]>> {
    return this.http.get<ApiResponse<LuckyDraw[]>>(`${this.apiUrl}/lucky-draw/history`);
  }
}
