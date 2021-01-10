package weber.de.example.graphql.bingo.entity.dao;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import weber.de.example.graphql.bingo.entity.BingoCheck;

import java.util.Objects;
import java.util.UUID;

@Value(staticConstructor = "of")
public class BingoCheckInfo implements Comparable<BingoCheckInfo> {

    private UUID checkId;

    private String value;

    public static BingoCheckInfo of(BingoCheck check) {
        if (check == null) {
            throw new IllegalArgumentException("Check cannot be null");
        }
        return of(check.getId(), Objects.requireNonNull(check.getTerm()).getValue());
    }


    @Override
    public int compareTo(@NotNull BingoCheckInfo o) {
        int stringCompare = this.getValue().compareToIgnoreCase(o.getValue());
        if (stringCompare != 0) {
            return stringCompare;
        }
        // TIE BREAKER
        return this.getCheckId().compareTo(o.getCheckId());
    }
}
