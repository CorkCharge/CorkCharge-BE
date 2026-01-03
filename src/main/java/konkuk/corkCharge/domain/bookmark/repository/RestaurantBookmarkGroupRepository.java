package konkuk.corkCharge.domain.bookmark.repository;

import konkuk.corkCharge.domain.bookmark.domain.RestaurantBookmarkGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantBookmarkGroupRepository extends JpaRepository<RestaurantBookmarkGroup, Long> {
    List<RestaurantBookmarkGroup> findAllByIdIn(List<Long> ids);
}
