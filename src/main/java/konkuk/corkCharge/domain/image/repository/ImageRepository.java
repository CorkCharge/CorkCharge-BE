package konkuk.corkCharge.domain.image.repository;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.domain.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByCategoryAndTypeId(ImageCategory category, Long typeId);

    Optional<Image> findFirstByCategoryAndTypeIdOrderByCreatedAtAsc(ImageCategory category, Long typeId);

    Optional<Image> findFirstByCategoryAndTypeIdAndType(ImageCategory category, Long typeId, ImageType type);

    Optional<Image> findFirstByCategoryAndTypeId(ImageCategory category, Long typeId);

    void deleteByCategoryAndTypeId(ImageCategory category, Long typeId);

    @Query("""
    select i.imageUrl
      from Image i
     where i.category = :category and i.typeId = :typeId
     order by i.createdAt asc
""")
    List<String> findUrlsByCategoryAndTypeId(@Param("category") ImageCategory category,
                                             @Param("typeId") Long typeId);
}
