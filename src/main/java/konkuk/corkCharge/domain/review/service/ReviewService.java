package konkuk.corkCharge.domain.review.service;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.image.service.S3ImageService;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.review.domain.Review;
import konkuk.corkCharge.domain.review.domain.ReviewRange;
import konkuk.corkCharge.domain.review.dto.request.PatchUpdateReviewRequest;
import konkuk.corkCharge.domain.review.dto.request.PostReviewCreateRequest;
import konkuk.corkCharge.domain.review.dto.response.GetCorkageScoreResponse;
import konkuk.corkCharge.domain.review.repository.ReviewRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.REVIEW;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ImageRepository imageRepository;
    private final S3ImageService s3ImageService;

    public void createReview(Long restaurantId, PostReviewCreateRequest requestDto, List<MultipartFile> images) {
         User user = userRepository.findById(requestDto.userId())
                 .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        Review review = Review.builder()
                .restaurant(restaurant)
                .user(user)
                .content(requestDto.content())
                .rating(requestDto.rating())
                .build();

        user.addReview(review);
        reviewRepository.save(review);

        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = s3ImageService.uploadImages(images, REVIEW, null);

            for (String url : uploadedUrls) {
                Image image = Image.builder()
                        .review(review)
                        .imageUrl(url)
                        .category(REVIEW)
                        .build();
                imageRepository.save(image);
            }
        }
        updateAverageRating(restaurant);
    }

    private void updateAverageRating(Restaurant restaurant) {
        List<Review> reviews = restaurant.getReviews();

        double avg = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        restaurant.updateRating(avg);
        restaurantRepository.save(restaurant);
    }

    public List<GetCorkageScoreResponse> getCorkageScores(String range) {
        ReviewRange reviewRange = ReviewRange.fromValue(range);
        LocalDateTime from = reviewRange.getFromDate();

        List<Review> reviews = reviewRepository.findRecentReviews(from);

        return reviews.stream()
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .map(GetCorkageScoreResponse::from)
                .toList();
    }

    @Transactional
    public void updateReview(Long reviewId, PatchUpdateReviewRequest request, List<MultipartFile> images) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(FORBIDDEN_REVIEW_EDIT);
        }

        review.updateContent(request.content());
        review.updateRating(request.rating());

        if (images != null && !images.isEmpty()) {
            for (Image image : review.getImages()) {
                s3ImageService.deleteImage(image.getImageUrl());
            }
            imageRepository.deleteAll(review.getImages());
            review.getImages().clear();

            List<String> uploadedUrls = s3ImageService.uploadImages(images, REVIEW, null);
            for (String url : uploadedUrls) {
                Image image = Image.builder()
                        .review(review)
                        .imageUrl(url)
                        .category(REVIEW)
                        .build();
                imageRepository.save(image);
                review.getImages().add(image);
            }
        }
        updateAverageRating(review.getRestaurant());
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(FORBIDDEN_REVIEW_EDIT);
        }

        for (Image image : review.getImages()) {
            s3ImageService.deleteImage(image.getImageUrl());
        }

        imageRepository.deleteAll(review.getImages());
        reviewRepository.delete(review);
    }

}
