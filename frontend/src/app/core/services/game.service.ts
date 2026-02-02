import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/api-response.model';
import { Game, GameRequest, GameResponse } from '../../models/game.model';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  playGame(request: GameRequest): Observable<ApiResponse<GameResponse>> {
    return this.http.post<ApiResponse<GameResponse>>(`${this.apiUrl}/game/play`, request);
  }

  getGameHistory(): Observable<ApiResponse<Game[]>> {
    return this.http.get<ApiResponse<Game[]>>(`${this.apiUrl}/game/history`);
  }
}
