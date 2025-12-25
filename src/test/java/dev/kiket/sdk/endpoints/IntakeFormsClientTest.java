package dev.kiket.sdk.endpoints;

import dev.kiket.sdk.client.KiketClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;

class IntakeFormsClientTest {

    @Test
    void constructorThrowsWhenProjectIdIsNull() {
        KiketClient client = Mockito.mock(KiketClient.class);
        assertThrows(IllegalArgumentException.class, () -> new IntakeFormsClient(client, null));
    }

    @Test
    void constructorThrowsWhenProjectIdIsBlank() {
        KiketClient client = Mockito.mock(KiketClient.class);
        assertThrows(IllegalArgumentException.class, () -> new IntakeFormsClient(client, "   "));
    }

    @Test
    void listIncludesProjectId() {
        KiketClient client = Mockito.mock(KiketClient.class);
        Mockito.when(client.get(anyString(), eq(IntakeFormsClient.IntakeFormListResponse.class)))
            .thenReturn(Mono.just(new IntakeFormsClient.IntakeFormListResponse()));

        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");
        intakeForms.list(null);

        Mockito.verify(client).get(
            argThat((ArgumentMatcher<String>) url ->
                url.contains("project_id=42") && url.contains("/ext/intake_forms")
            ),
            eq(IntakeFormsClient.IntakeFormListResponse.class)
        );
    }

    @Test
    void listIncludesOptionalFilters() {
        KiketClient client = Mockito.mock(KiketClient.class);
        Mockito.when(client.get(anyString(), eq(IntakeFormsClient.IntakeFormListResponse.class)))
            .thenReturn(Mono.just(new IntakeFormsClient.IntakeFormListResponse()));

        IntakeFormsClient.IntakeFormListOptions options = new IntakeFormsClient.IntakeFormListOptions();
        options.setActive(true);
        options.setPublicOnly(true);
        options.setLimit(10);

        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");
        intakeForms.list(options);

        Mockito.verify(client).get(
            argThat((ArgumentMatcher<String>) url ->
                url.contains("active=true") &&
                url.contains("public=true") &&
                url.contains("limit=10")
            ),
            eq(IntakeFormsClient.IntakeFormListResponse.class)
        );
    }

    @Test
    void getReturnsForm() {
        KiketClient client = Mockito.mock(KiketClient.class);
        IntakeFormsClient.IntakeForm form = new IntakeFormsClient.IntakeForm();
        form.setId(1L);
        form.setKey("feedback");
        Mockito.when(client.get(anyString(), eq(IntakeFormsClient.IntakeForm.class)))
            .thenReturn(Mono.just(form));

        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");
        IntakeFormsClient.IntakeForm result = intakeForms.get("feedback");

        assertEquals("feedback", result.getKey());
        Mockito.verify(client).get(
            argThat((ArgumentMatcher<String>) url ->
                url.contains("/ext/intake_forms/feedback") && url.contains("project_id=42")
            ),
            eq(IntakeFormsClient.IntakeForm.class)
        );
    }

    @Test
    void getThrowsWhenFormKeyIsNull() {
        KiketClient client = Mockito.mock(KiketClient.class);
        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");
        assertThrows(IllegalArgumentException.class, () -> intakeForms.get(null));
    }

    @Test
    void publicUrlReturnsUrlForPublicForm() {
        KiketClient client = Mockito.mock(KiketClient.class);
        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");

        IntakeFormsClient.IntakeForm form = new IntakeFormsClient.IntakeForm();
        form.setPublic(true);
        form.setFormUrl("https://app.kiket.dev/forms/feedback");

        assertEquals("https://app.kiket.dev/forms/feedback", intakeForms.publicUrl(form));
    }

    @Test
    void publicUrlReturnsNullForPrivateForm() {
        KiketClient client = Mockito.mock(KiketClient.class);
        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");

        IntakeFormsClient.IntakeForm form = new IntakeFormsClient.IntakeForm();
        form.setPublic(false);

        assertNull(intakeForms.publicUrl(form));
    }

    @Test
    void listSubmissionsIncludesFilters() {
        KiketClient client = Mockito.mock(KiketClient.class);
        Mockito.when(client.get(anyString(), eq(IntakeFormsClient.IntakeSubmissionListResponse.class)))
            .thenReturn(Mono.just(new IntakeFormsClient.IntakeSubmissionListResponse()));

        IntakeFormsClient.IntakeSubmissionListOptions options = new IntakeFormsClient.IntakeSubmissionListOptions();
        options.setStatus("pending");
        options.setLimit(25);

        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");
        intakeForms.listSubmissions("feedback", options);

        Mockito.verify(client).get(
            argThat((ArgumentMatcher<String>) url ->
                url.contains("/submissions") &&
                url.contains("status=pending") &&
                url.contains("limit=25")
            ),
            eq(IntakeFormsClient.IntakeSubmissionListResponse.class)
        );
    }

