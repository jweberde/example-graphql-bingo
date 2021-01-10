package weber.de.example.graphql.bingo.entity.dao;

import lombok.Value;
import weber.de.example.graphql.bingo.entity.BingoTerm;

import java.util.UUID;

@Value(staticConstructor = "of")
public class BingoTermCheckInput {

    UUID checkId;
    boolean checkState;
    BingoTerm term;
}
