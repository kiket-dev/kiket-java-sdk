package dev.kiket.sdk.endpoints;

import com.fasterxml.jackson.annotation.JsonProperty;

class RateLimitResponse {

    @JsonProperty("rate_limit")
    private RateLimitInfo rateLimit;

    public RateLimitInfo getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimitInfo rateLimit) {
        this.rateLimit = rateLimit;
    }
}
