package weber.de.example.graphql.bingo.graphql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import weber.de.example.graphql.bingo.control.BingoCardService;
import weber.de.example.graphql.bingo.publisher.event.BingoCardStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class AdminQueryResolver {

    @Autowired
    private BingoCardService cardService;

    @PreAuthorize("isAuthenticated()")
    public String getPingAdmin() {
        log.info("GetPingAdmin");
        return "PongAdmin " + this.getClass().getSimpleName();
    }

    @PreAuthorize("isAuthenticated()")
    public List<BingoCardStatus> getCurrentSessions(Instant since) {
        log.info("GetCurrentSessions");
        return this.cardService.getAllCardStatus(Optional.ofNullable(since));
    }
}
