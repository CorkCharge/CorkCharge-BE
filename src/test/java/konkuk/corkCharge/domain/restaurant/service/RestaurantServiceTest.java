package konkuk.corkCharge.domain.restaurant.service;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.dto.response.PostRestaurantGeocodingResponse;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.global.api.naverMapsApi.NaverGeocodingClient;
import konkuk.corkCharge.global.api.naverMapsApi.dto.Address;
import konkuk.corkCharge.global.api.naverMapsApi.dto.NaverMapsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private NaverGeocodingClient naverGeocodingClient;

    @InjectMocks
    private RestaurantService restaurantService;

    @Test
    void geocodeMissingLocationsProcessesOnlyRequestedLimit() {
        Restaurant restaurant = Restaurant.builder()
                .name("마곡 테스트 매장")
                .address("서울특별시 강서구 마곡중앙로 161-8")
                .build();

        when(restaurantRepository.findRestaurantsWithoutValidCoordinates(org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(List.of(restaurant));
        when(naverGeocodingClient.getCoordinatesByAddress(anyString()))
                .thenReturn(new NaverMapsResponse(List.of(new Address("126.830", "37.560"))));

        PostRestaurantGeocodingResponse response = restaurantService.geocodeMissingLocations(50);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(restaurantRepository).findRestaurantsWithoutValidCoordinates(pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(50);
        assertThat(response.requestedLimit()).isEqualTo(50);
        assertThat(response.targetCount()).isEqualTo(1);
        assertThat(response.successCount()).isEqualTo(1);
        assertThat(restaurant.getLatitude()).isEqualTo(37.560);
        assertThat(restaurant.getLongitude()).isEqualTo(126.830);
    }
}
