package konkuk.corkCharge.domain.corkageStore.repository;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorkageStoreRepository extends JpaRepository<CorkageStore, Long> {
}
