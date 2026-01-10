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

    // lat/lon 없을 때(그냥 2주 이내 최신순 전체) : 신규 매장 리스트
    List<Restaurant> findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(LocalDateTime from);

    // lat/lon 있을 때: DB에서 distance(km) 계산해서 함께 반환 : 신규 매장 리스트
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
          AND r.has_corkage = 1
          AND ST_X(r.location) != 0 AND ST_Y(r.location) != 0
        ORDER BY r.created_at DESC
        """, nativeQuery = true)
    List<RestaurantDistanceProjection> findNewRestaurantsWithDistance(
            @Param("from") LocalDateTime from,
            @Param("lat") double lat,
            @Param("lon") double lon
    );

    // 사용자 좌표 없을 때(저장 수 순) : 카테고리별 매장 리스트
    List<Restaurant> findByHasCorkageTrueAndRawCategoryContainingOrderByBookmarkCountDesc(String category);

    // 사용자 좌표 있을 때(저장 수 순) : 카테고리별 매장 리스트
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
    WHERE r.has_corkage = 1
      AND r.raw_category LIKE CONCAT('%', :category, '%')
      AND ST_X(r.location) != 0 AND ST_Y(r.location) != 0
    ORDER BY distanceKm ASC, r.bookmark_count DESC
    """, nativeQuery = true)
    List<RestaurantDistanceProjection> findCategoryRestaurantsWithDistance(
            @Param("category") String category,
            @Param("lat") double lat,
            @Param("lon") double lon
    );

    // 사용자 위치 기준 3Km 이내에 있는 모든 매장 : 가까운 매장 리스트
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
        WHERE r.has_corkage = 1
          AND ST_X(r.location) != 0
          AND ST_Y(r.location) != 0
          AND ST_Distance_Sphere(
                r.location,
                ST_SRID(POINT(:lon, :lat), 4326)
              ) <= :radiusMeters
        ORDER BY distanceKm ASC, r.bookmark_count DESC
        """, nativeQuery = true)
    List<RestaurantDistanceProjection> findNearbyRestaurantsWithinRadius(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") int radiusMeters
    );

    // 6개의 역 위치 기준 2km 이내에 있는 모든 매장 : 추천 매장 리스트
    @Query(value = """
        SELECT
            r.restaurant_id AS restaurantId,
            ROUND(
                LEAST(
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:gangnamLon, :gangnamLat), 4326)),
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:hongdaeLon, :hongdaeLat), 4326)),
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:seongsuLon, :seongsuLat), 4326)),
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:konkukLon, :konkukLat), 4326)),
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:itaewonLon, :itaewonLat), 4326)),
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:yongsanLon, :yongsanLat), 4326))
                ) / 1000,
                1
            ) AS distanceKm
        FROM restaurant r
        WHERE r.has_corkage = 1
          AND ST_X(r.location) != 0
          AND ST_Y(r.location) != 0
          AND (
                ST_Distance_Sphere(r.location, ST_SRID(POINT(:gangnamLon, :gangnamLat), 4326)) <= :radiusMeters
             OR ST_Distance_Sphere(r.location, ST_SRID(POINT(:hongdaeLon, :hongdaeLat), 4326)) <= :radiusMeters
             OR ST_Distance_Sphere(r.location, ST_SRID(POINT(:seongsuLon, :seongsuLat), 4326)) <= :radiusMeters
             OR ST_Distance_Sphere(r.location, ST_SRID(POINT(:konkukLon, :konkukLat), 4326)) <= :radiusMeters
             OR ST_Distance_Sphere(r.location, ST_SRID(POINT(:itaewonLon, :itaewonLat), 4326)) <= :radiusMeters
             OR ST_Distance_Sphere(r.location, ST_SRID(POINT(:yongsanLon, :yongsanLat), 4326)) <= :radiusMeters
          )
        ORDER BY distanceKm ASC, r.bookmark_count DESC
        """, nativeQuery = true)
    List<RestaurantDistanceProjection> findRecommandRestaurantsWithinRadius(
            @Param("radiusMeters") int radiusMeters,
            @Param("gangnamLat") double gangnamLat, @Param("gangnamLon") double gangnamLon,
            @Param("hongdaeLat") double hongdaeLat, @Param("hongdaeLon") double hongdaeLon,
            @Param("seongsuLat") double seongsuLat, @Param("seongsuLon") double seongsuLon,
            @Param("konkukLat") double konkukLat, @Param("konkukLon") double konkukLon,
            @Param("itaewonLat") double itaewonLat, @Param("itaewonLon") double itaewonLon,
            @Param("yongsanLat") double yongsanLat, @Param("yongsanLon") double yongsanLon
    );

    // Home 매장 탭 top5(가까운) 매장
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
        WHERE r.has_corkage = 1
          AND ST_X(r.location) != 0
          AND ST_Y(r.location) != 0
          AND ST_Distance_Sphere(
                r.location,
                ST_SRID(POINT(:lon, :lat), 4326)
              ) <= :radiusMeters
        ORDER BY distanceKm ASC, r.bookmark_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<RestaurantDistanceProjection> findNearbyRestaurantsWithinRadiusLimit(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") int radiusMeters,
            @Param("limit") int limit
    );

    // Home 매장 탭 top5(추천) 매장
    @Query(value = """
        SELECT
            r.restaurant_id AS restaurantId,
            ROUND(
                LEAST(
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:gangnamLon, :gangnamLat), 4326)),
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:hongdaeLon, :hongdaeLat), 4326)),
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:seongsuLon, :seongsuLat), 4326)),
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:konkukLon, :konkukLat), 4326)),
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:itaewonLon, :itaewonLat), 4326)),
                    ST_Distance_Sphere(r.location, ST_SRID(POINT(:yongsanLon, :yongsanLat), 4326))
                ) / 1000,
                1
            ) AS distanceKm
        FROM restaurant r
        WHERE r.has_corkage = 1
          AND ST_X(r.location) != 0
          AND ST_Y(r.location) != 0
          AND (
                ST_Distance_Sphere(r.location, ST_SRID(POINT(:gangnamLon, :gangnamLat), 4326)) <= :radiusMeters
             OR ST_Distance_Sphere(r.location, ST_SRID(POINT(:hongdaeLon, :hongdaeLat), 4326)) <= :radiusMeters
             OR ST_Distance_Sphere(r.location, ST_SRID(POINT(:seongsuLon, :seongsuLat), 4326)) <= :radiusMeters
             OR ST_Distance_Sphere(r.location, ST_SRID(POINT(:konkukLon, :konkukLat), 4326)) <= :radiusMeters
             OR ST_Distance_Sphere(r.location, ST_SRID(POINT(:itaewonLon, :itaewonLat), 4326)) <= :radiusMeters
             OR ST_Distance_Sphere(r.location, ST_SRID(POINT(:yongsanLon, :yongsanLat), 4326)) <= :radiusMeters
          )
        ORDER BY distanceKm ASC, r.bookmark_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<RestaurantDistanceProjection> findRecommandRestaurantsWithinRadiusLimit(
            @Param("radiusMeters") int radiusMeters,
            @Param("gangnamLat") double gangnamLat, @Param("gangnamLon") double gangnamLon,
            @Param("hongdaeLat") double hongdaeLat, @Param("hongdaeLon") double hongdaeLon,
            @Param("seongsuLat") double seongsuLat, @Param("seongsuLon") double seongsuLon,
            @Param("konkukLat") double konkukLat, @Param("konkukLon") double konkukLon,
            @Param("itaewonLat") double itaewonLat, @Param("itaewonLon") double itaewonLon,
            @Param("yongsanLat") double yongsanLat, @Param("yongsanLon") double yongsanLon,
            @Param("limit") int limit
    );

}