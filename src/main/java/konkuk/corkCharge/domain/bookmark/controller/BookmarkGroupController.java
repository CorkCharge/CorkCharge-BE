package konkuk.corkCharge.domain.bookmark.controller;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkGroupSort;
import konkuk.corkCharge.domain.bookmark.dto.request.PostBookmarkGroupRequest;
import konkuk.corkCharge.domain.bookmark.dto.request.PutBookmarkGroupRequest;
import konkuk.corkCharge.domain.bookmark.dto.response.GetBookmarkGroupDetailResponse;
import konkuk.corkCharge.domain.bookmark.dto.response.GetBookmarkGroupListResponse;
import konkuk.corkCharge.domain.bookmark.dto.response.PostBookmarkGroupResponse;
import konkuk.corkCharge.domain.bookmark.dto.response.PutBookmarkGroupResponse;
import konkuk.corkCharge.domain.bookmark.service.BookmarkGroupService;
import konkuk.corkCharge.global.annotation.LoginUserId;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookmarks/groups")
@RequiredArgsConstructor
public class BookmarkGroupController {

    private final BookmarkGroupService bookmarkGroupService;

    @PostMapping
    public BaseResponse<PostBookmarkGroupResponse> createGroup(
            @LoginUserId Long userId,
            @RequestBody PostBookmarkGroupRequest request
    ) {
        return BaseResponse.ok(
                bookmarkGroupService.createGroup(userId, request)
        );
    }

    @PutMapping("/{groupId}")
    public BaseResponse<PutBookmarkGroupResponse> updateGroup(
            @LoginUserId Long userId,
            @PathVariable Long groupId,
            @RequestBody PutBookmarkGroupRequest request
    ) {
        return BaseResponse.ok(
                bookmarkGroupService.updateGroup(userId, groupId, request)
        );
    }

    @DeleteMapping("/{groupId}")
    public BaseResponse<Void> deleteGroup(
            @LoginUserId Long userId,
            @PathVariable Long groupId
    ) {
        bookmarkGroupService.deleteGroup(userId, groupId);
        return BaseResponse.ok(null);
    }

    @GetMapping("/groups")
    public BaseResponse<GetBookmarkGroupListResponse> getGroupList(
            @LoginUserId Long userId
    ) {
        return BaseResponse.ok(
                bookmarkGroupService.getGroupList(userId)
        );
    }

    @GetMapping("/groups/{groupId}")
    public BaseResponse<GetBookmarkGroupDetailResponse> getGroupDetail(
            @LoginUserId Long userId,
            @PathVariable Long groupId,
            @RequestParam(required = false, defaultValue = "LATEST")
            BookmarkGroupSort sort
    ) {
        return BaseResponse.ok(
                bookmarkGroupService.getGroupDetail(userId, groupId, sort)
        );
    }
}
