package konkuk.corkCharge.domain.restaurant.repository;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}
