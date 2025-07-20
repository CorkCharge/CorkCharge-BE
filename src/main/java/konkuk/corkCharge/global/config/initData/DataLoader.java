package konkuk.corkCharge.global.config.initData;

import konkuk.corkCharge.global.api.GJRestaurantOpenApi.GwangjinRestaurantOpenApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
public class DataLoader implements ApplicationRunner {

    private final GwangjinRestaurantOpenApiClient restaurantClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        restaurantClient.fetchAndSaveRestaurants();
    }
}
