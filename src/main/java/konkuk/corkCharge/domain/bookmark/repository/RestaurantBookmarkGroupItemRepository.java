package konkuk.corkCharge.domain.bookmark.repository;

import konkuk.corkCharge.domain.bookmark.domain.Bookmark;
import konkuk.corkCharge.domain.bookmark.domain.BookmarkTargetType;
import konkuk.corkCharge.domain.bookmark.domain.RestaurantBookmarkGroupItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantBookmarkGroupItemRepository extends JpaRepository<RestaurantBookmarkGroupItem, Long> {
    List<RestaurantBookmarkGroupItem> findAllByBookmark(Bookmark bookmark);
    void deleteAllByBookmark(Bookmark bookmark);
    List<RestaurantBookmarkGroupItem> findAllByGroup_Id(Long groupId);
    long countByBookmark_Id(Long bookmarkId);
    void deleteAllByGroup_Id(Long groupId);
    int countByGroup_Id(Long groupId);
    boolean existsByGroup_IdAndBookmark_TargetTypeAndBookmark_TargetId(
            Long groupId,
            BookmarkTargetType targetType,
            Long targetId
    );
    boolean existsByBookmark_IdAndGroup_Id(Long bookmarkId, Long groupId);

    @Query(value = """
    SELECT
        g.restaurant_bookmark_group_id AS groupId,
        g.name AS name,
        g.color AS color,
        g.visibility AS visibility,
        r.restaurant_id AS restaurantId,
        r.latitude AS latitude,
        r.longitude AS longitude,
    
        cs.corkage_type AS corkageType,
        cs.corkage_price AS corkagePrice,
    
        (
          SELECT MIN(mc.price)
          FROM multi_corkage mc
          WHERE mc.corkage_store_id = cs.corkage_store_id
        ) AS minMultiPrice
    
    FROM restaurant_bookmark_group_item gi
      JOIN restaurant_bookmark_group g
        ON gi.restaurant_bookmark_group_id = g.restaurant_bookmark_group_id
      JOIN bookmark b
        ON gi.bookmark_id = b.bookmark_id
      JOIN restaurant r
        ON b.target_id = r.restaurant_id
      LEFT JOIN corkage_store cs
        ON cs.restaurant_id = r.restaurant_id
    
    WHERE g.user_id = :userId
      AND (:color IS NULL OR g.color = :color)
      AND b.target_type = 'RESTAURANT'
      AND r.has_corkage = 1
      AND ST_Within(r.location, ST_GeomFromText(:wktPolygon, 4326))
      AND ST_X(r.location) != 0
      AND ST_Y(r.location) != 0
    
    ORDER BY g.display_order ASC, gi.created_at DESC, r.restaurant_id DESC
    """, nativeQuery = true)
    List<GroupRestaurantPinProjection> findGroupRestaurantPinsInBoundsByColor(
            @Param("userId") Long userId,
            @Param("wktPolygon") String wktPolygon,
            @Param("color") String color
    );
}