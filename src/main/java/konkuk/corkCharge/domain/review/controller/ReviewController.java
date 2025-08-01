package konkuk.corkCharge.domain.review.controller;

import konkuk.corkCharge.domain.review.dto.request.PatchUpdateReviewRequest;
import konkuk.corkCharge.domain.review.dto.request.PostReviewCreateRequest;
import konkuk.corkCharge.domain.review.dto.response.GetCorkageScoreResponse;
import konkuk.corkCharge.domain.review.service.ReviewService;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{restaurantId}")
    public BaseResponse<Void> createReview(
            @PathVariable(name = "restaurantId") Long restaurantId,
            @RequestPart(value = "request") PostReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        reviewService.createReview(restaurantId, request, images);
        return BaseResponse.ok(null);
    }

    @GetMapping("/corkageScore")
    public BaseResponse<List<GetCorkageScoreResponse>> getCorkageScore(
            @RequestParam(name = "range", defaultValue = "1") String range
    ) {
        return BaseResponse.ok(reviewService.getCorkageScores(range));
    }

    @PatchMapping("/{reviewId}")
    public BaseResponse<Void> updateReview(
            @PathVariable(name = "reviewId") Long reviewId,
            @RequestPart("request") PatchUpdateReviewRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        reviewService.updateReview(reviewId, request, images);
        return BaseResponse.ok(null);
    }

    @DeleteMapping("/{reviewId}")
    public BaseResponse<Void> deleteReview(
            @PathVariable(name = "reviewId") Long reviewId,
            @RequestParam(name = "userId") Long userId
    ) {
        reviewService.deleteReview(reviewId, userId);
        return BaseResponse.ok(null);
    }

}