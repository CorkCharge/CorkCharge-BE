package konkuk.corkCharge.domain.helpRequest.repository;

import konkuk.corkCharge.domain.helpRequest.domain.HelpRequest;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {
    boolean existsByUserAndRestaurant(User user, Restaurant restaurant);
    Optional<HelpRequest> findByUserAndRestaurant(User user, Restaurant restaurant);
}
