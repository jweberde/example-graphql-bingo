package weber.de.example.graphql.bingo.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import weber.de.example.graphql.bingo.entity.BingoCard;
import weber.de.example.graphql.bingo.entity.BingoCheck;
import weber.de.example.graphql.bingo.entity.BingoTerm;
import weber.de.example.graphql.bingo.entity.dao.BingoTermInput;
import weber.de.example.graphql.bingo.publisher.event.BingoCardStatus;
import weber.de.example.graphql.bingo.repository.BingoCardRepository;
import weber.de.example.graphql.bingo.repository.BingoCheckRepository;
import weber.de.example.graphql.bingo.repository.BingoTermRepository;
import weber.de.example.graphql.bingo.test.TestDataUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test-database")
@ExtendWith(SpringExtension.class)
class BingoCardServiceTestIT {

    @Autowired
    BingoTermService termService;

    @Autowired
    BingoCardService cardService;

    @Autowired
    BingoTermRepository termRepository;

    @Autowired
    BingoCheckRepository checkRepository;

    @Autowired
    BingoCardRepository cardRepository;

    @Value("${app.card-size}")
    int cardSize;

    @Test
    void create() {
        final List<BingoTermInput> list = TestDataUtil.generateTerms();
        termService.createTerms(list, false);
        assertEquals(list.size(), termRepository.count());

        final String owner = this.getClass().getName() + "-create";
        final BingoCard card = cardService.createCard(owner);

        assertNotNull(card);
        assertNotNull(card.getId());
        assertNotNull(card.getCreatedAt());
        assertNotNull(card.getUpdatedAt());
        assertEquals(owner, card.getOwner());
        assertEquals(cardSize, card.getChecks().size());

        final BingoCard refreshedCard = cardRepository.findById(card.getId()).orElseThrow();

        assertNotNull(refreshedCard);
        assertNotNull(refreshedCard.getId());
        assertNotNull(refreshedCard.getCreatedAt());
        assertNotNull(refreshedCard.getUpdatedAt());
        assertEquals(owner, refreshedCard.getOwner());

        List<BingoCheck> checks = checkRepository.findByCardId(refreshedCard.getId());
        assertEquals(cardSize, checks.size());
        assertTrue(checks.stream().filter(c -> c.isChecked()).findAny().isEmpty());
    }

    @Test
    void updateCheck() {
        final List<BingoTermInput> list = TestDataUtil.generateTerms();
        termService.createTerms(list, false);
        assertEquals(list.size(), termRepository.count());

        final String owner = this.getClass().getName() + "-create";
        final BingoCard card = cardService.createCard(owner);
        final List<BingoCheck> checks = card.getChecks();

        final BingoCheck firstCheck = checks.get(0);
        assertFalse(firstCheck.isChecked());

        cardService.addCheck(card.getId(), firstCheck.getId(), true);

        final BingoCheck refreshedCheck = checkRepository.findById(firstCheck.getId()).orElseThrow();
        assertTrue(refreshedCheck.isChecked());
        assertEquals(card.getId(), refreshedCheck.getCardId());
    }

    @Test
    void checkStatus() {
        final List<BingoTermInput> list = TestDataUtil.generateTerms();
        termService.createTerms(list, false);
        assertEquals(list.size(), termRepository.count());

        final String owner = this.getClass().getName() + "-create";
        final BingoCard card = cardService.createCard(owner);
        final List<BingoCheck> checks = card.getChecks();

        final List<String> assigendTerms = checks.stream()
                .map(BingoCheck::getTerm)
                .map(BingoTerm::getValue)
                .collect(Collectors.toList());

        final BingoCheck firstCheck = checks.get(0);
        assertFalse(firstCheck.isChecked());

        final BingoCardStatus cardStatus1 = cardService.getCardStatus(card.getId());
        assertEquals(card.getId(), cardStatus1.getCardId());
        assertEquals(owner, cardStatus1.getOwner());
        assertEquals(cardStatus1.getCheckedCount(), 0);
        assertEquals(cardStatus1.getMissingCount(), assigendTerms.size());
        assertTrue(cardStatus1.getCheckedTerms().isEmpty());
        assertTrue(cardStatus1.getMissingTerms().containsAll(assigendTerms));

        final BingoTerm addedTerm1 = cardService.addCheck(card.getId(), firstCheck.getId(), true);

        final BingoCardStatus cardStatus2 = cardService.getCardStatus(card.getId());
        assertEquals(card.getId(), cardStatus2.getCardId());
        assertEquals(owner, cardStatus2.getOwner());
        assertEquals(cardStatus2.getCheckedCount(), 1);
        assertEquals(cardStatus2.getMissingCount(), assigendTerms.size() - 1);
        assertTrue(cardStatus2.getCheckedTerms().contains(addedTerm1.getValue()));
        assertEquals(cardStatus2.getMissingTerms().size(), assigendTerms.size() - 1);
    }


    @BeforeEach
    public void beforeEach() {
        this.checkRepository.deleteAll();
        this.checkRepository.flush();
        this.cardRepository.deleteAll();
        this.cardRepository.flush();
        this.termRepository.deleteAll();
        this.termRepository.flush();
    }
}