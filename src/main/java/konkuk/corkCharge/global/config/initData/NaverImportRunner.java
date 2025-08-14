package konkuk.corkCharge.global.config.initData;

import konkuk.corkCharge.domain.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class NaverImportRunner implements ApplicationRunner {

    private final RestaurantService restaurantService;

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("second");
        log.info(">>> NaverImportRunner START");
        try {
            long updated = restaurantService.importFromNaver();
            log.info(">>> NaverImportRunner DONE, updated={}", updated);
        } catch (Exception e) {
            log.error(">>> NaverImportRunner FAILED", e);
        }
        // ※ 프로세스 종료 안 함 (서버 계속 동작)
    }
}