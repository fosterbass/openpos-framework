export interface AutoPersonalizationRequest {
    deviceName?: string;
    /**
     * An object with additional attributes that may be relevant for the auto personalization endpoint
     * to have for processing the request.
     */
    additionalAttributes?: any;
}