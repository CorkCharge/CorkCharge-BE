package konkuk.corkCharge.domain.suggestion.repository;

import konkuk.corkCharge.domain.suggestion.domain.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
}
