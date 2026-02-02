export interface LuckyDrawInfo {
  drawId: number;
  totalTickets: number;
  soldTickets: number;
  remainingTickets: number;
  status: string;
  prizePool: number;
  ticketPrice: number;
  drawDate: string;
  myTickets: TicketInfo[];
  winners: WinnerInfo[];
}

export interface TicketInfo {
  ticketId: number;
  ticketNumber: number;
  purchaseDate: string;
  isWinner: boolean;
  prizeAmount: number;
}

export interface WinnerInfo {
  rank: number;
  username: string;
  ticketNumber: number;
  prize: number;
}

export interface LuckyDraw {
  id: number;
  totalTickets: number;
  soldTickets: number;
  status: string;
  drawDate: string;
  prizePool: number;
  ticketPrice: number;
  createdAt: string;
}
