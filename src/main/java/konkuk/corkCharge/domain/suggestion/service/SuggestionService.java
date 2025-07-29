package konkuk.corkCharge.domain.suggestion.service;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.suggestion.domain.Suggestion;
import konkuk.corkCharge.domain.suggestion.dto.request.PostSuggestionRequest;
import konkuk.corkCharge.domain.suggestion.dto.response.GetSuggestionDetailResponse;
import konkuk.corkCharge.domain.suggestion.dto.response.GetSuggestionListResponse;
import konkuk.corkCharge.domain.suggestion.repository.SuggestionRepository;
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
public class SuggestionService {
    private final SuggestionRepository suggestionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createSuggestion(Long userId, PostSuggestionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Suggestion suggestion = Suggestion.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .suggestionCategory(request.category())
                .build();

        suggestionRepository.save(suggestion);
    }

    @Transactional(readOnly = true)
    public List<GetSuggestionListResponse> getSuggestions(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return suggestionRepository.findAllByUser_UserId(userId).stream()
                .map(suggestion -> new GetSuggestionListResponse(
                        suggestion.getSuggestionId(),
                        suggestion.getTitle(),
                        suggestion.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public GetSuggestionDetailResponse getSuggestionDetail(Long suggestionId){
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new CustomException(SUGGESTION_NOT_FOUND));

        return new GetSuggestionDetailResponse(
                suggestion.getTitle(),
                suggestion.getContent(),
                suggestion.getCreatedAt()
        );
    }
}

