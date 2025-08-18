package konkuk.corkCharge.domain.ownerRestaurant.repository;

import konkuk.corkCharge.domain.ownerRestaurant.domain.OwnerRestaurant;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRestaurantRepository extends JpaRepository<OwnerRestaurant, Long> {
    boolean existsByRestaurant(Restaurant restaurant);
    List<OwnerRestaurant> findAllByUser_UserIdAndRestaurant_HasCorkageFalse(Long userId);
}
