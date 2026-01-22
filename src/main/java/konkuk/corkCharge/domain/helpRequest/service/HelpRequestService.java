package konkuk.corkCharge.domain.helpRequest.service;

import konkuk.corkCharge.domain.helpRequest.domain.HelpRequest;
import konkuk.corkCharge.domain.helpRequest.dto.request.PostHelpRequestDetailRequest;
import konkuk.corkCharge.domain.helpRequest.repository.HelpRequestRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class HelpRequestService {

    private final HelpRequestRepository helpRequestRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

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
}
