package weber.de.example.graphql.bingo.graphql.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CardInput {

    @NotBlank
    private String owner;
}
