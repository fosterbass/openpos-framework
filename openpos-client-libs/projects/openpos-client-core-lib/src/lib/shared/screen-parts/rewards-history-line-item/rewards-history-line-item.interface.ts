export interface RewardHistory {
    promotionId: string;
    name: string;
    expirationDate: string;
    rewardType: string;
    reward: number;
    redeemed: boolean;
}

export interface RewardsHistoryLineItemComponentInterface {
    redeemedLabel: string;
    expiredLabel: string;
    loyaltyIcon: string;
    expiredIcon: string;
}
