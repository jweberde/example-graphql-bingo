package weber.de.example.graphql.bingo.repository;

import weber.de.example.graphql.bingo.publisher.event.BingoCardStatus;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface BingoCardRepositoryCustom {

    Optional<BingoCardStatus> findCardStatus(UUID cardId, boolean withTerms);

    Map<UUID, Optional<BingoCardStatus>> findAllCardStatus(boolean withTerms, Optional<Instant> createdSince);
}
