package konkuk.corkCharge.domain.bookmark.repository;

import konkuk.corkCharge.domain.bookmark.domain.Bookmark;
import konkuk.corkCharge.domain.bookmark.domain.BookmarkTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUser_UserIdAndTargetType(Long userId, BookmarkTargetType bookmarkTargetType);

    long countByTargetTypeAndTargetId(BookmarkTargetType type, Long targetId);

    Optional<Bookmark> findByUser_UserIdAndTargetTypeAndTargetId(
            Long userId,
            BookmarkTargetType targetType,
            Long targetId
    );

    @Query("""
        select b
          from Bookmark b
         where b.user.userId = :userId
           and b.targetType = :targetType
           and b.targetId in :targetIds
    """)
    List<Bookmark> findAllByUserIdAndTargetTypeAndTargetIdIn(
            @Param("userId") Long userId,
            @Param("targetType") BookmarkTargetType targetType,
            @Param("targetIds") List<Long> targetIds
    );

}
