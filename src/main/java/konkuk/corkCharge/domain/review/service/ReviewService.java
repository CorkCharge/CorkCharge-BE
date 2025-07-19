package konkuk.corkCharge.domain.review.service;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.image.service.S3ImageService;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.review.domain.Review;
import konkuk.corkCharge.domain.review.dto.request.PostReviewCreateRequest;
import konkuk.corkCharge.domain.review.repository.ReviewRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.REVIEW;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.RESTAURANT_NOT_FOUND;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

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
                        .build();
                imageRepository.save(image);
            }
        }

    }
}
