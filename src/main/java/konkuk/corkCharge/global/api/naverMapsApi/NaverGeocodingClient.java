package konkuk.corkCharge.global.api.naverMapsApi;

import konkuk.corkCharge.global.api.naverMapsApi.dto.NaverMapsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverGeocodingClient {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    private final RestClient restClient = RestClient.builder()
            .requestFactory(createRequestFactory())
            .build();

    public NaverMapsResponse getCoordinatesByAddress(String address) {
        URI uri = buildGeocodingUri(address);
        log.debug("Naver geocoding request. uri={}", uri);

        NaverMapsResponse response = restClient.get()
                .uri(uri)
                .header("X-NCP-APIGW-API-KEY-ID", clientId)
                .header("X-NCP-APIGW-API-KEY", clientSecret)
                .retrieve()
                .body(NaverMapsResponse.class);
        log.debug("Naver geocoding response. addressCount={}",
                response == null || response.addresses() == null ? null : response.addresses().size());
        return response;
    }

    static URI buildGeocodingUri(String address) {
        return UriComponentsBuilder
                .fromUriString("https://maps.apigw.ntruss.com/map-geocode/v2/geocode")
                .queryParam("query", address)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
    }

    private SimpleClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return requestFactory;
    }

}
