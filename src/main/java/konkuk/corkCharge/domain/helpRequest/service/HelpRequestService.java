package konkuk.corkCharge.domain.helpRequest.service;

import konkuk.corkCharge.domain.helpRequest.domain.HelpRequest;
import konkuk.corkCharge.domain.helpRequest.dto.request.PostCorkageRequest;
import konkuk.corkCharge.domain.helpRequest.repository.HelpRequestRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.RESTAURANT_NOT_FOUND;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class HelpRequestService {

    private final HelpRequestRepository helpRequestRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public void createHelpRequest(Long userId, PostCorkageRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        HelpRequest helpRequest = HelpRequest.builder()
                .user(user)
                .restaurant(restaurant)
                .content(request.content())
                .build();

        helpRequestRepository.save(helpRequest);
    }
}
