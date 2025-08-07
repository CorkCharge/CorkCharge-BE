package konkuk.corkCharge.domain.bookmark.repository;

import konkuk.corkCharge.domain.bookmark.domain.Bookmark;
import konkuk.corkCharge.domain.bookmark.domain.BookmarkTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUser_UserIdAndTargetType(Long userId, BookmarkTargetType bookmarkTargetType);

    long countByTargetTypeAndTargetId(BookmarkTargetType type, Long targetId);
}
