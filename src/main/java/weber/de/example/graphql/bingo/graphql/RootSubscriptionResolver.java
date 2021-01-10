package weber.de.example.graphql.bingo.graphql;

import graphql.kickstart.servlet.context.GraphQLWebSocketContext;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import weber.de.example.graphql.bingo.control.BingoRestartEvent;
import weber.de.example.graphql.bingo.publisher.BingoCardUpdatePublisher;
import weber.de.example.graphql.bingo.publisher.event.BingoCardCreated;
import weber.de.example.graphql.bingo.publisher.event.BingoCardStatus;

import javax.websocket.Session;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RootSubscriptionResolver implements GraphQLSubscriptionResolver {

    @Autowired
    BingoCardUpdatePublisher bingoCardUpdatePublisher;

    public Publisher<BingoCardStatus> getBingoCardUpdate(UUID cardId, DataFetchingEnvironment env) {
        log.info("Publisher getBingoCardUpdate: {}", cardId);
        // authenticate(env);
        if (cardId != null) {
            return bingoCardUpdatePublisher.getUpdatePublisher()
                    .filter(c -> c.getCardId().equals(cardId));
        } else {
            return bingoCardUpdatePublisher.getUpdatePublisher();
        }
    }

    public Publisher<BingoRestartEvent> getBingoRestart(DataFetchingEnvironment env) {
        // authenticate(env);
        return bingoCardUpdatePublisher.getRestartPublisher();
    }

    public Publisher<BingoCardCreated> getBingoCardCreated(DataFetchingEnvironment env) {
        // authenticate(env);
        return bingoCardUpdatePublisher.getCreatePublisher();
    }


    private void authenticate(DataFetchingEnvironment env) {
        log.info("Publisher getBingoCardCreated");
        GraphQLWebSocketContext context = env.getContext();
        Optional<Authentication> authentication = Optional.ofNullable(context.getSession())
                .map(Session::getUserProperties)
                .map(props -> props.get("CONNECT_TOKEN"))
                .map(Authentication.class::cast);
        log.info("Subscribe to publisher with token: {}", authentication);
        authentication.ifPresent(SecurityContextHolder.getContext()::setAuthentication);
        log.info("Security context principal: {}", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}