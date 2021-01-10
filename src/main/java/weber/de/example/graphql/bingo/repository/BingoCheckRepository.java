package weber.de.example.graphql.bingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import weber.de.example.graphql.bingo.entity.BingoCheck;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BingoCheckRepository extends JpaRepository<BingoCheck, UUID> {
    List<BingoCheck> findByCardId(UUID id);

    Optional<BingoCheck> findByCardIdAndId(UUID cardId, UUID checkId);
}
