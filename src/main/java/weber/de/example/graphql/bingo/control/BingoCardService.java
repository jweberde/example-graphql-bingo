package weber.de.example.graphql.bingo.control;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import weber.de.example.graphql.bingo.entity.BingoCard;
import weber.de.example.graphql.bingo.entity.BingoCheck;
import weber.de.example.graphql.bingo.entity.BingoTerm;
import weber.de.example.graphql.bingo.entity.dao.BingoPool;
import weber.de.example.graphql.bingo.publisher.BingoCardUpdatePublisher;
import weber.de.example.graphql.bingo.publisher.event.BingoCardStatus;
import weber.de.example.graphql.bingo.repository.BingoCardRepository;
import weber.de.example.graphql.bingo.repository.BingoCheckRepository;
import weber.de.example.graphql.bingo.repository.BingoTermRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BingoCardService {


    @Autowired
    BingoCardUpdatePublisher publisher;

    @Autowired
    BingoCardRepository bingoCardRepository;

    @Autowired
    BingoCheckRepository bingoCheckRepository;

    @Autowired
    BingoPoolService bingoPoolService;

    @Value("${app.card-size}")
    int cardSize;

    @Autowired
    BingoTermRepository bingoTermRepository;

    @Transactional
    public BingoCard createCard(String owner) {
        log.info("Create new BingoCard {}", owner);
        final BingoPool bingoTermPool = this.bingoPoolService.getBingoTermPool();

        BingoCard card = new BingoCard();
        card.setOwner(owner);

        final List<UUID> termIds = bingoTermPool.pullRandom(cardSize);
        final List<BingoCheck> checks = termIds.stream()
                .map(termId -> {
                    BingoCheck check = new BingoCheck();
                    check.setCard(card);
                    check.setChecked(false);
                    check.setTerm(bingoTermRepository.getOne(termId));
                    return check;
                }).collect(Collectors.toList());
        card.setChecks(checks);
        final BingoCard savedCard = this.bingoCardRepository.save(card);
        this.publisher.publish(savedCard);
        this.publisher.publish(this.getCardStatus(savedCard.getId()));
        return savedCard;
    }

    @Transactional
    public BingoTerm addCheck(UUID cardId, UUID checkId, boolean checked) {
        final BingoCheck bingoCheck = this.bingoCheckRepository.findByCardIdAndId(cardId, checkId)
                .orElseThrow(() -> new IllegalArgumentException("No Check with Card " + cardId + " and " + checkId + " exists."));
        bingoCheck.setChecked(checked);
        final BingoCheck savedCheck = bingoCheckRepository.saveAndFlush(bingoCheck);
        log.debug("Update cardId {} with checkId {} to state {}", cardId, checkId, checked);
        final BingoCardStatus cardStatus = this.getCardStatus(cardId);
        this.publisher.publish(cardStatus);
        return bingoTermRepository.findById(savedCheck.getTermId()).orElseThrow();
    }

    @PreAuthorize("isAuthenticated()")
    public int publishStatus(Optional<Instant> since) {
        final List<BingoCardStatus> allCardStatus = this.getAllCardStatus(since);
        allCardStatus.forEach(cardStatus -> this.publisher.publish(cardStatus));
        return allCardStatus.size();
    }


    @Transactional(readOnly = true)
    public BingoCardStatus getCardStatus(UUID cardId) {
        return bingoCardRepository.findCardStatus(cardId, true)
                .orElseThrow(() -> new IllegalArgumentException("No Card with " + cardId));
    }

    @PreAuthorize("isAuthenticated()")
    public List<BingoCardStatus> getAllCardStatus(Optional<Instant> since) {
        final Map<UUID, Optional<BingoCardStatus>> allCardStatus = bingoCardRepository.findAllCardStatus(true, since);
        return allCardStatus
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(BingoCard.BY_PROGRESS)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    public boolean restartBingo() {
        // Delete All
        bingoCardRepository.deleteAll();

        this.publisher.publish(BingoRestartEvent.of());

        return true;
    }
}
