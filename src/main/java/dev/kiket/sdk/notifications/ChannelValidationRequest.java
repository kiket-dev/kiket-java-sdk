package dev.kiket.sdk.notifications;

/**
 * Request to validate a notification channel.
 */
public class ChannelValidationRequest {
    private final String channelId;
    private final String channelType;

    public ChannelValidationRequest(String channelId) {
        this(channelId, "channel");
    }

    public ChannelValidationRequest(String channelId, String channelType) {
        this.channelId = channelId;
        this.channelType = channelType;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getChannelType() {
        return channelType;
    }
}
