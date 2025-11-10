package konkuk.corkCharge.domain.restaurant.repository;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}