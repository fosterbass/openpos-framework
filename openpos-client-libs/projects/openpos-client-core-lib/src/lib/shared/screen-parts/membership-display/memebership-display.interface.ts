export interface Membership {
    id: string;
    name: string;
    nonMemberName?: string;
    member: boolean;
}

export interface MembershipDisplayComponentInterface {
    checkMarkIcon: string;
    uncheckMarkIcon: string;
}
