package konkuk.corkCharge.domain.helpRequest.repository;

import konkuk.corkCharge.domain.helpRequest.domain.HelpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {
}
