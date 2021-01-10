package weber.de.example.graphql.bingo.entity.dao;

import lombok.Value;

import javax.validation.constraints.NotBlank;


@Value(staticConstructor = "of")
public class BingoTermInput {

    @NotBlank
    private String category;

    @NotBlank
    private String term;

}
