package konkuk.corkCharge.domain.ownerRestaurant.repository;

import konkuk.corkCharge.domain.ownerRestaurant.domain.OwnerRestaurant;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRestaurantRepository extends JpaRepository<OwnerRestaurant, Long> {
    boolean existsByRestaurant(Restaurant restaurant);
}
