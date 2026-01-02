package dev.kiket.sdk.audit;

import java.time.Instant;

/**
 * Options for listing blockchain anchors.
 */
public class ListAnchorsOptions {
    private String status;
    private String network;
    private Instant from;
    private Instant to;
    private int page = 1;
    private int perPage = 25;

    public ListAnchorsOptions() {}

    public ListAnchorsOptions status(String status) {
        this.status = status;
        return this;
    }

    public ListAnchorsOptions network(String network) {
        this.network = network;
        return this;
    }

    public ListAnchorsOptions from(Instant from) {
        this.from = from;
        return this;
    }

    public ListAnchorsOptions to(Instant to) {
        this.to = to;
        return this;
    }

    public ListAnchorsOptions page(int page) {
        this.page = page;
        return this;
    }

    public ListAnchorsOptions perPage(int perPage) {
        this.perPage = perPage;
        return this;
    }

    // Getters
    public String getStatus() { return status; }
    public String getNetwork() { return network; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public int getPage() { return page; }
    public int getPerPage() { return perPage; }
}
