export interface ReferralInfo {
  referralCode: string;
  referralLink: string;
  totalReferrals: number;
  directReferrals: number;
  totalEarnings: number;
  directMembers: ReferralMember[];
}

export interface ReferralMember {
  id: number;
  username: string;
  level: number;
  deposits: number;
  joinedAt: string;
  activated: boolean;
}

export interface Referral {
  id: number;
  userId: number;
  referralId: number;
  level: number;
  createdAt: string;
}
