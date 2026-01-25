package konkuk.corkCharge.domain.corkageStore.repository;

import konkuk.corkCharge.domain.corkageStore.domain.MultiCorkage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MultiCorkageRepository extends JpaRepository<MultiCorkage, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from MultiCorkage mc where mc.corkageStore.corkageStoreId = :corkageStoreId")
    void deleteAllByCorkageStoreId(@Param("corkageStoreId") Long corkageStoreId);
}
