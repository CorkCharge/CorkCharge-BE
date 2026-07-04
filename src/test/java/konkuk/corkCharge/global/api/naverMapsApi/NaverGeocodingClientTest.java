package konkuk.corkCharge.global.api.naverMapsApi;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class NaverGeocodingClientTest {

    @Test
    void buildGeocodingUriEncodesKoreanAddressQuery() {
        URI uri = NaverGeocodingClient.buildGeocodingUri("서울특별시 강서구 마곡중앙6로 40");

        assertThat(uri.toASCIIString()).contains("query=%EC%84%9C%EC%9A%B8");
        assertThat(uri.toASCIIString()).doesNotContain(" ");
        assertThat(uri.getRawQuery()).contains("%EA%B0%95%EC%84%9C%EA%B5%AC");
    }
}
