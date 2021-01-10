package weber.de.example.graphql.bingo.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import weber.de.example.graphql.bingo.entity.BingoCard;
import weber.de.example.graphql.bingo.entity.BingoTerm;
import weber.de.example.graphql.bingo.entity.dao.BingoCheckInfo;
import weber.de.example.graphql.bingo.entity.dao.BingoPool;
import weber.de.example.graphql.bingo.entity.dao.BingoTermCheckInput;
import weber.de.example.graphql.bingo.publisher.BingoCardUpdatePublisher;
import weber.de.example.graphql.bingo.publisher.event.BingoCardStatus;
import weber.de.example.graphql.bingo.repository.BingoCardRepository;
import weber.de.example.graphql.bingo.repository.BingoCheckRepository;
import weber.de.example.graphql.bingo.repository.BingoTermRepository;
import weber.de.example.graphql.bingo.util.CardStatusUtil;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "app.card-size=3"
})
class BingoCardServiceTest {

    public static final String STANDARD_OWNER = "Example";

    @TestConfiguration
    static class BingoCardServiceTestConfig {
        @Bean
        public BingoCardService cardService() {
            return new BingoCardService();
        }
    }

    @MockBean
    BingoCardRepository bingoCardRepository;

    @MockBean
    BingoTermRepository bingoTermRepository;

    @MockBean
    BingoCheckRepository bingoCheckRepository;

    @MockBean
    BingoCardUpdatePublisher publisher;

    @MockBean
    BingoPoolService bingoPoolService;

    @Value("${app.card-size}")
    int expectedAppCardSize;

    @Autowired
    BingoCardService service;

    private final Map<UUID, BingoTerm> testTerms = Stream.of("A", "B", "C", "D", "E", "F", "G")
            .map(s -> {
                final BingoTerm bingoTerm = new BingoTerm();
                bingoTerm.setValue(s);
                bingoTerm.setCategory("some_category");
                bingoTerm.setUpdatedAt(Instant.now());
                bingoTerm.setCreatedAt(Instant.now());
                bingoTerm.setId(UUID.randomUUID());
                return bingoTerm;
            }).collect(Collectors.toMap(BingoTerm::getId, Function.identity()));


    private final Map<UUID, BingoCard> savedCards = new HashMap<>();

    @BeforeEach
    void before() {
        final BingoPool pool = new BingoPool(new ArrayList<>(testTerms.values()));
        Mockito.when(bingoPoolService.getBingoTermPool()).thenReturn(pool);

        Mockito.when(bingoTermRepository.getOne(Mockito.any())).thenAnswer(a -> {
            final BingoTerm bingoTerm = testTerms.get(a.getArgument(0, UUID.class));
            if (bingoTerm == null) {
                throw new EntityNotFoundException("Could not find " + a.getArgument(0, UUID.class));
            }
            return bingoTerm;
        });

        Mockito.when(bingoCardRepository.save(Mockito.any())).thenAnswer(a -> {
            final BingoCard theCardForSave = a.getArgument(0, BingoCard.class);
            theCardForSave.setId(UUID.randomUUID());
            savedCards.put(theCardForSave.getId(), theCardForSave);
            return theCardForSave;
        });

        Mockito.when(bingoCardRepository.findCardStatus(Mockito.any(UUID.class), Mockito.anyBoolean())).then(a -> {
            final UUID id = a.getArgument(0, UUID.class);
            final boolean withTerms = a.getArgument(1, Boolean.class);
            if (savedCards.containsKey(id)) {
                final BingoCard bingoCard = savedCards.get(id);
                return CardStatusUtil.calculateStatus(bingoCard.getId(), bingoCard.getOwner(), bingoCard.getChecks().stream().map(t -> BingoTermCheckInput.of(t.getId(), t.isChecked(), t.getTerm())).collect(Collectors.toList()), withTerms);
            } else {
                return Optional.empty();
            }
        });
        Mockito.when(bingoCardRepository.findAllCardStatus(Mockito.anyBoolean(), Mockito.any(Optional.class)))
                .then(a -> this.savedCards.keySet()
                        .stream()
                        // Reusing the previous Mock.
                        .collect(Collectors.toMap(Function.identity(), k -> this.bingoCardRepository.findCardStatus(k, a.getArgument(0, Boolean.class))))
                );
    }

    @Test
    void createCard() {
        final BingoCard newCard = this.service.createCard(STANDARD_OWNER);
        assertThat(newCard).isNotNull();
        assertThat(newCard.getOwner()).isEqualTo(STANDARD_OWNER);

        Mockito.verify(this.bingoCardRepository).save(newCard);
        Mockito.verify(this.publisher).publish(Mockito.any(BingoCard.class));
        Mockito.verify(this.publisher).publish(Mockito.any(BingoCardStatus.class));
    }


    @Test
    void getCardStatus() {
        final BingoCard newCard = this.service.createCard(STANDARD_OWNER);
        assertThat(newCard).isNotNull();
        final BingoCardStatus cardStatus = this.service.getCardStatus(newCard.getId());
        assertThat(cardStatus).isNotNull();
        assertThat(cardStatus.getCheckedCount()).isEqualTo(0);
        assertThat(cardStatus.getMissingCount()).isEqualTo(expectedAppCardSize);

        final List<BingoCheckInfo> expectedBingoCheckInfo = newCard.getChecks().stream().map(BingoCheckInfo::of).collect(Collectors.toList());
        assertThat(cardStatus.getMissingTerms()).containsExactlyInAnyOrderElementsOf(expectedBingoCheckInfo);

        assertThat(cardStatus.getCheckedTerms()).isEmpty();
    }

    @Test
    void publishStatus() {
        assertThat(this.service.createCard(STANDARD_OWNER + "1")).isNotNull();
        assertThat(this.service.createCard(STANDARD_OWNER + "2")).isNotNull();

        Mockito.verify(this.publisher, times(2)).publish(Mockito.any(BingoCardStatus.class));

        final int publishedStatuses = this.service.publishStatus(Optional.empty());
        assertThat(publishedStatuses).isEqualTo(2);

        Mockito.verify(this.publisher, times(4)).publish(Mockito.any(BingoCardStatus.class));
    }

    @Test
    void restartBingo() {
        this.service.restartBingo();
        Mockito.verify(this.publisher).publish(Mockito.any(BingoRestartEvent.class));
        Mockito.verify(this.bingoCardRepository).deleteAll();
    }
}