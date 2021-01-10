package weber.de.example.graphql.bingo.graphql.input;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class CardCheck {

    @NotNull
    private UUID cardId;

    @NotNull
    private UUID checkId;

    @NotNull
    private Boolean checked;

}
