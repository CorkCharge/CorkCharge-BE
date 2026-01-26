package konkuk.corkCharge.domain.review.service;

import konkuk.corkCharge.domain.bookmark.domain.Bookmark;
import konkuk.corkCharge.domain.bookmark.domain.BookmarkTargetType;
import konkuk.corkCharge.domain.bookmark.repository.BookmarkRepository;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.image.service.S3ImageService;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.restaurant.service.RestaurantSummaryService;
import konkuk.corkCharge.domain.review.domain.Review;
import konkuk.corkCharge.domain.review.dto.mapper.GetRestaurantReviewResponseMapper;
import konkuk.corkCharge.domain.review.dto.request.CorkageReviewSort;
import konkuk.corkCharge.domain.review.dto.request.GetCorkageReviewRequest;
import konkuk.corkCharge.domain.review.dto.request.PatchUpdateReviewRequest;
import konkuk.corkCharge.domain.review.dto.request.PostReviewCreateRequest;
import konkuk.corkCharge.domain.review.dto.response.GetCorkageReviewResponse;
import konkuk.corkCharge.domain.review.dto.response.GetHomeCorkageReviewResponse;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final BookmarkRepository bookmarkRepository;

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

    @Transactional(readOnly = true)
    public List<GetCorkageReviewResponse> getCorkageReviews(Long userId, GetCorkageReviewRequest req) {

        CorkageReviewSort sort = (req == null || req.sort() == null) ? CorkageReviewSort.BOOKMARK : req.sort();

        String keyword = (req == null || req.keyword() == null) ? null : req.keyword().trim();
        String sido = (req == null) ? null : req.sido();
        String sigungu = (req == null) ? null : req.sigungu();

        String dongRegex = null;
        if (req != null && req.dongList() != null && !req.dongList().isEmpty()) {
            dongRegex = String.join("|", req.dongList());
        }

        List<CorkageReviewProjection> rows = reviewRepository.searchCorkageReviews(
                keyword,
                sido,
                sigungu,
                dongRegex,
                sort.name()
        );

        if (rows.isEmpty()) return List.of();

        List<Long> reviewIds = rows.stream()
                .map(CorkageReviewProjection::getReviewId)
                .toList();

        Map<Long, List<String>> imageMap = imageRepository.findReviewImagesByReviewIds(reviewIds).stream()
                .collect(Collectors.groupingBy(
                        Image::getTypeId,
                        Collectors.mapping(Image::getImageUrl, Collectors.toList())
                ));

        Set<Long> scrappedReviewIds = getScrappedReviewIds(userId, reviewIds);

        return rows.stream()
                .map(row -> {
                    boolean scrap = userId != null && scrappedReviewIds.contains(row.getReviewId());

                    return new GetCorkageReviewResponse(
                            row.getReviewId(),
                            row.getRestaurantId(),
                            row.getRestaurantName(),
                            row.getWriter(),
                            row.getContent(),
                            row.getRating() == null ? 0 : row.getRating(),
                            row.getCreatedAt(),
                            imageMap.getOrDefault(row.getReviewId(), List.of()),
                            row.getBookmarkCount() == null ? 0 : row.getBookmarkCount(),
                            scrap
                    );
                })
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
            imageRepository.findAllByCategoryAndTypeId(REVIEW, reviewId)
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
        imageRepository.findAllByCategoryAndTypeId(REVIEW, reviewId)
                .forEach(img -> s3ImageService.deleteImage(img.getImageUrl()));
        imageRepository.deleteByCategoryAndTypeId(REVIEW, reviewId);

        reviewRepository.delete(review);

        updateAverageRating(review.getRestaurant());
    }

    @Transactional(readOnly = true)
    public List<GetRestaurantReviewResponse> getRestaurantReviews(Long userId, Long restaurantId) {

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

        Set<Long> scrappedReviewIds = getScrappedReviewIds(userId, reviewIds);

        return reviews.stream()
                .map(review -> {
                    boolean scrap =  userId != null && scrappedReviewIds.contains(review.getReviewId());

                    return getRestaurantReviewResponseMapper.toResponse(
                            review,
                            imageMap.getOrDefault(review.getReviewId(), List.of()),
                            scrap
                            );
                })
                .toList();
    }

    private Set<Long> getScrappedReviewIds(Long userId, List<Long> reviewIds) {
        Set<Long> scrappedReviewIds = (userId == null) ? Collections.emptySet() : bookmarkRepository
                .findAllByUserIdAndTargetTypeAndTargetIdIn(userId, BookmarkTargetType.REVIEW, reviewIds).stream()
                .map(Bookmark::getTargetId)
                .collect(Collectors.toSet());
        return scrappedReviewIds;
    }

    @Transactional(readOnly = true)
    public List<GetHomeCorkageReviewResponse> getHomeCorkageReviews() {
        final int LIMIT = 5;

        List<CorkageReviewProjection> rows =
                reviewRepository.findTopCorkageReviewsOrderByBookmark(LIMIT);

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
                .map(row -> new GetHomeCorkageReviewResponse(
                        row.getReviewId(),
                        row.getRestaurantId(),
                        row.getRestaurantName(),
                        row.getWriter(),
                        row.getContent(),
                        row.getRating() == null ? 0 : row.getRating(),
                        row.getCreatedAt(),
                        imageMap.getOrDefault(row.getReviewId(), List.of())
                ))
                .toList();
    }

}
