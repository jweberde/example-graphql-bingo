package weber.de.example.graphql.bingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import weber.de.example.graphql.bingo.entity.BingoCard;

import java.util.UUID;

@Repository
public interface BingoCardRepository extends JpaRepository<BingoCard, UUID>, BingoCardRepositoryCustom {

}
