package konkuk.corkCharge.domain.suggestion.service;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.suggestion.dto.request.PostSuggestionRequest;
import konkuk.corkCharge.domain.suggestion.repository.SuggestionRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.dto.response.GetUserProfileResponse;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.domain.user.service.UserService;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class SuggestionService {
    private final SuggestionRepository suggestionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createSuggestion(Long userId, PostSuggestionRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById()
    }

}