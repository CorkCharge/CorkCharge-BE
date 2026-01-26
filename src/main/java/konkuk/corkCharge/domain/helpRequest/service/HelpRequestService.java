package konkuk.corkCharge.domain.helpRequest.service;

import konkuk.corkCharge.domain.helpRequest.domain.HelpRequest;
import konkuk.corkCharge.domain.helpRequest.dto.request.GetHelpRequestRestaurantsRequest;
import konkuk.corkCharge.domain.helpRequest.dto.request.PostHelpRequestDetailRequest;
import konkuk.corkCharge.domain.helpRequest.dto.response.GetHelpRequestRestaurantsResponse;
import konkuk.corkCharge.domain.helpRequest.repository.HelpRequestRepository;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.domain.ImageType;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class HelpRequestService {

    private final HelpRequestRepository helpRequestRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public void createHelpRequest(Long userId, Long restaurantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        // 이미 요청한 적 있는지 검사
        if (helpRequestRepository.existsByUserAndRestaurant(user, restaurant)) {
            throw new CustomException(ALREADY_COMPLETED_HELP_REQUEST);
        }

        HelpRequest helpRequest = HelpRequest.builder()
                .user(user)
                .restaurant(restaurant)
                .build();

        restaurant.increaseHelpRequestCount();

        helpRequestRepository.save(helpRequest);
    }

    @Transactional
    public void submitDetail(Long userId, PostHelpRequestDetailRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        HelpRequest helpRequest = helpRequestRepository
                .findByUserAndRestaurant(user, restaurant)
                .orElseThrow(() -> new CustomException(HELP_REQUEST_NOT_FOUND));

        helpRequest.updateDetail(
                request.corkageType(),
                request.preferredPrice(),
                request.firstPriority(),
                request.secondPriority(),
                request.content()
        );
    }

    public GetHelpRequestRestaurantsResponse getHelpRequestRestaurants(
            GetHelpRequestRestaurantsRequest request
    ) {
        String sido = request != null ? request.sido() : null;
        String sigungu = request != null ? request.sigungu() : null;
        List<String> dong = request != null ? request.dong() : null;
        String keyword = request != null ? request.keyword() : null;

        // dong 제외하고 DB 조회
        List<Restaurant> restaurantEntities =
                restaurantRepository.findHelpRequestTargetRestaurants(
                        sido,
                        sigungu,
                        keyword
                );

        // dong 필터링 (괄호 기준)
        if (dong != null && !dong.isEmpty()) {
            restaurantEntities = restaurantEntities.stream()
                    .filter(r ->
                            dong.stream().anyMatch(d ->
                                    r.getAddress().contains("(" + d + ")")
                            )
                    )
                    .toList();
        }

        // 요청 수 기준 재정렬
        restaurantEntities = restaurantEntities.stream()
                .sorted(Comparator.comparing(Restaurant::getHelpRequestCount).reversed())
                .toList();

        // DTO 매핑
        List<GetHelpRequestRestaurantsResponse.RestaurantInfoSummary> restaurants =
                restaurantEntities.stream()
                        .map(restaurant -> {

                            String mainImageUrl = imageRepository
                                    .findFirstByCategoryAndTypeIdAndType(
                                            ImageCategory.RESTAURANT,
                                            restaurant.getRestaurantId(),
                                            ImageType.MAIN
                                    )
                                    .map(Image::getImageUrl)
                                    .orElse(null);

                            return new GetHelpRequestRestaurantsResponse.RestaurantInfoSummary(
                                    restaurant.getRestaurantId(),
                                    restaurant.getName(),
                                    restaurant.getAddress(),
                                    restaurant.getHelpRequestCount(),
                                    restaurant.getOpeningHours(),
                                    mainImageUrl
                            );
                        })
                        .toList();

        return new GetHelpRequestRestaurantsResponse(restaurants);
    }
}
