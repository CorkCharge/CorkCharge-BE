package konkuk.corkCharge.global.api.naverMapsApi;

import konkuk.corkCharge.global.api.naverMapsApi.dto.NaverMapsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverGeocodingClient {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    private final RestClient restClient = RestClient.create();

    public NaverMapsResponse getCoordinatesByAddress(String address) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://maps.apigw.ntruss.com/map-geocode/v2/geocode")
                .queryParam("query", address)
                .build()
                .toUri();

        return restClient.get()
                .uri(uri)
                .header("X-NCP-APIGW-API-KEY-ID", clientId)
                .header("X-NCP-APIGW-API-KEY", clientSecret)
                .retrieve()
                .body(NaverMapsResponse.class);
    }

}
