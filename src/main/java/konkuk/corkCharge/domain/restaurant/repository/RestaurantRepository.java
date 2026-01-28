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

    List<Restaurant> findByBookmarkCountGreaterThanEqual(int count);

    Optional<Restaurant> findFirstByHasCorkageFalseOrderByBookmarkCountDesc();

    List<Restaurant> findByHasCorkageFalseAndBookmarkCountGreaterThanEqual(int count);

    @Query("""
    select r.restaurantId
      from Restaurant r
     where lower(r.name) like lower(concat('%', :keyword, '%'))
        or lower(r.address) like lower(concat('%', :keyword, '%'))
""")
    List<Long> findIdsByNameOrAddressContains(@Param("keyword") String keyword);

    @Query("""
        SELECT r FROM Restaurant r
        WHERE (r.latitude IS NULL OR r.longitude IS NULL OR r.latitude = 0 OR r.longitude = 0)
        """)
    List<Restaurant> findRestaurantsWithoutValidCoordinates();

    // lat/lon 없을 때(그냥 2주 이내 최신순 콜키지 매장)
    @Query(value = """
        SELECT r.*
        FROM restaurant r
        JOIN corkage_store cs
          ON cs.restaurant_id = r.restaurant_id
        WHERE cs.created_at >= :from
          AND r.has_corkage = 1
        
          AND (:sido IS NULL OR :sido = '' OR r.address LIKE CONCAT('%', :sido, '%'))
          AND (:sigungu IS NULL OR :sigungu = '' OR r.address LIKE CONCAT('%', :sigungu, '%'))
          AND (:dongRegex IS NULL OR :dongRegex = '' OR r.address REGEXP :dongRegex)
        
        ORDER BY cs.created_at DESC, r.restaurant_id DESC
    """, nativeQuery = true)
    List<Restaurant> findNewCorkageRestaurantsWithRegion(
            @Param("from") LocalDateTime from,
            @Param("sido") String sido,
            @Param("sigungu") String sigungu,
            @Param("dongRegex") String dongRegex
    );

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
          JOIN corkage_store cs
            ON cs.restaurant_id = r.restaurant_id
        WHERE cs.created_at >= :from
          AND r.has_corkage = 1
          AND ST_X(r.location) != 0 AND ST_Y(r.location) != 0
        
          -- 지역 필터(옵션)
          AND (:sido IS NULL OR :sido = '' OR r.address LIKE CONCAT('%', :sido, '%'))
          AND (:sigungu IS NULL OR :sigungu = '' OR r.address LIKE CONCAT('%', :sigungu, '%'))
          AND (:dongRegex IS NULL OR :dongRegex = '' OR r.address REGEXP :dongRegex)
        
        ORDER BY cs.created_at DESC, r.restaurant_id DESC
    """, nativeQuery = true)
    List<RestaurantDistanceProjection> findNewRestaurantsWithDistanceAndRegion(
            @Param("from") LocalDateTime from,
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("sido") String sido,
            @Param("sigungu") String sigungu,
            @Param("dongRegex") String dongRegex
    );

    // 사용자 좌표 없을 때(저장 수 순) : 카테고리별 매장 리스트
    @Query(value = """
        SELECT *
        FROM restaurant r
        WHERE r.has_corkage = 1
          AND r.raw_category LIKE CONCAT('%', :category, '%')
        
          -- 시/도
          AND (:sido IS NULL OR :sido = '' OR r.address LIKE CONCAT('%', :sido, '%'))
        
          -- 시/군/구
          AND (:sigungu IS NULL OR :sigungu = '' OR r.address LIKE CONCAT('%', :sigungu, '%'))
        
          -- 동 (단일 dong)
          AND (:dong IS NULL OR :dong = '' OR r.address LIKE CONCAT('%', :dong, '%'))
        
        ORDER BY r.bookmark_count DESC
    """, nativeQuery = true)
    List<Restaurant> findCategoryRestaurantsWithoutLocationWithRegion(
            @Param("category") String category,
            @Param("sido") String sido,
            @Param("sigungu") String sigungu,
            @Param("dong") String dong
    );

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
        
          -- ✅ 지역 필터 (null/빈값이면 무시)
          AND (:sido IS NULL OR :sido = '' OR r.address LIKE CONCAT('%', :sido, '%'))
          AND (:sigungu IS NULL OR :sigungu = '' OR r.address LIKE CONCAT('%', :sigungu, '%'))
          AND (:dongRegex IS NULL OR :dongRegex = '' OR r.address REGEXP :dongRegex)
        
        ORDER BY distanceKm ASC, r.bookmark_count DESC
    """, nativeQuery = true)
    List<RestaurantDistanceProjection> findCategoryRestaurantsWithDistanceAndRegion(
            @Param("category") String category,
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("sido") String sido,
            @Param("sigungu") String sigungu,
            @Param("dongRegex") String dongRegex
    );

    // 사용자 위치 기준 3Km 이내에 있는 모든 매장 : 가까운 매장 리스트
    @Query(value = """
    WITH dist AS (
      SELECT
        r.restaurant_id AS restaurantId,
        r.bookmark_count AS bookmarkCount,
        ST_Distance_Sphere(
          r.location,
          ST_SRID(POINT(:lon, :lat), 4326)
        ) AS distanceMeters
      FROM restaurant r
      WHERE r.has_corkage = 1
        AND ST_X(r.location) != 0
        AND ST_Y(r.location) != 0
    )
    SELECT
      restaurantId AS restaurantId,
      ROUND(distanceMeters / 1000, 1) AS distanceKm
    FROM dist
    WHERE distanceMeters <= :radiusMeters
    ORDER BY distanceMeters ASC, bookmarkCount DESC
    """, nativeQuery = true)
    List<RestaurantDistanceProjection> findNearbyRestaurantsWithinRadius(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") int radiusMeters
    );

    // 6개의 역 위치 기준 2km 이내에 있는 모든 매장 : 추천 매장 리스트
    @Query(value = """
    WITH dist AS (
      SELECT
        r.restaurant_id AS restaurantId,
        r.bookmark_count AS bookmarkCount,
        LEAST(
          ST_Distance_Sphere(r.location, ST_SRID(POINT(:gangnamLon, :gangnamLat), 4326)),
          ST_Distance_Sphere(r.location, ST_SRID(POINT(:hongdaeLon, :hongdaeLat), 4326)),
          ST_Distance_Sphere(r.location, ST_SRID(POINT(:seongsuLon, :seongsuLat), 4326)),
          ST_Distance_Sphere(r.location, ST_SRID(POINT(:konkukLon, :konkukLat), 4326)),
          ST_Distance_Sphere(r.location, ST_SRID(POINT(:itaewonLon, :itaewonLat), 4326)),
          ST_Distance_Sphere(r.location, ST_SRID(POINT(:yongsanLon, :yongsanLat), 4326))
        ) AS minDistanceMeters
      FROM restaurant r
      WHERE r.has_corkage = 1
        AND ST_X(r.location) != 0
        AND ST_Y(r.location) != 0
    )
    SELECT
      restaurantId,
      ROUND(minDistanceMeters / 1000, 1) AS distanceKm
    FROM dist
    WHERE minDistanceMeters <= :radiusMeters
    ORDER BY distanceKm ASC, bookmarkCount DESC
""", nativeQuery = true)
    List<RestaurantDistanceProjection> findRecommendRestaurantsWithinRadius(
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
    WITH dist AS (
      SELECT
        r.restaurant_id AS restaurantId,
        r.bookmark_count AS bookmarkCount,
        ST_Distance_Sphere(
          r.location,
          ST_SRID(POINT(:lon, :lat), 4326)
        ) AS distanceMeters
      FROM restaurant r
      WHERE r.has_corkage = 1
        AND ST_X(r.location) != 0
        AND ST_Y(r.location) != 0
    )
    SELECT restaurantId
    FROM dist
    WHERE distanceMeters <= :radiusMeters
    ORDER BY distanceMeters ASC, bookmarkCount DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Long> findNearbyRestaurantIdsWithinRadiusLimit(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") int radiusMeters,
            @Param("limit") int limit
    );

    // Home 매장 탭 top5(추천) 매장
    @Query(value = """
    WITH distance_calc AS (
        SELECT
            r.restaurant_id,
            r.bookmark_count,
            LEAST(
                ST_Distance_Sphere(r.location, ST_SRID(POINT(:gangnamLon, :gangnamLat), 4326)),
                ST_Distance_Sphere(r.location, ST_SRID(POINT(:hongdaeLon, :hongdaeLat), 4326)),
                ST_Distance_Sphere(r.location, ST_SRID(POINT(:seongsuLon, :seongsuLat), 4326)),
                ST_Distance_Sphere(r.location, ST_SRID(POINT(:konkukLon, :konkukLat), 4326)),
                ST_Distance_Sphere(r.location, ST_SRID(POINT(:itaewonLon, :itaewonLat), 4326)),
                ST_Distance_Sphere(r.location, ST_SRID(POINT(:yongsanLon, :yongsanLat), 4326))
            ) AS min_distance
        FROM restaurant r
        WHERE r.has_corkage = 1
          AND ST_X(r.location) != 0
          AND ST_Y(r.location) != 0
    )
    SELECT
        restaurant_id
    FROM distance_calc
    WHERE min_distance <= :radiusMeters
    ORDER BY min_distance ASC, bookmark_count DESC
    LIMIT :limit;
    """, nativeQuery = true)
    List<Long> findRecommendRestaurantIdsWithinRadiusLimit(
            @Param("radiusMeters") int radiusMeters,
            @Param("gangnamLat") double gangnamLat, @Param("gangnamLon") double gangnamLon,
            @Param("hongdaeLat") double hongdaeLat, @Param("hongdaeLon") double hongdaeLon,
            @Param("seongsuLat") double seongsuLat, @Param("seongsuLon") double seongsuLon,
            @Param("konkukLat") double konkukLat, @Param("konkukLon") double konkukLon,
            @Param("itaewonLat") double itaewonLat, @Param("itaewonLon") double itaewonLon,
            @Param("yongsanLat") double yongsanLat, @Param("yongsanLon") double yongsanLon,
            @Param("limit") int limit
    );

    // 콜키지맵 조회(필터링: 검색어 + 지역 + 콜키지)
    @Query(value = """
    SELECT
        r.restaurant_id AS restaurantId,
        r.name AS restaurantName,
        r.address AS address,
        r.latitude AS latitude,
        r.longitude AS longitude,
    
        cs.corkage_type AS corkageType,
        cs.corkage_price AS corkagePrice,
        mm.minPrice AS minMultiPrice
    
    FROM restaurant r
    JOIN corkage_store cs
      ON cs.restaurant_id = r.restaurant_id
    
    LEFT JOIN (
        SELECT mc.corkage_store_id, MIN(mc.price) AS minPrice
        FROM multi_corkage mc
        GROUP BY mc.corkage_store_id
    ) mm
      ON mm.corkage_store_id = cs.corkage_store_id
    
    WHERE r.has_corkage = 1
      AND ST_Within(r.location, ST_GeomFromText(:wktPolygon, 4326))
      AND ST_X(r.location) != 0
      AND ST_Y(r.location) != 0
    
      -- 검색어
      AND (:keyword IS NULL OR :keyword = '' OR r.name LIKE CONCAT('%', :keyword, '%'))
    
      -- 지역(시/도, 시/군/구)
      AND (:sido IS NULL OR :sido = '' OR r.address LIKE CONCAT('%', :sido, '%'))
      AND (:sigungu IS NULL OR :sigungu = '' OR r.address LIKE CONCAT('%', :sigungu, '%'))
      -- 동 리스트(여러 개 OR) => REGEXP로 처리
      AND (:dongRegex IS NULL OR :dongRegex = '' OR r.address REGEXP :dongRegex)
    
      -- 평점 필터
      AND (:minScore IS NULL OR r.rating >= :minScore)
      AND (:maxScore IS NULL OR r.rating <= :maxScore)
    
      -- 콜키지 타입 필터
      AND (:useTypeFilter = 0 OR cs.corkage_type IN (:corkageTypes))
    
      -- 옵션 비트마스크(0이면 필터 없음)
      AND (:optionMask = 0 OR (cs.option_bits & :optionMask) != 0)
    
      -- 타입별 가격 필터
      AND (
            cs.corkage_type IN ('FREE', 'MULTIPLE')
            OR (cs.corkage_type = 'PER_BOTTLE' AND cs.corkage_price BETWEEN :minBottlePrice AND :maxBottlePrice)
            OR (cs.corkage_type = 'PER_PERSON' AND cs.corkage_price BETWEEN :minPersonPrice AND :maxPersonPrice)
            OR (cs.corkage_type = 'PER_TABLE'  AND cs.corkage_price BETWEEN :minTablePrice  AND :maxTablePrice)
      )
    
    ORDER BY r.restaurant_id DESC
    """, nativeQuery = true)
    List<MapPinProjection> findMapPins(
            @Param("wktPolygon") String wktPolygon,
            @Param("keyword") String keyword,
            @Param("sido") String sido,
            @Param("sigungu") String sigungu,
            @Param("dongRegex") String dongRegex,

            @Param("minScore") Double minScore,
            @Param("maxScore") Double maxScore,

            @Param("useTypeFilter") int useTypeFilter,
            @Param("corkageTypes") List<String> corkageTypes,

            @Param("optionMask") int optionMask,

            @Param("minBottlePrice") int minBottlePrice,
            @Param("maxBottlePrice") int maxBottlePrice,
            @Param("minPersonPrice") int minPersonPrice,
            @Param("maxPersonPrice") int maxPersonPrice,
            @Param("minTablePrice") int minTablePrice,
            @Param("maxTablePrice") int maxTablePrice
    );

}