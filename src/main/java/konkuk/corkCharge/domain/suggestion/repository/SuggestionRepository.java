package konkuk.corkCharge.domain.suggestion.repository;

import konkuk.corkCharge.domain.suggestion.domain.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    List<Suggestion> findAllByUser_UserId(Long userId);
}
