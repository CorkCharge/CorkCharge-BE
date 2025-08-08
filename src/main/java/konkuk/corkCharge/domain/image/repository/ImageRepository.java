package konkuk.corkCharge.domain.image.repository;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.domain.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findProfileImageByUser_UserId(Long userId);
    Optional<Image> findFirstByReview_ReviewId(Long reviewId);
    List<Image> findAllByRestaurant_RestaurantId(Long restaurantId);
    List<Image> findAllByReview_ReviewId(Long reviewId);
    List<Image> findAllByTip_TipId(Long tipId);
    Optional<Image> findFirstByRestaurant_RestaurantIdAndCategoryAndType(Long restaurantId, ImageCategory imageCategory, ImageType imageType);
}
