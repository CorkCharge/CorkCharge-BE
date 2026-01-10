package konkuk.corkCharge.domain.review.service;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.image.service.S3ImageService;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.restaurant.service.RestaurantSummaryService;
import konkuk.corkCharge.domain.review.domain.Review;
import konkuk.corkCharge.domain.review.domain.ReviewRange;
import konkuk.corkCharge.domain.review.dto.mapper.GetRestaurantReviewResponseMapper;
import konkuk.corkCharge.domain.review.dto.request.CorkageReviewSort;
import konkuk.corkCharge.domain.review.dto.request.PatchUpdateReviewRequest;
import konkuk.corkCharge.domain.review.dto.request.PostReviewCreateRequest;
import konkuk.corkCharge.domain.review.dto.response.GetCorkageReviewResponse;
import konkuk.corkCharge.domain.review.dto.response.GetRestaurantReviewResponse;
import konkuk.corkCharge.domain.review.repository.CorkageReviewProjection;
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
import java.util.Map;
import java.util.stream.Collectors;

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
    private final RestaurantSummaryService restaurantSummaryService;

    private final GetRestaurantReviewResponseMapper getRestaurantReviewResponseMapper;

    @Transactional
    public void createReview(Long userId, Long restaurantId,
                             PostReviewCreateRequest requestDto,
                             List<MultipartFile> images) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        Review review = Review.builder()
                .restaurant(restaurant)
                .user(user)
                .content(requestDto.content())
                .rating(requestDto.rating())
                .build();

        reviewRepository.save(review);

        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = s3ImageService.uploadImages(images, REVIEW, null);

            for (String url : uploadedUrls) {
                Image image = Image.builder()
                        .typeId(review.getReviewId())
                        .category(REVIEW)
                        .imageUrl(url)
                        .build();
                imageRepository.save(image);
            }
        }

        updateAverageRating(restaurant);
        // 캐시 무효화
        restaurantSummaryService.evictSummary(restaurantId);
    }

    private void updateAverageRating(Restaurant restaurant) {
        List<Review> reviews = reviewRepository.findAllByRestaurant_RestaurantId(restaurant.getRestaurantId());

        double avg = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        restaurant.updateRating(avg);
        restaurant.setReviewCount(reviews.size());
        restaurantRepository.save(restaurant);
    }

    public List<GetCorkageReviewResponse> getCorkageScores(CorkageReviewSort sort) {

        List<CorkageReviewProjection> rows = switch (sort) {
            case LATEST -> reviewRepository.findAllCorkageReviewsOrderByLatest();
            case BOOKMARK -> reviewRepository.findAllCorkageReviewsOrderByBookmark();
        };

        if (rows.isEmpty())
            return List.of();

        List<Long> reviewIds = rows.stream()
                .map(CorkageReviewProjection::getReviewId)
                .toList();

        Map<Long, List<String>> imageMap =
                imageRepository.findReviewImagesByReviewIds(reviewIds).stream()
                        .collect(Collectors.groupingBy(
                                Image::getTypeId,
                                Collectors.mapping(Image::getImageUrl, Collectors.toList())
                        ));

        return rows.stream()
                .map(row -> new GetCorkageReviewResponse(
                        row.getReviewId(),
                        row.getRestaurantId(),
                        row.getRestaurantName(),
                        row.getWriter(),
                        row.getContent(),
                        row.getRating() == null ? 0 : row.getRating(),
                        row.getCreatedAt(),
                        imageMap.getOrDefault(row.getReviewId(), List.of()),
                        row.getBookmarkCount() == null ? 0 : row.getBookmarkCount()
                ))
                .toList();
    }

    @Transactional
    public void updateReview(Long userId, Long reviewId,
                             PatchUpdateReviewRequest request,
                             List<MultipartFile> images) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(FORBIDDEN_REVIEW_EDIT);
        }

        review.updateContent(request.content());
        review.updateRating(request.rating());

        if (images != null && !images.isEmpty()) {
            imageRepository.findByCategoryAndTypeId(REVIEW, reviewId)
                    .forEach(img -> s3ImageService.deleteImage(img.getImageUrl()));
            imageRepository.deleteByCategoryAndTypeId(REVIEW, reviewId);

            // 새 이미지 업로드/저장
            List<String> uploadedUrls = s3ImageService.uploadImages(images, REVIEW, null);
            for (String url : uploadedUrls) {
                Image image = Image.builder()
                        .typeId(reviewId)
                        .category(REVIEW)
                        .imageUrl(url)
                        .build();
                imageRepository.save(image);
            }
        }

        updateAverageRating(review.getRestaurant());
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(FORBIDDEN_REVIEW_EDIT);
        }

        // 이미지 삭제
        imageRepository.findByCategoryAndTypeId(REVIEW, reviewId)
                .forEach(img -> s3ImageService.deleteImage(img.getImageUrl()));
        imageRepository.deleteByCategoryAndTypeId(REVIEW, reviewId);

        reviewRepository.delete(review);

        updateAverageRating(review.getRestaurant());
    }

    @Transactional(readOnly = true)
    public List<GetRestaurantReviewResponse> getRestaurantReviews(Long restaurantId) {

        List<Review> reviews =
                reviewRepository.findByRestaurant_RestaurantIdOrderByCreatedAtDesc(restaurantId);

        if (reviews.isEmpty()) {
            return List.of();
        }

        List<Long> reviewIds = reviews.stream()
                .map(Review::getReviewId)
                .toList();

        // 리뷰 이미지
        Map<Long, List<String>> imageMap =
                imageRepository.findReviewImagesByReviewIds(reviewIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                Image::getTypeId,
                                Collectors.mapping(Image::getImageUrl, Collectors.toList())
                        ));

        return reviews.stream()
                .map(review -> getRestaurantReviewResponseMapper.toResponse(
                        review,
                        imageMap.getOrDefault(review.getReviewId(), List.of())
                ))
                .toList();

    }

}
