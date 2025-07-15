package konkuk.corkCharge.global.openApi.GJRestaurantOpenApi;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.global.exception.CustomException;
import konkuk.corkCharge.global.openApi.GJRestaurantOpenApi.dto.RestaurantOpenApiResponse;
import konkuk.corkCharge.global.openApi.GJRestaurantOpenApi.dto.RestaurantRowDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

import static konkuk.corkCharge.global.openApi.GJRestaurantOpenApi.exception.OpenApiException.RESTAURANT_API_ERROR;

@Component
@RequiredArgsConstructor
@Slf4j
public class GwangjinRestaurantOpenApiClient {

    private final RestaurantRepository restaurantRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openApi.serviceKey}")
    private String AUTH_ENCODING_KEY;

    @Value("${openApi.baseUrl}")
    private String BASE_URL;

    private static final String DATASET = "LOCALDATA_072404_GJ";

    public void fetchAndSaveRestaurants() {
        String url = String.format("%s/%s/json/%s/1/1000/", BASE_URL, AUTH_ENCODING_KEY, DATASET);

        try {
            RestaurantOpenApiResponse response = restTemplate.getForObject(url, RestaurantOpenApiResponse.class);

            Set<String> stateNames = new HashSet<>();
            for (RestaurantRowDto rowDto : response.getLocalData().getRow()) {
                if (!"영업".equals(rowDto.getDTLSTATENM()))
                    continue;

                Restaurant restaurant = Restaurant.builder()
                        .name(rowDto.getBPLCNM())
                        .address(rowDto.getRDNWHLADDR())
                        .roadZipCode(rowDto.getRDNPOSTNO())
                        .phone(rowDto.getSITETEL())
                        .latitude(0.0)
                        .longitude(0.0)
                        .bookmarkCount(0)
                        .rating(null)
                        .hasCorkage(false)
                        .build();

                restaurantRepository.save(restaurant);
            }
        } catch (Exception e) {
            throw new CustomException(RESTAURANT_API_ERROR);
        }

    }
}
