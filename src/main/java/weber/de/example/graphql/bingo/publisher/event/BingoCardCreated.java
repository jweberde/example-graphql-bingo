package weber.de.example.graphql.bingo.publisher.event;

import lombok.Builder;
import weber.de.example.graphql.bingo.entity.BingoCard;
import weber.de.example.graphql.bingo.entity.dao.BingoCheckInfo;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public class BingoCardCreated {

    private UUID cardId;

    private Instant createdAt;

    private String cardOwner;

    private List<BingoCheckInfo> terms;

    public static BingoCardCreated of(BingoCard card) {
        final List<BingoCheckInfo> terms = Optional.ofNullable(card.getChecks())
                .map(
                        s -> s.stream()
                                .map(check -> BingoCheckInfo.of(check))
                                .collect(Collectors.toList())
                ).orElse(Collections.emptyList());


        return BingoCardCreated.builder()
                .cardId(card.getId())
                .createdAt(card.getCreatedAt())
                .cardOwner(card.getOwner())
                .terms(terms)
                .build();
    }
}
