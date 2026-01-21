package dev.kiket.sdk.endpoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.kiket.sdk.client.KiketClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    public IntakeFormListResponse list(IntakeFormListOptions options) {
        String url = buildUrl(null, null,
            options != null ? options.getActive() : null,
            options != null ? options.getPublicOnly() : null,
            options != null ? options.getLimit() : null,
            null, null);
        return client.get(url, IntakeFormListResponse.class).block();
    }

    public IntakeForm get(String formKey) {
        if (formKey == null || formKey.isBlank()) {
            throw new IllegalArgumentException("formKey is required");
        }
        String url = buildUrl(formKey, null, null, null, null, null, null);
        return client.get(url, IntakeForm.class).block();
    }

    public String publicUrl(IntakeForm form) {
        if (form != null && form.isPublic()) {
            return form.getFormUrl();
        }
        return null;
    }

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

    public static class IntakeFormListOptions {
        private Boolean active;
        private Boolean publicOnly;
        private Integer limit;

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public Boolean getPublicOnly() { return publicOnly; }
        public void setPublicOnly(Boolean publicOnly) { this.publicOnly = publicOnly; }
        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }
    }

    public static class IntakeSubmissionListOptions {
        private String status;
        private Integer limit;
        private String since;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }
        public String getSince() { return since; }
        public void setSince(String since) { this.since = since; }
    }

    public static class IntakeFormListResponse {
        private List<IntakeForm> data;

        public List<IntakeForm> getData() { return data; }
        public void setData(List<IntakeForm> data) { this.data = data; }
    }

    public static class IntakeSubmissionListResponse {
        private List<IntakeSubmission> data;

        public List<IntakeSubmission> getData() { return data; }
        public void setData(List<IntakeSubmission> data) { this.data = data; }
    }

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

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
        public List<IntakeFormField> getFields() { return fields; }
        public void setFields(List<IntakeFormField> fields) { this.fields = fields; }
        public String getFormUrl() { return formUrl; }
        public void setFormUrl(String formUrl) { this.formUrl = formUrl; }
        public boolean isEmbedAllowed() { return embedAllowed; }
        public void setEmbedAllowed(boolean embedAllowed) { this.embedAllowed = embedAllowed; }
        public int getSubmissionsCount() { return submissionsCount; }
        public void setSubmissionsCount(int submissionsCount) { this.submissionsCount = submissionsCount; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

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

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getFieldType() { return fieldType; }
        public void setFieldType(String fieldType) { this.fieldType = fieldType; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        public String getPlaceholder() { return placeholder; }
        public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
        public String getHelpText() { return helpText; }
        public void setHelpText(String helpText) { this.helpText = helpText; }
    }

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

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getIntakeFormId() { return intakeFormId; }
        public void setIntakeFormId(Long intakeFormId) { this.intakeFormId = intakeFormId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        public String getSubmittedByEmail() { return submittedByEmail; }
        public void setSubmittedByEmail(String submittedByEmail) { this.submittedByEmail = submittedByEmail; }
        public String getReviewedBy() { return reviewedBy; }
        public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
        public String getReviewedAt() { return reviewedAt; }
        public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class IntakeFormStats {
        @JsonProperty("total_submissions")
        private int totalSubmissions;
        private int pending;
        private int approved;
        private int rejected;
        private int converted;
        private String period;

        public int getTotalSubmissions() { return totalSubmissions; }
        public void setTotalSubmissions(int totalSubmissions) { this.totalSubmissions = totalSubmissions; }
        public int getPending() { return pending; }
        public void setPending(int pending) { this.pending = pending; }
        public int getApproved() { return approved; }
        public void setApproved(int approved) { this.approved = approved; }
        public int getRejected() { return rejected; }
        public void setRejected(int rejected) { this.rejected = rejected; }
        public int getConverted() { return converted; }
        public void setConverted(int converted) { this.converted = converted; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
    }
}
