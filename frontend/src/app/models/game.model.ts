export interface GameRequest {
  gameType: string;
  betAmount: number;
  selectedNumber: number;
}

export interface GameResponse {
  gameId: number;
  gameType: string;
  betAmount: number;
  result: string;
  selectedNumber: number;
  winningNumber: number;
  payout: number;
  directPayout: number;
  compoundPayout: number;
  newBalance: number;
  message: string;
}

export interface Game {
  id: number;
  userId: number;
  gameType: string;
  betAmount: number;
  result: string;
  multiplier: number;
  payout: number;
  directPayout: number;
  compoundPayout: number;
  timestamp: string;
  selectedNumber: number;
  winningNumber: number;
}
