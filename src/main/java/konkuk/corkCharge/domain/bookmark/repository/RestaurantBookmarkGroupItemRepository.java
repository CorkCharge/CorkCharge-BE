package konkuk.corkCharge.domain.bookmark.repository;

import konkuk.corkCharge.domain.bookmark.domain.Bookmark;
import konkuk.corkCharge.domain.bookmark.domain.RestaurantBookmarkGroupItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantBookmarkGroupItemRepository extends JpaRepository<RestaurantBookmarkGroupItem, Long> {
    List<RestaurantBookmarkGroupItem> findAllByBookmark(Bookmark bookmark);
    void deleteAllByBookmark(Bookmark bookmark);
}