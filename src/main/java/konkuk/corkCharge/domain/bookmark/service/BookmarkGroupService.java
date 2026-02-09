package konkuk.corkCharge.domain.bookmark.service;

import konkuk.corkCharge.domain.bookmark.domain.*;
import konkuk.corkCharge.domain.bookmark.dto.request.PostBookmarkGroupRequest;
import konkuk.corkCharge.domain.bookmark.dto.request.PutBookmarkGroupRequest;
import konkuk.corkCharge.domain.bookmark.dto.response.*;
import konkuk.corkCharge.domain.bookmark.repository.BookmarkRepository;
import konkuk.corkCharge.domain.bookmark.repository.RestaurantBookmarkGroupItemRepository;
import konkuk.corkCharge.domain.bookmark.repository.RestaurantBookmarkGroupRepository;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.restaurant.service.RestaurantSummaryService;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
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
    private final ImageRepository imageRepository;
    private final CorkageStoreRepository corkageStoreRepository;


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

        int nextOrder = groupRepository.findMaxDisplayOrderByUserId(userId) + 1;

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

        // 이름 null / blank 검증
        if (request.name() == null || request.name().isBlank()) {
            throw new CustomException(BAD_REQUEST);
        }

        // 기존 이름과 완전히 동일한 경우
        if (group.getName().equals(request.name())) {
            throw new CustomException(GROUP_NAME_SAME_AS_BEFORE);
        }

        // 다른 그룹이 이미 사용 중인 이름인 경우
        if (groupRepository.existsByUser_UserIdAndName(userId, request.name())) {
            throw new CustomException(GROUP_NAME_ALREADY_EXISTS);
        }

        // 정상 업데이트
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

    @Transactional(readOnly = true)
    public GetBookmarkGroupListResponse getGroupList(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<RestaurantBookmarkGroup> groups =
                groupRepository.findAllByUser_UserIdOrderByDisplayOrderAsc(userId);

        List<GetBookmarkGroupListResponse.GroupDto> groupDtos =
                groups.stream()
                        .map(group -> new GetBookmarkGroupListResponse.GroupDto(
                                group.getId(),
                                group.getName(),
                                group.getColor(),
                                group.getVisibility(),
                                restaurantBookmarkGroupItemRepository.countByGroup_Id(group.getId()),
                                group.getCreatedAt(),
                                group.getUpdatedAt()
                        ))
                        .toList();

        return new GetBookmarkGroupListResponse(
                groupDtos.size(),
                groupDtos
        );
    }

    @Transactional(readOnly = true)
    public GetBookmarkGroupDetailResponse getGroupDetail(
            Long userId,
            Long groupId,
            BookmarkGroupSort sort
    ) {
        RestaurantBookmarkGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(GROUP_NOT_FOUND));

        // PRIVATE 접근 권한 체크
        if (group.getVisibility() == BookmarkGroupVisibility.PRIVATE
                && !group.getUser().getUserId().equals(userId)) {
            throw new CustomException(PERMISSION_DENIED);
        }

        List<RestaurantBookmarkGroupItem> items =
                restaurantBookmarkGroupItemRepository.findAllByGroup_Id(groupId);

        BookmarkGroupSort appliedSort =
                (sort == null) ? BookmarkGroupSort.LATEST : sort;

        items = switch (appliedSort) {

            case LATEST -> items.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList();

            case REVIEW_COUNT_DESC -> items.stream()
                    .sorted((a, b) -> {
                        Restaurant ra = restaurantRepository.findById(
                                a.getBookmark().getTargetId()
                        ).orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

                        Restaurant rb = restaurantRepository.findById(
                                b.getBookmark().getTargetId()
                        ).orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

                        return Integer.compare(
                                rb.getReviewCount(),
                                ra.getReviewCount()
                        );
                    })
                    .toList();

            case RATING_DESC -> items.stream()
                    .sorted((a, b) -> {
                        Restaurant ra = restaurantRepository.findById(
                                a.getBookmark().getTargetId()
                        ).orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

                        Restaurant rb = restaurantRepository.findById(
                                b.getBookmark().getTargetId()
                        ).orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

                        return Double.compare(
                                rb.getRating(),
                                ra.getRating()
                        );
                    })
                    .toList();
        };

        // Restaurant 조회 (정렬 이후)
        List<Restaurant> restaurants = items.stream()
                .map(item -> restaurantRepository.findById(
                        item.getBookmark().getTargetId()
                ).orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND)))
                .toList();

        List<GetBookmarkGroupDetailResponse.RestaurantDto> restaurantDtos =
                restaurants.stream()
                        .map(restaurant -> {
                            List<String> imageUrls =
                                    imageRepository.findAllByCategoryAndTypeId(
                                                    ImageCategory.RESTAURANT,
                                                    restaurant.getRestaurantId()
                                            ).stream()
                                            .map(Image::getImageUrl)
                                            .toList();

                            CorkageStore corkageStore = null;
                            if (restaurant.isHasCorkage()) {
                                corkageStore = corkageStoreRepository
                                        .findByRestaurant_RestaurantId(
                                                restaurant.getRestaurantId()
                                        )
                                        .orElse(null);
                            }

                            return new GetBookmarkGroupDetailResponse.RestaurantDto(
                                    restaurant.getRestaurantId(),
                                    restaurant.getName(),
                                    restaurant.getRating() != null ? restaurant.getRating() : 0.0,
                                    restaurant.getReviewCount(),
                                    restaurant.getOpeningHours(),
                                    imageUrls,
                                    corkageStore != null
                                            ? formatCorkagePrice(corkageStore)
                                            : null,
                                    corkageStore != null
                                            ? corkageStore.getEtcContent()
                                            : null
                            );
                        })
                        .toList();

        return new GetBookmarkGroupDetailResponse(
                group.getName(),
                restaurantDtos.size(),
                restaurantDtos
        );
    }

    private String formatCorkagePrice(CorkageStore store) {
        if (store.getCorkagePrice() == null) return null;
        return "병당 " + store.getCorkagePrice() + "원";
    }

    public GetRestaurantBookmarkGroupListResponse getBookmarkGroupsByRestaurant(
            User user,
            Long restaurantId
    ) {
        // 유저의 모든 그룹 조회 (정렬 포함)
        List<RestaurantBookmarkGroup> groups =
                groupRepository.findAllByUser_UserIdOrderByDisplayOrderAsc(
                        user.getUserId()
                );

        // 그룹별 storedFlag + storeCount 계산
        List<GetRestaurantBookmarkGroupItemResponse> groupResponses =
                groups.stream()
                        .map(group -> {
                            boolean storedFlag =
                                    restaurantBookmarkGroupItemRepository
                                            .existsByGroup_IdAndBookmark_TargetTypeAndBookmark_TargetId(
                                                    group.getId(),
                                                    BookmarkTargetType.RESTAURANT,
                                                    restaurantId
                                            );

                            int storeCount =
                                    restaurantBookmarkGroupItemRepository.countByGroup_Id(group.getId());

                            return new GetRestaurantBookmarkGroupItemResponse(
                                    group.getId(),
                                    group.getName(),
                                    group.getColor(),
                                    group.getVisibility(),
                                    storedFlag,
                                    storeCount,
                                    group.getCreatedAt(),
                                    group.getUpdatedAt()
                            );
                        })
                        .toList();

        // 최종 응답
        return new GetRestaurantBookmarkGroupListResponse(
                groupResponses.size(),
                groupResponses
        );
    }
}
