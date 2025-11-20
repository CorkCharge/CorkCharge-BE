package konkuk.corkCharge.domain.corkageStore.repository;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorkageStoreRepository extends JpaRepository<CorkageStore, Long> {

    @Query("""
        SELECT cs
        FROM CorkageStore cs
        JOIN FETCH cs.restaurant r
        LEFT JOIN FETCH cs.multiPrices mp
    """)
    List<CorkageStore> findAllForRestaurant();

    Optional<CorkageStore> findByRestaurant_RestaurantId(Long restaurantId);
}
