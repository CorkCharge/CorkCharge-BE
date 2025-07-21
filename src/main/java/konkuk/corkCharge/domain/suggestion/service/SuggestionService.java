package konkuk.corkCharge.domain.suggestion.service;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.suggestion.domain.Suggestion;
import konkuk.corkCharge.domain.suggestion.dto.request.PostSuggestionRequest;
import konkuk.corkCharge.domain.suggestion.repository.SuggestionRepository;
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
public class SuggestionService {
    private final SuggestionRepository suggestionRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public void createSuggestion(Long userId, PostSuggestionRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        Suggestion suggestion = new Suggestion(
                user,
                restaurant,
                request.content(),
                request.category()
        );

        suggestionRepository.save(suggestion);
    }

}