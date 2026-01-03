package konkuk.corkCharge.domain.restaurant.repository;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByHasCorkageTrue();

    List<Restaurant> findByNameContaining(String keyword);

    List<Restaurant> findByBookmarkCountGreaterThanEqual(int count);

    List<Restaurant> findByAddressContaining(String address);

    Optional<Restaurant> findFirstByHasCorkageFalseOrderByBookmarkCountDesc();

    List<Restaurant> findByHasCorkageFalseAndBookmarkCountGreaterThanEqual(int count);

    // 공간 인덱스 활용 범위 검색
    @Query(value = """
        SELECT *
        FROM restaurant r
        WHERE r.has_corkage = 1
          AND ST_Within(
                r.location,
                ST_GeomFromText(:wktPolygon, 4326)
              )
          AND ST_X(r.location) != 0 AND ST_Y(r.location) != 0
        """, nativeQuery = true)
    List<Restaurant> findCorkageRestaurantsInBounds(@Param("wktPolygon") String wktPolygon);

    @Query("""
        SELECT r FROM Restaurant r
        WHERE (r.latitude IS NULL OR r.longitude IS NULL OR r.latitude = 0 OR r.longitude = 0)
        """)
    List<Restaurant> findRestaurantsWithoutValidCoordinates();

    // lat/lon 없을 때(그냥 2주 이내 최신순 전체)
    List<Restaurant> findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(LocalDateTime from);

    // lat/lon 있을 때: DB에서 distance(km) 계산해서 함께 반환
    @Query(value = """
        SELECT
            r.restaurant_id AS restaurantId,
            ROUND(
                ST_Distance_Sphere(
                    r.location,
                    ST_SRID(POINT(:lon, :lat), 4326)
                ) / 1000,
                1
            ) AS distanceKm
        FROM restaurant r
        WHERE r.created_at >= :from
          AND ST_X(r.location) != 0 AND ST_Y(r.location) != 0
        ORDER BY r.created_at DESC
        """, nativeQuery = true)
    List<NewRestaurantDistanceProjection> findNewRestaurantsWithDistance(
            @Param("from") LocalDateTime from,
            @Param("lat") double lat,
            @Param("lon") double lon
    );
}