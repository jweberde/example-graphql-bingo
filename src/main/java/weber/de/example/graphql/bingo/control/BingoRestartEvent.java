package weber.de.example.graphql.bingo.control;

import lombok.Value;

import java.time.Instant;

@Value(staticConstructor = "of")
public class BingoRestartEvent {

    private Instant createdAt = Instant.now();

    private boolean restart = true;
}
