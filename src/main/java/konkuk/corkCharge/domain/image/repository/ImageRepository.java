package konkuk.corkCharge.domain.image.repository;

import konkuk.corkCharge.domain.image.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findProfileImageByUser_UserId(Long userId);
}
