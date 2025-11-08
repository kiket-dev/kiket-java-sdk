package dev.kiket.sdk.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kiket.sdk.client.KiketClient;
import lombok.Data;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomDataClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KiketClient client;
    private final String projectId;

    public CustomDataClient(KiketClient client, String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("projectId is required for custom data operations");
        }
        this.client = client;
        this.projectId = projectId;
    }

    public CustomDataListResponse list(String moduleKey, String table, CustomDataListOptions options) {
        String url = buildUrl(moduleKey, table, null,
            options != null ? options.getLimit() : null,
            options != null ? options.getFilters() : null);
        return client.get(url, CustomDataListResponse.class).block();
    }

    public CustomDataRecordResponse get(String moduleKey, String table, String recordId) {
        String url = buildUrl(moduleKey, table, recordId, null, null);
        return client.get(url, CustomDataRecordResponse.class).block();
    }

    public CustomDataRecordResponse create(String moduleKey, String table, Map<String, Object> record) {
        String url = buildUrl(moduleKey, table, null, null, null);
        return client.post(url, Map.of("record", record), CustomDataRecordResponse.class).block();
    }

    public CustomDataRecordResponse update(String moduleKey, String table, String recordId, Map<String, Object> record) {
        String url = buildUrl(moduleKey, table, recordId, null, null);
        return client.patch(url, Map.of("record", record), CustomDataRecordResponse.class).block();
    }

    public void delete(String moduleKey, String table, String recordId) {
        String url = buildUrl(moduleKey, table, recordId, null, null);
        client.delete(url, Map.class).block();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String buildUrl(
        String moduleKey,
        String table,
        String recordId,
        Integer limit,
        Map<String, Object> filters
    ) {
        StringBuilder path = new StringBuilder("/ext/custom_data/")
            .append(encode(moduleKey))
            .append("/")
            .append(encode(table));

        if (recordId != null) {
            path.append("/").append(recordId);
        }

        List<String> query = new ArrayList<>();
        query.add("project_id=" + encode(projectId));
        if (limit != null) {
            query.add("limit=" + limit);
        }
        if (filters != null && !filters.isEmpty()) {
            try {
                query.add("filters=" + encode(MAPPER.writeValueAsString(filters)));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to serialize filters", e);
            }
        }

        return path.append("?").append(String.join("&", query)).toString();
    }

    @Data
    public static class CustomDataListOptions {
        private Integer limit;
        private Map<String, Object> filters;
    }

    @Data
    public static class CustomDataListResponse {
        private List<Map<String, Object>> data;
    }

    @Data
    public static class CustomDataRecordResponse {
        private Map<String, Object> data;
    }
}
