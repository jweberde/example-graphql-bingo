package weber.de.example.graphql.bingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import weber.de.example.graphql.bingo.entity.BingoTerm;

import java.util.UUID;

@Repository
public interface BingoTermRepository extends JpaRepository<BingoTerm, UUID> {
}
