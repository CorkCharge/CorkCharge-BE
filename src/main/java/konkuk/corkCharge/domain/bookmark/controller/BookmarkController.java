package konkuk.corkCharge.domain.bookmark.controller;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkTargetType;
import konkuk.corkCharge.domain.bookmark.dto.request.PostBookmarkRequest;
import konkuk.corkCharge.domain.bookmark.dto.response.GetSavedRestaurantResponse;
import konkuk.corkCharge.domain.bookmark.service.BookmarkService;
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
    public BaseResponse<Void> createBookmark(@RequestParam Long userId, @RequestBody PostBookmarkRequest request) {
        bookmarkService.createBookmark(userId, request);
        return BaseResponse.ok(null);
    }

    @DeleteMapping("/{bookmarkId}")
    public BaseResponse<Void> deleteBookmark(@RequestParam Long userId, @PathVariable Long bookmarkId){
        bookmarkService.deleteBookmark(userId, bookmarkId);
        return BaseResponse.ok(null);
    }

    @GetMapping("/restaurant")
    public BaseResponse<List<GetSavedRestaurantResponse>> getSavedRestaurants(
            @RequestParam Long userId){
        return BaseResponse.ok(bookmarkService.getSavedRestaurants(userId));
    }
}
