package konkuk.corkCharge.domain.corkageStore.repository;

import konkuk.corkCharge.domain.corkageStore.domain.MultiCorkage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MultiCorkageRepository extends JpaRepository<MultiCorkage, Long> {
}
