package weber.de.example.graphql.bingo.publisher.event;

import lombok.Value;
import weber.de.example.graphql.bingo.entity.dao.BingoCheckInfo;

import java.util.List;
import java.util.UUID;

@Value(staticConstructor = "of")
public class BingoCardStatus {

    private UUID cardId;

    private String owner;


    private int missingCount;

    private List<BingoCheckInfo> missingTerms;

    private int checkedCount;

    private List<BingoCheckInfo> checkedTerms;

}
