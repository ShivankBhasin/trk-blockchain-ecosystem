import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/api-response.model';
import { IncomeOverview, Income } from '../../models/income.model';

@Injectable({
  providedIn: 'root'
})
export class IncomeService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getIncomeOverview(): Observable<ApiResponse<IncomeOverview>> {
    return this.http.get<ApiResponse<IncomeOverview>>(`${this.apiUrl}/income`);
  }

  getIncomeHistory(): Observable<ApiResponse<Income[]>> {
    return this.http.get<ApiResponse<Income[]>>(`${this.apiUrl}/income/history`);
  }
}
