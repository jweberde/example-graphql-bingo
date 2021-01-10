package weber.de.example.graphql.bingo.graphql.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class TermPoolInput {

    private List<TermPoolCategoryInput> categories;

    private boolean replace;

    @Data
    public static class TermPoolCategoryInput {

        @NotBlank
        private String category;

        @NotEmpty
        public List<@NotBlank String> terms;

    }
}
