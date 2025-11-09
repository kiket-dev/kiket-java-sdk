package dev.kiket.sdk.endpoints;

import dev.kiket.sdk.client.KiketClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;

class SlaEventsClientTest {

    @Test
    void listBuildsQueryParams() {
        KiketClient client = Mockito.mock(KiketClient.class);
        Mockito.when(client.get(Mockito.anyString(), eq(SlaEventsClient.SlaEventsResponse.class)))
            .thenReturn(Mono.just(new SlaEventsClient.SlaEventsResponse()));

        SlaEventsClient.SlaEventsListOptions options = new SlaEventsClient.SlaEventsListOptions();
        options.setIssueId("77");
        options.setState("breached");
        options.setLimit(5);

        SlaEventsClient sla = new SlaEventsClient(client, "proj-9");
        sla.list(options);

        Mockito.verify(client).get(
            eq("/ext/sla/events?project_id=proj-9&issue_id=77&state=breached&limit=5"),
            eq(SlaEventsClient.SlaEventsResponse.class)
        );
    }
}
