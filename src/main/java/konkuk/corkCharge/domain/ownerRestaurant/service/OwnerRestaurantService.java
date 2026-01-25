package konkuk.corkCharge.domain.ownerRestaurant.service;

import konkuk.corkCharge.domain.ownerRestaurant.domain.OwnerRestaurant;
import konkuk.corkCharge.domain.ownerRestaurant.dto.mapper.OwnerMyRestaurantResponseMapper;
import konkuk.corkCharge.domain.ownerRestaurant.dto.response.GetOwnerMyRestaurantListResponse;
import konkuk.corkCharge.domain.ownerRestaurant.repository.OwnerRestaurantRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.restaurant.service.RestaurantSummaryService;
import konkuk.corkCharge.domain.user.domain.Role;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class OwnerRestaurantService {

    private final OwnerRestaurantRepository ownerRestaurantRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantSummaryService restaurantSummaryService;
    private final OwnerMyRestaurantResponseMapper ownerMyRestaurantResponseMapper;

    @Transactional
    public void registerRestaurant(Long userId, Long restaurantId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        if(ownerRestaurantRepository.existsByRestaurant(restaurant)){
            throw new CustomException(ALREADY_REGISTERED_BY_ANOTHER_OWNER);
        }

        OwnerRestaurant ownerRestaurant = OwnerRestaurant.builder()
                        .restaurant(restaurant)
                        .user(user)
                        .build();

        ownerRestaurantRepository.save(ownerRestaurant);
    }

    @Transactional(readOnly = true)
    public GetOwnerMyRestaurantListResponse getMyRestaurants(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (user.getRole() != Role.OWNER) {
            throw new CustomException(ACCESS_DENIED);
        }

        List<Long> restaurantIds = ownerRestaurantRepository.findRestaurantIdsByUserId(userId);
        if (restaurantIds.isEmpty()) {
            return new GetOwnerMyRestaurantListResponse(List.of());
        }

        List<RestaurantSummary> summaries = restaurantSummaryService.getSummariesInOrder(restaurantIds);

        List<GetOwnerMyRestaurantListResponse.Item> items = summaries.stream()
                .map(ownerMyRestaurantResponseMapper::toItem)
                .toList();

        return new GetOwnerMyRestaurantListResponse(items);
    }

}