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
            ST_SRID(ST_MakeEnvelope(:lonMin, :latMin, :lonMax, :latMax), 4326)
          ) = 1
    """, nativeQuery = true)
    List<Restaurant> findCorkageRestaurantsInBounds(
            @Param("latMin") double latMin,
            @Param("latMax") double latMax,
            @Param("lonMin") double lonMin,
            @Param("lonMax") double lonMax
    );

    // 좌표 정보가 비어 있는 레스토랑 찾는 함수
    List<Restaurant> findByLocationIsNull();
}