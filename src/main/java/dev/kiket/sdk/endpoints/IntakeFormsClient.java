package dev.kiket.sdk.endpoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.kiket.sdk.client.KiketClient;
import lombok.Data;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client for managing intake forms and submissions via the Kiket API.
 */
public class IntakeFormsClient {
    private final KiketClient client;
    private final String projectId;

    public IntakeFormsClient(KiketClient client, String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("projectId is required for intake form operations");
        }
        this.client = client;
        this.projectId = projectId;
    }

    /**
     * List all intake forms for the project.
     */
    public IntakeFormListResponse list(IntakeFormListOptions options) {
        String url = buildUrl(null, null,
            options != null ? options.getActive() : null,
            options != null ? options.getPublicOnly() : null,
            options != null ? options.getLimit() : null,
            null, null);
        return client.get(url, IntakeFormListResponse.class).block();
    }

    /**
     * Get a specific intake form by key or ID.
     */
    public IntakeForm get(String formKey) {
        if (formKey == null || formKey.isBlank()) {
            throw new IllegalArgumentException("formKey is required");
        }
        String url = buildUrl(formKey, null, null, null, null, null, null);
        return client.get(url, IntakeForm.class).block();
    }

    /**
     * Get the public URL for a form if it's public.
     */
    public String publicUrl(IntakeForm form) {
        if (form != null && form.isPublic()) {
            return form.getFormUrl();
        }
        return null;
    }

    /**
     * List submissions for an intake form.
     */
    public IntakeSubmissionListResponse listSubmissions(String formKey, IntakeSubmissionListOptions options) {
        if (formKey == null || formKey.isBlank()) {
            throw new IllegalArgumentException("formKey is required");
        }
        String url = buildSubmissionsUrl(formKey, null,
            options != null ? options.getStatus() : null,
            options != null ? options.getLimit() : null,
            options != null ? options.getSince() : null);
        return client.get(url, IntakeSubmissionListResponse.class).block();
    }

    /**
     * Get a specific submission by ID.
     */
    public IntakeSubmission getSubmission(String formKey, Object submissionId) {
        if (formKey == null || formKey.isBlank()) {
            throw new IllegalArgumentException("formKey is required");
        }
        if (submissionId == null) {
            throw new IllegalArgumentException("submissionId is required");
        }
        String url = buildSubmissionsUrl(formKey, submissionId.toString(), null, null, null);
        return client.get(url, IntakeSubmission.class).block();
    }

    /**
     * Create a new submission for an intake form.
     */
    public IntakeSubmission createSubmission(String formKey, Map<String, Object> data, Map<String, Object> metadata) {
        if (formKey == null || formKey.isBlank()) {
            throw new IllegalArgumentException("formKey is required");
        }
        if (data == null) {
            throw new IllegalArgumentException("data is required");
        }

        var payload = new java.util.HashMap<String, Object>();
        payload.put("project_id", projectId);
        payload.put("data", data);
        if (metadata != null) {
            payload.put("metadata", metadata);
        }

        String url = "/ext/intake_forms/" + encode(formKey) + "/submissions";
        return client.post(url, payload, IntakeSubmission.class).block();
    }

    /**
     * Approve a pending submission.
     */
    public IntakeSubmission approveSubmission(String formKey, Object submissionId, String notes) {
        if (formKey == null || formKey.isBlank()) {
            throw new IllegalArgumentException("formKey is required");
        }
        if (submissionId == null) {
            throw new IllegalArgumentException("submissionId is required");
        }

        var payload = new java.util.HashMap<String, Object>();
        payload.put("project_id", projectId);
        if (notes != null) {
            payload.put("notes", notes);
        }

        String url = "/ext/intake_forms/" + encode(formKey) + "/submissions/" + submissionId + "/approve";
        return client.post(url, payload, IntakeSubmission.class).block();
    }

    /**
     * Reject a pending submission.
     */
    public IntakeSubmission rejectSubmission(String formKey, Object submissionId, String notes) {
        if (formKey == null || formKey.isBlank()) {
            throw new IllegalArgumentException("formKey is required");
        }
        if (submissionId == null) {
            throw new IllegalArgumentException("submissionId is required");
        }

        var payload = new java.util.HashMap<String, Object>();
        payload.put("project_id", projectId);
        if (notes != null) {
            payload.put("notes", notes);
        }

        String url = "/ext/intake_forms/" + encode(formKey) + "/submissions/" + submissionId + "/reject";
        return client.post(url, payload, IntakeSubmission.class).block();
    }

    /**
     * Get submission statistics for an intake form.
     */
    public IntakeFormStats stats(String formKey, String period) {
        if (formKey == null || formKey.isBlank()) {
            throw new IllegalArgumentException("formKey is required");
        }

        List<String> query = new ArrayList<>();
        query.add("project_id=" + encode(projectId));
        if (period != null) {
            query.add("period=" + encode(period));
        }

        String url = "/ext/intake_forms/" + encode(formKey) + "/stats?" + String.join("&", query);
        return client.get(url, IntakeFormStats.class).block();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String buildUrl(
        String formKey,
        String submissionId,
        Boolean active,
        Boolean publicOnly,
        Integer limit,
        String status,
        String since
    ) {
        StringBuilder path = new StringBuilder("/ext/intake_forms");

        if (formKey != null) {
            path.append("/").append(encode(formKey));
        }

        if (submissionId != null) {
            path.append("/submissions/").append(submissionId);
        }

        List<String> query = new ArrayList<>();
        query.add("project_id=" + encode(projectId));

        if (active != null) {
            query.add("active=" + active);
        }
        if (publicOnly != null) {
            query.add("public=" + publicOnly);
        }
        if (limit != null) {
            query.add("limit=" + limit);
        }
        if (status != null) {
            query.add("status=" + encode(status));
        }
        if (since != null) {
            query.add("since=" + encode(since));
        }

        return path.append("?").append(String.join("&", query)).toString();
    }

    private String buildSubmissionsUrl(
        String formKey,
        String submissionId,
        String status,
        Integer limit,
        String since
    ) {
        StringBuilder path = new StringBuilder("/ext/intake_forms/")
            .append(encode(formKey))
            .append("/submissions");

        if (submissionId != null) {
            path.append("/").append(submissionId);
        }

        List<String> query = new ArrayList<>();
        query.add("project_id=" + encode(projectId));

        if (status != null) {
            query.add("status=" + encode(status));
        }
        if (limit != null) {
            query.add("limit=" + limit);
        }
        if (since != null) {
            query.add("since=" + encode(since));
        }

        return path.append("?").append(String.join("&", query)).toString();
    }

    // Data classes

    @Data
    public static class IntakeFormListOptions {
        private Boolean active;
        private Boolean publicOnly;
        private Integer limit;
    }

    @Data
    public static class IntakeSubmissionListOptions {
        private String status;
        private Integer limit;
        private String since;
    }

    @Data
    public static class IntakeFormListResponse {
        private List<IntakeForm> data;
    }

    @Data
    public static class IntakeSubmissionListResponse {
        private List<IntakeSubmission> data;
    }

    @Data
    public static class IntakeForm {
        private Long id;
        private String key;
        private String name;
        private String description;
        private boolean active;
        @JsonProperty("public")
        private boolean isPublic;
        private List<IntakeFormField> fields;
        @JsonProperty("form_url")
        private String formUrl;
        @JsonProperty("embed_allowed")
        private boolean embedAllowed;
        @JsonProperty("submissions_count")
        private int submissionsCount;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("updated_at")
        private String updatedAt;
    }

    @Data
    public static class IntakeFormField {
        private String key;
        private String label;
        @JsonProperty("field_type")
        private String fieldType;
        private boolean required;
        private List<String> options;
        private String placeholder;
        @JsonProperty("help_text")
        private String helpText;
    }

    @Data
    public static class IntakeSubmission {
        private Long id;
        @JsonProperty("intake_form_id")
        private Long intakeFormId;
        private String status;
        private Map<String, Object> data;
        private Map<String, Object> metadata;
        @JsonProperty("submitted_by_email")
        private String submittedByEmail;
        @JsonProperty("reviewed_by")
        private String reviewedBy;
        @JsonProperty("reviewed_at")
        private String reviewedAt;
        private String notes;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("updated_at")
        private String updatedAt;
    }

    @Data
    public static class IntakeFormStats {
        @JsonProperty("total_submissions")
        private int totalSubmissions;
        private int pending;
        private int approved;
        private int rejected;
        private int converted;
        private String period;
    }
}
