package konkuk.corkCharge.domain.bookmark.repository;

import konkuk.corkCharge.domain.bookmark.domain.RestaurantBookmarkGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RestaurantBookmarkGroupRepository extends JpaRepository<RestaurantBookmarkGroup, Long> {
    List<RestaurantBookmarkGroup> findAllByIdIn(List<Long> ids);
    boolean existsByUser_UserIdAndName(Long userId, String name);
    int countByUser_UserId(Long userId);
    List<RestaurantBookmarkGroup> findAllByUser_UserIdOrderByDisplayOrderAsc(Long userId);
    @Query("""
    select coalesce(max(g.displayOrder), 0)
    from RestaurantBookmarkGroup g
    where g.user.userId = :userId
    """)
    int findMaxDisplayOrderByUserId(Long userId);
}
