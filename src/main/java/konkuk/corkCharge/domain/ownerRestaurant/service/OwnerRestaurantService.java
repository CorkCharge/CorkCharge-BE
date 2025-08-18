package konkuk.corkCharge.domain.ownerRestaurant.service;

import konkuk.corkCharge.domain.ownerRestaurant.domain.OwnerRestaurant;
import konkuk.corkCharge.domain.ownerRestaurant.repository.OwnerRestaurantRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class OwnerRestaurantService {

    private final OwnerRestaurantRepository ownerRestaurantRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

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
}
