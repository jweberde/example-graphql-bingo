package weber.de.example.graphql.bingo.repository.impl;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import weber.de.example.graphql.bingo.entity.BingoCard;
import weber.de.example.graphql.bingo.entity.BingoTerm;
import weber.de.example.graphql.bingo.entity.dao.BingoTermCheckInput;
import weber.de.example.graphql.bingo.publisher.event.BingoCardStatus;
import weber.de.example.graphql.bingo.repository.BingoCardRepositoryCustom;
import weber.de.example.graphql.bingo.util.CardStatusUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;

@Slf4j
public class BingoCardRepositoryCustomImpl implements BingoCardRepositoryCustom {

    @PersistenceContext
    EntityManager em;


    @Override
    @Timed
    public Optional<BingoCardStatus> findCardStatus(UUID cardId, boolean withTerms) {
        log.debug("[BEFORE] Find CardStatus {} withTerms: {}", cardId, withTerms);

        final BingoCard bingoCard = em.find(BingoCard.class, cardId);
        if (bingoCard == null) {
            return Optional.empty();
        }

        final Optional<BingoCardStatus> status = calculateStatus(bingoCard, withTerms);
        log.debug("[AFTER] Find CardStatus {} withTerms: {}", cardId, withTerms);
        return status;
    }

    @Override
    public Map<UUID, Optional<BingoCardStatus>> findAllCardStatus(boolean withTerms, Optional<Instant> createdSince) {
        log.debug("[BEFORE] Find findAllCardStatus with Terms: {} since: {}", withTerms, createdSince);

        final TypedQuery<BingoCard> query = createdSince.map(i -> {
            TypedQuery<BingoCard> q = em.createQuery("FROM BingoCard c WHERE c.createdAt >= :createdSince", BingoCard.class);
            q.setParameter("createdSince", i);
            return q;
        }).orElseGet(() -> em.createQuery("FROM BingoCard c", BingoCard.class));

        final List<BingoCard> resultList = query.getResultList();

        if (resultList.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<UUID, Optional<BingoCardStatus>> statusMap = calculateStatusAsMap(resultList, withTerms);
        log.debug("[AFTER] Find findAllCardStatus with Terms: {} since: {}", withTerms, createdSince);
        return statusMap;
    }

    /**
     * Returns Map of CardIdUUID and BingoCardStatus.
     *
     * @param cards
     * @param withTerms
     * @return
     */
    private Map<UUID, Optional<BingoCardStatus>> calculateStatusAsMap(Collection<BingoCard> cards, boolean withTerms) {
        if (cards.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<UUID, BingoCard> cardIdMap = cards.stream().collect(Collectors.toMap(BingoCard::getId, Function.identity()));

        // NO joins we will use cache for terms.
        final TypedQuery<Tuple> query = em.createQuery("SELECT c.id, c.checked, c.term, c.card.id FROM BingoCheck c WHERE c.card.id IN(:cardIds)", Tuple.class);
        query.setParameter("cardIds", cardIdMap.keySet());
        final List<Tuple> resultListForAll = query.getResultList();

        final Map<UUID, List<BingoTermCheckInput>> groupedByCardId = resultListForAll.stream()
                .collect(Collectors.groupingBy(t -> t.get(3, UUID.class), mapping(BingoCardRepositoryCustomImpl::convert, Collectors.toList())));

        final Map<UUID, Optional<BingoCardStatus>> statusMap = new HashMap<>();
        for (UUID cardId : cardIdMap.keySet()) {
            final List<BingoTermCheckInput> checksPerCardId = groupedByCardId.getOrDefault(cardId, Collections.emptyList());

            if (checksPerCardId.isEmpty()) {
                log.warn("Could not find BingoChecks for CardId: {}", cardId);
                statusMap.put(cardId, Optional.empty());
            } else {
                final BingoCard bingoCard = cardIdMap.get(cardId);
                statusMap.put(cardId, CardStatusUtil.calculateStatus(bingoCard, checksPerCardId, withTerms));
            }
        }
        return statusMap;
    }

    private Optional<BingoCardStatus> calculateStatus(BingoCard bingoCard, boolean withTerms) {
        // NO joins we will use cache for terms.
        final TypedQuery<Tuple> query = em.createQuery("SELECT c.id, c.checked, c.term FROM BingoCheck c WHERE c.card.id = :cardId", Tuple.class);
        query.setParameter("cardId", bingoCard.getId());
        final List<Tuple> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            log.warn("Could not find BingoChecks for CardId: {}", bingoCard.getId());
            return Optional.empty();
        }
        return CardStatusUtil.calculateStatus(bingoCard, resultList.stream().map(BingoCardRepositoryCustomImpl::convert).collect(Collectors.toList()), withTerms);
    }

    private static BingoTermCheckInput convert(Tuple t) {
        return BingoTermCheckInput.of(t.get(0, UUID.class), t.get(1, boolean.class), t.get(2, BingoTerm.class));
    }

}
