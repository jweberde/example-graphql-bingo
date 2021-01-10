package weber.de.example.graphql.bingo.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import weber.de.example.graphql.bingo.entity.BingoCard;
import weber.de.example.graphql.bingo.entity.BingoCheck;
import weber.de.example.graphql.bingo.entity.BingoTerm;
import weber.de.example.graphql.bingo.entity.dao.BingoTermInput;
import weber.de.example.graphql.bingo.repository.BingoCardRepository;
import weber.de.example.graphql.bingo.repository.BingoCheckRepository;
import weber.de.example.graphql.bingo.repository.BingoTermRepository;
import weber.de.example.graphql.bingo.test.TestDataUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test-database")
@ExtendWith(SpringExtension.class)
class BingoTermServiceTestIT {

    @Autowired
    BingoTermService termService;

    @Autowired
    BingoCardService cardService;

    @Autowired
    BingoTermRepository termRepository;

    @Autowired
    BingoCardRepository cardRepository;

    @Autowired
    BingoCheckRepository checkRepository;

    @Test
    void create() {
        final List<BingoTermInput> list = TestDataUtil.generateTerms();

        final List<BingoTerm> terms = termService.createTerms(list, false);

        assertEquals(list.size(), termRepository.count());

        for (BingoTerm t : terms) {
            assertNotNull(t);
            assertNotNull(t.getId());
            assertNotNull(t.getUpdatedAt());
            assertNotNull(t.getCreatedAt());
            assertNotNull(t.getCategory());
            assertNotNull(t.getValue());
        }
        final List<BingoTerm> all = termRepository.findAll();
        assertEquals(list.size(), all.size());

        for (BingoTerm t : all) {
            assertNotNull(t);
            assertNotNull(t.getId());
            assertNotNull(t.getUpdatedAt());
            assertNotNull(t.getCreatedAt());
            assertNotNull(t.getCategory());
            assertNotNull(t.getValue());
        }

        termService.createTerms(list, true);
        assertEquals(list.size(), termRepository.count());
    }

    @Test
    void createWithErrorPreventDelete() {
        final List<BingoTermInput> list = TestDataUtil.generateTerms();

        termService.createTerms(list, false);

        assertEquals(list.size(), termRepository.count());

        List<BingoTermInput> list2 = new ArrayList<>();
        list2.add(BingoTermInput.of(BingoTerm.GENERAL_CATEGORY, "UNIQUE"));
        // Duplicate
        list2.add(list.get(0));
        assertThrows(DataIntegrityViolationException.class, () -> termService.createTerms(list2, false));

        assertEquals(list.size(), termRepository.count());

        list2.remove(1);
        termService.createTerms(list2, false);
        assertEquals(list.size() + 1, termRepository.count());
    }


    @Test
    public void deleteCascade() {
        final List<BingoTermInput> list = TestDataUtil.generateTerms();

        final List<BingoTerm> terms = termService.createTerms(list, false);
        assertEquals(list.size(), termRepository.count());

        final String owner = this.getClass().getName() + "-create";
        final BingoCard card = cardService.createCard(owner);
        final int currentCheckSize = card.getChecks().size();

        assertTrue(currentCheckSize > 0);

        final BingoCheck deleteCheckTerm = card.getChecks().get(0);
        final UUID deleteTermId = deleteCheckTerm.getTerm().getId();

        long beforeDeleteTermCount = termRepository.count();

        this.termRepository.deleteById(deleteTermId);
        this.termRepository.flush();

        System.out.println("DELETE " + deleteTermId);

        assertEquals(beforeDeleteTermCount - 1, termRepository.count());

        final BingoCard updatedCard = this.cardRepository.findById(card.getId()).orElseThrow();
        List<BingoCheck> checks = checkRepository.findByCardId(updatedCard.getId());
        assertEquals(currentCheckSize - 1, checks.size());
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