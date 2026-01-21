package dev.kiket.sdk.endpoints;

import dev.kiket.sdk.client.KiketClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlaEventsClient {
    private final KiketClient client;
    private final String projectId;

    public SlaEventsClient(KiketClient client, String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("projectId is required for SLA queries");
        }

        this.client = client;
        this.projectId = projectId;
    }

    public SlaEventsResponse list(SlaEventsListOptions options) {
        String url = buildUrl(options);
        return client.get(url, SlaEventsResponse.class).block();
    }

    private String buildUrl(SlaEventsListOptions options) {
        List<String> query = new ArrayList<>();
        query.add("project_id=" + encode(projectId));

        if (options != null) {
            if (options.getIssueId() != null && !options.getIssueId().isBlank()) {
                query.add("issue_id=" + encode(options.getIssueId()));
            }
            if (options.getState() != null && !options.getState().isBlank()) {
                query.add("state=" + encode(options.getState()));
            }
            if (options.getLimit() != null) {
                query.add("limit=" + options.getLimit());
            }
        }

        return "/ext/sla/events?" + String.join("&", query);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static class SlaEventsListOptions {
        private String issueId;
        private String state;
        private Integer limit;

        public String getIssueId() { return issueId; }
        public void setIssueId(String issueId) { this.issueId = issueId; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }
    }

    public static class SlaEventsResponse {
        private List<Map<String, Object>> data;

        public List<Map<String, Object>> getData() { return data; }
        public void setData(List<Map<String, Object>> data) { this.data = data; }
    }
}
