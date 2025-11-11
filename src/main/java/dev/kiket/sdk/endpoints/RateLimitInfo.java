package dev.kiket.sdk.endpoints;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the current extension rate limit window.
 */
public class RateLimitInfo {
    private int limit;
    private int remaining;

    @JsonProperty("window_seconds")
    private int windowSeconds;

    @JsonProperty("reset_in")
    private int resetIn;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public int getResetIn() {
        return resetIn;
    }

    public void setResetIn(int resetIn) {
        this.resetIn = resetIn;
    }
}
