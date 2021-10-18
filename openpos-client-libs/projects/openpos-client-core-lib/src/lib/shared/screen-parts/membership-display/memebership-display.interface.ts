export interface Membership {
    id: string;
    name: string;
    nonMemberName?: string;
    member: boolean;
}

export interface MembershipDisplayComponentInterface {
    nonMemberIcon: string;
    memberIcon: string;
}
