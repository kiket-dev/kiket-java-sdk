package dev.kiket.sdk.endpoints;

import dev.kiket.sdk.client.KiketClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

class CustomDataClientTest {

    @Test
    void listAddsProjectParametersAndFilters() {
        KiketClient client = Mockito.mock(KiketClient.class);
        Mockito.when(client.get(Mockito.anyString(), eq(CustomDataClient.CustomDataListResponse.class)))
            .thenReturn(Mono.just(new CustomDataClient.CustomDataListResponse()));

        CustomDataClient.CustomDataListOptions options = new CustomDataClient.CustomDataListOptions();
        options.setLimit(5);
        options.setFilters(Map.of("status", "active"));

        CustomDataClient customData = new CustomDataClient(client, "42");
        customData.list("com.example.module", "records", options);

        Mockito.verify(client).get(
            eq("/ext/custom_data/com.example.module/records?project_id=42&limit=5&filters=%7B%22status%22%3A%22active%22%7D"),
            eq(CustomDataClient.CustomDataListResponse.class)
        );
    }

    @Test
    void createSendsRecordPayload() {
        KiketClient client = Mockito.mock(KiketClient.class);
        Mockito.when(client.post(Mockito.anyString(), Mockito.anyMap(), eq(CustomDataClient.CustomDataRecordResponse.class)))
            .thenReturn(Mono.just(new CustomDataClient.CustomDataRecordResponse()));

        CustomDataClient customData = new CustomDataClient(client, "proj-1");
        Map<String, Object> record = new HashMap<>();
        record.put("email", "lead@example.com");

        customData.create("com.example.module", "records", record);

        Mockito.verify(client).post(
            eq("/ext/custom_data/com.example.module/records?project_id=proj-1"),
            argThat((ArgumentMatcher<Map<String, Object>>) body ->
                body.containsKey("record") &&
                "lead@example.com".equals(((Map<?, ?>) body.get("record")).get("email"))
            ),
            eq(CustomDataClient.CustomDataRecordResponse.class)
        );
    }

    @Test
    void updateUsesPatch() {
        KiketClient client = Mockito.mock(KiketClient.class);
        Mockito.when(client.patch(Mockito.anyString(), Mockito.anyMap(), eq(CustomDataClient.CustomDataRecordResponse.class)))
            .thenReturn(Mono.just(new CustomDataClient.CustomDataRecordResponse()));

        CustomDataClient customData = new CustomDataClient(client, "proj-1");
        customData.update("com.example.module", "records", "7", Map.of("status", "active"));

        Mockito.verify(client).patch(
            eq("/ext/custom_data/com.example.module/records/7?project_id=proj-1"),
            argThat((ArgumentMatcher<Map<String, Object>>) body ->
                body.containsKey("record") &&
                "active".equals(((Map<?, ?>) body.get("record")).get("status"))
            ),
            eq(CustomDataClient.CustomDataRecordResponse.class)
        );
    }

    @Test
    void deleteInvokesDeleteEndpoint() {
        KiketClient client = Mockito.mock(KiketClient.class);
        Mockito.when(client.delete(Mockito.anyString(), Mockito.eq(Map.class)))
            .thenReturn(Mono.just(Map.of()));

        CustomDataClient customData = new CustomDataClient(client, "42");
        customData.delete("com.example.module", "records", "9");

        Mockito.verify(client).delete(
            eq("/ext/custom_data/com.example.module/records/9?project_id=42"),
            eq(Map.class)
        );
    }
}
