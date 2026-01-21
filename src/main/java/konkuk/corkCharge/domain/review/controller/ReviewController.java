package konkuk.corkCharge.domain.review.controller;

import konkuk.corkCharge.domain.review.dto.request.CorkageReviewSort;
import konkuk.corkCharge.domain.review.dto.request.PatchUpdateReviewRequest;
import konkuk.corkCharge.domain.review.dto.request.PostReviewCreateRequest;
import konkuk.corkCharge.domain.review.dto.response.GetCorkageReviewResponse;
import konkuk.corkCharge.domain.review.dto.response.GetHomeCorkageReviewResponse;
import konkuk.corkCharge.domain.review.dto.response.GetRestaurantReviewResponse;
import konkuk.corkCharge.domain.review.service.ReviewService;
import konkuk.corkCharge.global.annotation.LoginUserId;
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
            @LoginUserId Long userId,
            @PathVariable(name = "restaurantId") Long restaurantId,
            @RequestPart(value = "request") PostReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        reviewService.createReview(userId, restaurantId, request, images);
        return BaseResponse.ok(null);
    }

    @GetMapping("/corkageReview")
    public BaseResponse<List<GetCorkageReviewResponse>> getCorkageReview(
            @LoginUserId(required = false) Long userId,
            @RequestParam(name = "sort", defaultValue = "BOOKMARK") CorkageReviewSort sort
    ) {
        return BaseResponse.ok(reviewService.getCorkageReviews(userId, sort));
    }

    @PatchMapping("/{reviewId}")
    public BaseResponse<Void> updateReview(
            @LoginUserId Long userId,
            @PathVariable(name = "reviewId") Long reviewId,
            @RequestPart("request") PatchUpdateReviewRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        reviewService.updateReview(userId, reviewId, request, images);
        return BaseResponse.ok(null);
    }

    @DeleteMapping("/{reviewId}")
    public BaseResponse<Void> deleteReview(
            @LoginUserId Long userId,
            @PathVariable(name = "reviewId") Long reviewId
    ) {
        reviewService.deleteReview(userId, reviewId);
        return BaseResponse.ok(null);
    }

    @GetMapping("/{restaurantId}")
    public BaseResponse<List<GetRestaurantReviewResponse>> getRestaurantReviews(
            @LoginUserId(required = false) Long userId,
            @PathVariable(name = "restaurantId") Long restaurantId
    ) {
        return BaseResponse.ok(reviewService.getRestaurantReviews(userId, restaurantId));
    }

    @GetMapping("/home")
    public BaseResponse<List<GetHomeCorkageReviewResponse>> getHomeCorkageReviews() {
        return BaseResponse.ok(reviewService.getHomeCorkageReviews());
    }

}