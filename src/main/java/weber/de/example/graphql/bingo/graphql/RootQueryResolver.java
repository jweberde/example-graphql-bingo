package weber.de.example.graphql.bingo.graphql;

import graphql.kickstart.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import weber.de.example.graphql.bingo.control.BingoCardService;
import weber.de.example.graphql.bingo.publisher.event.BingoCardStatus;

import java.util.UUID;

@Slf4j
@Component
public class RootQueryResolver implements GraphQLQueryResolver {

    @Autowired
    private BingoCardService cardService;

    @Autowired
    private AdminQueryResolver admin;

    public BingoCardStatus getStatus(UUID cardId) {
        final BingoCardStatus cardStatus = this.cardService.getCardStatus(cardId);
        return cardStatus;
    }

    public String getPing(DataFetchingEnvironment ex) {
        log.info("GetPing");
        return "Pong " + this.getClass().getSimpleName();
    }

}
