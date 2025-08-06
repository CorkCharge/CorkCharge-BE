package konkuk.corkCharge.domain.bookmark.service;

import konkuk.corkCharge.domain.bookmark.domain.Bookmark;
import konkuk.corkCharge.domain.bookmark.dto.request.PostBookmarkRequest;
import konkuk.corkCharge.domain.bookmark.repository.BookmarkRepository;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.review.repository.ReviewRepository;
import konkuk.corkCharge.domain.tip.repository.TipRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void createBookmark(Long userId, PostBookmarkRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        switch(request.targetType()){
            case RESTAURANT -> {
                if (!restaurantRepository.existsById(request.targetId())) {
                    throw new CustomException(RESTAURANT_NOT_FOUND);
                }
            }
            case REVIEW -> {
                if (!reviewRepository.existsById(request.targetId())) {
                    throw new CustomException(REVIEW_NOT_FOUND);
                }
            }
            case TIP -> {
                if (!tipRepository.existsById(request.targetId())) {
                    throw new CustomException(TIP_NOT_FOUND);
                }
            }
        }
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .targetId(request.targetId())
                .targetType(request.targetType())
                .build();

        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void deleteBookmark(Long bookmarkId){
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                        .orElseThrow(() -> new CustomException(BOOKMARK_NOT_FOUND));

        bookmarkRepository.delete(bookmark);
    }
}
