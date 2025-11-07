package dev.kiket.sdk.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * HTTP client for Kiket API.
 */
public class KiketClient {
    private final WebClient webClient;
    private final String workspaceToken;
    private final String eventVersion;

    public KiketClient(String baseUrl, String workspaceToken, String eventVersion) {
        this.workspaceToken = workspaceToken;
        this.eventVersion = eventVersion;

        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "kiket-sdk-java/0.1.0")
            .build();
    }

    public <T> Mono<T> get(String path, Class<T> responseType) {
        return webClient.get()
            .uri(path)
            .headers(this::addAuthHeaders)
            .retrieve()
            .bodyToMono(responseType);
    }

    public <T> Mono<T> post(String path, Object body, Class<T> responseType) {
        return webClient.post()
            .uri(path)
            .headers(this::addAuthHeaders)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType);
    }

    public <T> Mono<T> put(String path, Object body, Class<T> responseType) {
        return webClient.put()
            .uri(path)
            .headers(this::addAuthHeaders)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType);
    }

    public <T> Mono<T> delete(String path, Class<T> responseType) {
        return webClient.delete()
            .uri(path)
            .headers(this::addAuthHeaders)
            .retrieve()
            .bodyToMono(responseType);
    }

    private void addAuthHeaders(HttpHeaders headers) {
        if (workspaceToken != null) {
            headers.setBearerAuth(workspaceToken);
        }
        if (eventVersion != null) {
            headers.set("X-Kiket-Event-Version", eventVersion);
        }
    }
}