    @Test
    void createSubmissionSendsPayload() {
        KiketClient client = Mockito.mock(KiketClient.class);
        IntakeFormsClient.IntakeSubmission submission = new IntakeFormsClient.IntakeSubmission();
        submission.setId(1L);
        submission.setStatus("pending");
        Mockito.when(client.post(anyString(), Mockito.anyMap(), eq(IntakeFormsClient.IntakeSubmission.class)))
            .thenReturn(Mono.just(submission));

        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");
        Map<String, Object> data = new HashMap<>();
        data.put("email", "test@example.com");

        IntakeFormsClient.IntakeSubmission result = intakeForms.createSubmission("feedback", data, null);

        assertEquals("pending", result.getStatus());
        Mockito.verify(client).post(
            argThat((ArgumentMatcher<String>) url -> url.contains("/ext/intake_forms/feedback/submissions")),
            argThat((ArgumentMatcher<Map<String, Object>>) body ->
                "42".equals(body.get("project_id")) &&
                body.containsKey("data")
            ),
            eq(IntakeFormsClient.IntakeSubmission.class)
        );
    }

    @Test
    void createSubmissionIncludesMetadata() {
        KiketClient client = Mockito.mock(KiketClient.class);
        IntakeFormsClient.IntakeSubmission submission = new IntakeFormsClient.IntakeSubmission();
        Mockito.when(client.post(anyString(), Mockito.anyMap(), eq(IntakeFormsClient.IntakeSubmission.class)))
            .thenReturn(Mono.just(submission));

        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");
        Map<String, Object> data = Map.of("email", "test@example.com");
        Map<String, Object> metadata = Map.of("source", "api");

        intakeForms.createSubmission("feedback", data, metadata);

        Mockito.verify(client).post(
            anyString(),
            argThat((ArgumentMatcher<Map<String, Object>>) body ->
                body.containsKey("metadata") &&
                "api".equals(((Map<?, ?>) body.get("metadata")).get("source"))
            ),
            eq(IntakeFormsClient.IntakeSubmission.class)
        );
    }

    @Test
    void approveSubmissionSendsPayload() {
        KiketClient client = Mockito.mock(KiketClient.class);
        IntakeFormsClient.IntakeSubmission submission = new IntakeFormsClient.IntakeSubmission();
        submission.setStatus("approved");
        Mockito.when(client.post(anyString(), Mockito.anyMap(), eq(IntakeFormsClient.IntakeSubmission.class)))
            .thenReturn(Mono.just(submission));

        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");
        IntakeFormsClient.IntakeSubmission result = intakeForms.approveSubmission("feedback", 1, "Looks good!");

        assertEquals("approved", result.getStatus());
        Mockito.verify(client).post(
            argThat((ArgumentMatcher<String>) url -> url.contains("/approve")),
            argThat((ArgumentMatcher<Map<String, Object>>) body ->
                "42".equals(body.get("project_id")) &&
                "Looks good!".equals(body.get("notes"))
            ),
            eq(IntakeFormsClient.IntakeSubmission.class)
        );
    }

    @Test
    void rejectSubmissionSendsPayload() {
        KiketClient client = Mockito.mock(KiketClient.class);
        IntakeFormsClient.IntakeSubmission submission = new IntakeFormsClient.IntakeSubmission();
        submission.setStatus("rejected");
        Mockito.when(client.post(anyString(), Mockito.anyMap(), eq(IntakeFormsClient.IntakeSubmission.class)))
            .thenReturn(Mono.just(submission));

        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");
        IntakeFormsClient.IntakeSubmission result = intakeForms.rejectSubmission("feedback", 1, "Invalid data");

        assertEquals("rejected", result.getStatus());
        Mockito.verify(client).post(
            argThat((ArgumentMatcher<String>) url -> url.contains("/reject")),
            argThat((ArgumentMatcher<Map<String, Object>>) body ->
                "42".equals(body.get("project_id")) &&
                "Invalid data".equals(body.get("notes"))
            ),
            eq(IntakeFormsClient.IntakeSubmission.class)
        );
    }

    @Test
    void statsReturnsStatistics() {
        KiketClient client = Mockito.mock(KiketClient.class);
        IntakeFormsClient.IntakeFormStats stats = new IntakeFormsClient.IntakeFormStats();
        stats.setTotalSubmissions(100);
        stats.setPending(10);
        stats.setApproved(80);
        Mockito.when(client.get(anyString(), eq(IntakeFormsClient.IntakeFormStats.class)))
            .thenReturn(Mono.just(stats));

        IntakeFormsClient intakeForms = new IntakeFormsClient(client, "42");
        IntakeFormsClient.IntakeFormStats result = intakeForms.stats("feedback", "month");

        assertEquals(100, result.getTotalSubmissions());
        Mockito.verify(client).get(
            argThat((ArgumentMatcher<String>) url ->
                url.contains("/stats") && url.contains("period=month")
            ),
            eq(IntakeFormsClient.IntakeFormStats.class)
        );
    }
}
