package konkuk.corkCharge.domain.bookmark.service;

import konkuk.corkCharge.domain.bookmark.domain.Bookmark;
import konkuk.corkCharge.domain.bookmark.domain.RestaurantBookmarkGroup;
import konkuk.corkCharge.domain.bookmark.domain.RestaurantBookmarkGroupItem;
import konkuk.corkCharge.domain.bookmark.dto.request.PostBookmarkGroupRequest;
import konkuk.corkCharge.domain.bookmark.dto.request.PutBookmarkGroupRequest;
import konkuk.corkCharge.domain.bookmark.dto.response.PostBookmarkGroupResponse;
import konkuk.corkCharge.domain.bookmark.dto.response.PutBookmarkGroupResponse;
import konkuk.corkCharge.domain.bookmark.repository.BookmarkRepository;
import konkuk.corkCharge.domain.bookmark.repository.RestaurantBookmarkGroupItemRepository;
import konkuk.corkCharge.domain.bookmark.repository.RestaurantBookmarkGroupRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.restaurant.service.RestaurantSummaryService;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.domain.user.service.UserService;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class BookmarkGroupService {

    private final UserRepository userRepository;
    private final RestaurantBookmarkGroupRepository groupRepository;
    private final RestaurantBookmarkGroupItemRepository restaurantBookmarkGroupItemRepository;
    private final BookmarkRepository bookmarkRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantSummaryService restaurantSummaryService;


    @Transactional
    public PostBookmarkGroupResponse createGroup(
            Long userId,
            PostBookmarkGroupRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (request.name() == null || request.name().isBlank()) {
            throw new CustomException(BAD_REQUEST);
        }

        if (groupRepository.existsByUser_UserIdAndName(userId, request.name())) {
            throw new CustomException(BAD_REQUEST);
        }

        int nextOrder = groupRepository.countByUser_UserId(userId) + 1;

        RestaurantBookmarkGroup group = groupRepository.save(
                RestaurantBookmarkGroup.builder()
                        .user(user)
                        .name(request.name())
                        .color(request.color())
                        .visibility(request.visibility())
                        .displayOrder(nextOrder)
                        .build()
        );

        return new PostBookmarkGroupResponse(
                group.getId(),
                group.getName(),
                group.getColor(),
                group.getDisplayOrder()
        );
    }

    @Transactional
    public PutBookmarkGroupResponse updateGroup(
            Long userId,
            Long groupId,
            PutBookmarkGroupRequest request
    ) {
        RestaurantBookmarkGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(GROUP_NOT_FOUND));
        // 소유자 검증
        if (!group.getUser().getUserId().equals(userId)) {
            throw new CustomException(PERMISSION_DENIED);
        }

        // 이름 검증
        if (request.name() == null || request.name().isBlank()) {
            throw new CustomException(BAD_REQUEST);
        }

        if (!group.getName().equals(request.name())
                && groupRepository.existsByUser_UserIdAndName(userId, request.name())) {
            throw new CustomException(BAD_REQUEST);
        }

        group.setName(request.name());
        group.setColor(request.color());
        group.setVisibility(request.visibility());

        return new PutBookmarkGroupResponse(
                group.getId(),
                group.getName(),
                group.getColor(),
                group.getVisibility(),
                group.getDisplayOrder()
        );
    }

    @Transactional
    public void deleteGroup(Long userId, Long groupId) {

        RestaurantBookmarkGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(GROUP_NOT_FOUND));

        if (!group.getUser().getUserId().equals(userId)) {
            throw new CustomException(PERMISSION_DENIED);
        }

        List<RestaurantBookmarkGroupItem> items =
                restaurantBookmarkGroupItemRepository.findAllByGroup_Id(groupId);

        for (RestaurantBookmarkGroupItem item : items) {
            Bookmark bookmark = item.getBookmark();

            long groupCount =
                    restaurantBookmarkGroupItemRepository.countByBookmark_Id(bookmark.getId());

            // 이 그룹에만 속한 북마크라면
            if (groupCount == 1) {
                restaurantBookmarkGroupItemRepository.delete(item);

                bookmarkRepository.delete(bookmark);

                Restaurant restaurant = restaurantRepository
                        .findById(bookmark.getTargetId())
                        .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

                restaurant.setBookmarkCount(restaurant.getBookmarkCount() - 1);
                restaurantRepository.save(restaurant);

                restaurantSummaryService.evictSummary(restaurant.getRestaurantId());
            }
        }

        // 아직 남아 있는 이 그룹의 매핑들 삭제
        restaurantBookmarkGroupItemRepository.deleteAllByGroup_Id(groupId);

        // 그룹 삭제
        groupRepository.delete(group);
    }
}
