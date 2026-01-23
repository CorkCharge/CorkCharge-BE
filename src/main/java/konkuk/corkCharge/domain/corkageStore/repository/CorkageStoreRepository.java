package konkuk.corkCharge.domain.corkageStore.repository;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorkageStoreRepository extends JpaRepository<CorkageStore, Long> {

    Optional<CorkageStore> findByRestaurant_RestaurantId(Long restaurantId);

    // redis용 bulk 메서드
    @Query("""
    select cs
    from CorkageStore cs
    join fetch cs.restaurant r
    where r.restaurantId in :restaurantIds
""")
    List<CorkageStore> findAllByRestaurantIdIn(@Param("restaurantIds") List<Long> restaurantIds);
}
