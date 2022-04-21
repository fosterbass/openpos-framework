export class PersonalizationRequest {
    constructor(
        public deviceToken: string,
        public deviceId: string,
        public appId: string,
        public deviceType: string,
        public pairedAppId?: string,
        public pairedDeviceId?: string
    ) { }

    public personalizationParameters = {};
}
