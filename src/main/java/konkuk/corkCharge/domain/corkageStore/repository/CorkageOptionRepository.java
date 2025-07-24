package konkuk.corkCharge.domain.corkageStore.repository;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorkageOptionRepository extends JpaRepository<CorkageOption, Long> {
}
