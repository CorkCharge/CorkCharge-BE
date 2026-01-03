package konkuk.corkCharge.domain.bookmark.service;

import konkuk.corkCharge.domain.bookmark.domain.Bookmark;
import konkuk.corkCharge.domain.bookmark.domain.RestaurantBookmarkGroup;
import konkuk.corkCharge.domain.bookmark.domain.RestaurantBookmarkGroupItem;
import konkuk.corkCharge.domain.bookmark.dto.request.DeleteBookmarkRequest;
import konkuk.corkCharge.domain.bookmark.dto.request.PostBookmarkRequest;
import konkuk.corkCharge.domain.bookmark.dto.response.GetSavedReviewResponse;
import konkuk.corkCharge.domain.bookmark.dto.response.GetSavedTipResponse;
import konkuk.corkCharge.domain.bookmark.repository.BookmarkRepository;
import konkuk.corkCharge.domain.bookmark.repository.RestaurantBookmarkGroupItemRepository;
import konkuk.corkCharge.domain.bookmark.repository.RestaurantBookmarkGroupRepository;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.bookmark.dto.response.GetSavedRestaurantResponse;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.restaurant.service.RestaurantSummaryService;
import konkuk.corkCharge.domain.review.domain.Review;
import konkuk.corkCharge.domain.review.repository.ReviewRepository;
import konkuk.corkCharge.domain.tip.domain.Tip;
import konkuk.corkCharge.domain.tip.repository.TipRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static konkuk.corkCharge.domain.bookmark.domain.BookmarkTargetType.*;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final TipRepository tipRepository;
    private final ImageRepository imageRepository;
    private final CorkageStoreRepository corkageStoreRepository;
    private final RestaurantSummaryService restaurantSummaryService;
    private final RestaurantBookmarkGroupRepository restaurantBookmarkGroupRepository;
    private final RestaurantBookmarkGroupItemRepository restaurantBookmarkGroupItemRepository;

    @Transactional
    public void createBookmark(Long userId, PostBookmarkRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        switch (request.targetType()) {
            case RESTAURANT -> {
                Restaurant restaurant = restaurantRepository.findById(request.targetId())
                        .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

                if (request.groupIds() == null || request.groupIds().isEmpty()) {
                    throw new CustomException(BAD_REQUEST);
                }

                restaurant.setBookmarkCount(restaurant.getBookmarkCount() + 1);
                restaurantRepository.save(restaurant);

                // 캐시 무효화
                restaurantSummaryService.evictSummary(request.targetId());
            }
            case REVIEW -> {
                Review review = reviewRepository.findById(request.targetId())
                        .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND));

                review.setBookmarkCount(review.getBookmarkCount() + 1);
                reviewRepository.save(review);
            }
            case TIP -> {
                if (!tipRepository.existsById(request.targetId())) {
                    throw new CustomException(TIP_NOT_FOUND);
                }
            }
        }

        Bookmark bookmark = bookmarkRepository.save(
                Bookmark.builder()
                        .user(user)
                        .targetId(request.targetId())
                        .targetType(request.targetType())
                        .build()
        );

        // 북마크 그룹 매핑
        if (request.targetType() == RESTAURANT) {
            List<RestaurantBookmarkGroup> groups =
                    restaurantBookmarkGroupRepository.findAllByIdIn(request.groupIds());

            for (RestaurantBookmarkGroup group : groups) {
                restaurantBookmarkGroupItemRepository.save(
                        RestaurantBookmarkGroupItem.builder()
                                .bookmark(bookmark)
                                .group(group)
                                .build()
                );
            }
        }
    }

    @Transactional
    public void deleteBookmark(Long userId, DeleteBookmarkRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Bookmark bookmark = bookmarkRepository
                .findByUser_UserIdAndTargetTypeAndTargetId(
                        userId, request.targetType(), request.targetId())
                .orElseThrow(() -> new CustomException(BOOKMARK_NOT_FOUND));

        // restaurant는 이 함수에서 편집 불가
        if (bookmark.getTargetType() == RESTAURANT) {
            throw new CustomException(BAD_REQUEST);
        }

        bookmarkRepository.delete(bookmark);

        switch (bookmark.getTargetType()) {
            case REVIEW -> {
                Review review = reviewRepository.findById(bookmark.getTargetId())
                        .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND));

                review.setBookmarkCount(review.getBookmarkCount() - 1);
                reviewRepository.save(review);
            }
            case TIP -> {
                if (!tipRepository.existsById(bookmark.getTargetId())) {
                    throw new CustomException(TIP_NOT_FOUND);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<GetSavedRestaurantResponse> getSavedRestaurants(Long userId){
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return bookmarkRepository
                .findAllByUser_UserIdAndTargetType(userId, RESTAURANT)
                .stream()
                .map(bookmark -> {
                    Restaurant restaurant = restaurantRepository
                            .findById(bookmark.getTargetId())
                            .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));
                    List<Image> images = imageRepository.findByCategoryAndTypeId(
                            ImageCategory.RESTAURANT,
                            restaurant.getRestaurantId()
                    );

                    CorkageStore corkageStore = corkageStoreRepository
                            .findByRestaurant_RestaurantId(restaurant.getRestaurantId())
                            .orElse(null);

                    return GetSavedRestaurantResponse.from(
                            bookmark,
                            restaurant,
                            images,
                            corkageStore
                    );
                })
                .toList();
    }

    @Transactional
    public List<GetSavedReviewResponse> getSavedReviews(Long userId){
        userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(USER_NOT_FOUND));

        return bookmarkRepository
                .findAllByUser_UserIdAndTargetType(userId, REVIEW)
                .stream()
                .map(bookmark -> {
                    Review review = reviewRepository.findById(bookmark.getTargetId())
                            .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND));

                    String imageUrl = imageRepository
                            .findFirstByCategoryAndTypeId(ImageCategory.REVIEW, review.getReviewId())
                            .map(Image::getImageUrl)
                            .orElse("");

                    String authorName = review.getUser().getName();

                    String restaurantName = review.getRestaurant().getName();

                    long totalSaved = bookmarkRepository.countByTargetTypeAndTargetId(REVIEW, review.getReviewId());


                    return new GetSavedReviewResponse(
                            bookmark.getId(),
                            review.getReviewId(),
                            restaurantName,
                            (int) totalSaved,
                            imageUrl,
                            review.getRating(),
                            review.getContent(),
                            authorName,
                            review.getCreatedAt()
                    );

                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GetSavedTipResponse> getSavedTips(Long userId){
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return bookmarkRepository
                .findAllByUser_UserIdAndTargetType(userId, TIP)
                .stream()
                .map(bookmark -> {
                    Tip tip = tipRepository.findById(bookmark.getTargetId())
                            .orElseThrow(()-> new CustomException(TIP_NOT_FOUND));

                    String imageUrl = imageRepository
                            .findFirstByCategoryAndTypeId(ImageCategory.TIP, tip.getTipId())
                            .map(Image::getImageUrl)
                            .orElse("");

                    return new GetSavedTipResponse(
                            bookmark.getId(),
                            tip.getTipId(),
                            tip.getTitle(),
                            tip.getTipCategory(),
                            imageUrl,
                            tip.getCreatedAt()
                    );
                })
                .toList();
    }
}
