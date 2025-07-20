package konkuk.corkCharge.domain.suggestion.controller;

import konkuk.corkCharge.domain.suggestion.dto.request.PostSuggestionRequest;
import konkuk.corkCharge.domain.suggestion.service.SuggestionService;
import konkuk.corkCharge.domain.user.dto.response.GetUserProfileResponse;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suggestion")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;

    @GetMapping
    public BaseResponse<GetUserProfileResponse> postSuggestion(@RequestParam Long userId, @RequestBody PostSuggestionRequest request){
        suggestionService.createSuggestion(userId, request);
        return BaseResponse.ok(null);
    }
}
