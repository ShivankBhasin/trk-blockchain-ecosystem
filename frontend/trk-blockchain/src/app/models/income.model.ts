export interface IncomeOverview {
  totalIncome: number;
  winnersIncome: number;
  directLevelIncome: number;
  winnerLevelIncome: number;
  cashbackIncome: number;
  roiOnRoiIncome: number;
  clubIncome: number;
  luckyDrawIncome: number;
  recentIncomes: IncomeHistory[];
}

export interface IncomeHistory {
  type: string;
  amount: number;
  source: string;
  timestamp: string;
}

export interface Income {
  id: number;
  userId: number;
  type: string;
  amount: number;
  sourceUserId: number;
  level: number;
  timestamp: string;
  description: string;
}
