package konkuk.corkCharge.domain.suggestion.controller;

import konkuk.corkCharge.domain.suggestion.dto.request.PostSuggestionRequest;
import konkuk.corkCharge.domain.suggestion.dto.response.GetSuggestionDetailResponse;
import konkuk.corkCharge.domain.suggestion.dto.response.GetSuggestionListResponse;
import konkuk.corkCharge.domain.suggestion.service.SuggestionService;
import konkuk.corkCharge.domain.user.dto.response.GetUserProfileResponse;
import konkuk.corkCharge.global.annotation.LoginUserId;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suggestion")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;

    @PostMapping
    public BaseResponse<Void> postSuggestion(
            @LoginUserId Long userId,
            @RequestBody PostSuggestionRequest request
    ){
        suggestionService.createSuggestion(userId, request);
        return BaseResponse.ok(null);
    }

    @GetMapping
    public BaseResponse<List<GetSuggestionListResponse>> getSuggestions(
            @LoginUserId Long userId
    ){
        return BaseResponse.ok(suggestionService.getSuggestions(userId));
    }

    @GetMapping("/{suggestionId}")
    public BaseResponse<GetSuggestionDetailResponse> getSuggestionDetail(
            @LoginUserId Long userId,
            @PathVariable Long suggestionId
    ){
        return BaseResponse.ok(suggestionService.getSuggestionDetail(userId, suggestionId));
    }
}
