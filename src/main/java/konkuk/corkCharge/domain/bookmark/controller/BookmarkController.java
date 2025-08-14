package konkuk.corkCharge.domain.bookmark.controller;

import konkuk.corkCharge.domain.bookmark.dto.request.PostBookmarkRequest;
import konkuk.corkCharge.domain.bookmark.dto.response.GetSavedRestaurantResponse;
import konkuk.corkCharge.domain.bookmark.dto.response.GetSavedReviewResponse;
import konkuk.corkCharge.domain.bookmark.dto.response.GetSavedTipResponse;
import konkuk.corkCharge.domain.bookmark.service.BookmarkService;
import konkuk.corkCharge.global.annotation.LoginUserId;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping
    public BaseResponse<Void> createBookmark(
            @LoginUserId Long userId,
            @RequestBody PostBookmarkRequest request
    ) {
        bookmarkService.createBookmark(userId, request);
        return BaseResponse.ok(null);
    }

    @DeleteMapping("/{bookmarkId}")
    public BaseResponse<Void> deleteBookmark(
            @LoginUserId Long userId,
            @PathVariable(name = "bookmarkId") Long bookmarkId
    ){
        bookmarkService.deleteBookmark(userId, bookmarkId);
        return BaseResponse.ok(null);
    }

    @GetMapping("/restaurant")
    public BaseResponse<List<GetSavedRestaurantResponse>> getSavedRestaurants(
            @LoginUserId Long userId
    ){
        return BaseResponse.ok(bookmarkService.getSavedRestaurants(userId));
    }

    @GetMapping("/review")
    public BaseResponse<List<GetSavedReviewResponse>> getSavedReviews(
            @LoginUserId Long userId
    ){
        return BaseResponse.ok(bookmarkService.getSavedReviews(userId));
    }

    @GetMapping("/tip")
    public BaseResponse<List<GetSavedTipResponse>> getSavedTips(
            @LoginUserId Long userId
    ){
        return BaseResponse.ok(bookmarkService.getSavedTips(userId));
    }
}
