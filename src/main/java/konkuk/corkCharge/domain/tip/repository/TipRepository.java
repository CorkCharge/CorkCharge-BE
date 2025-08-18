package konkuk.corkCharge.domain.tip.repository;

import konkuk.corkCharge.domain.tip.domain.Tip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipRepository extends JpaRepository<Tip,Long> {
}
