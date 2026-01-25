package konkuk.corkCharge.domain.ownerRestaurant.repository;

import konkuk.corkCharge.domain.ownerRestaurant.domain.OwnerRestaurant;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRestaurantRepository extends JpaRepository<OwnerRestaurant, Long> {
    boolean existsByRestaurant(Restaurant restaurant);

    @Query("""
        select orr.restaurant.restaurantId
          from OwnerRestaurant orr
         where orr.user.userId = :userId
         order by orr.createdAt desc, orr.id desc
    """)
    List<Long> findRestaurantIdsByUserId(@Param("userId") Long userId);

    boolean existsByUser_UserIdAndRestaurant_RestaurantId(Long userId, Long restaurantId);

}
