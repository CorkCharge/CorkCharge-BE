package konkuk.corkCharge.domain.bookmark.controller;

import konkuk.corkCharge.domain.bookmark.dto.request.PostBookmarkRequest;
import konkuk.corkCharge.domain.bookmark.service.BookmarkService;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping
    public BaseResponse<Void> createBookmark(@RequestParam Long userId, @RequestBody PostBookmarkRequest request){
        bookmarkService.createBookmark(userId, request);
        return BaseResponse.ok(null);
    }
}
