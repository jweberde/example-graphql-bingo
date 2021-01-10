package weber.de.example.graphql.bingo.graphql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import weber.de.example.graphql.bingo.control.BingoCardService;
import weber.de.example.graphql.bingo.control.BingoTermService;
import weber.de.example.graphql.bingo.entity.BingoTerm;
import weber.de.example.graphql.bingo.entity.dao.BingoTermInput;
import weber.de.example.graphql.bingo.graphql.input.TermPoolInput;
import weber.de.example.graphql.bingo.infra.ValidateMethod;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AdminMutationResolver {

    @Autowired
    BingoTermService termService;

    @Autowired
    BingoCardService cardService;

    @ValidateMethod
    public boolean updateTermPool(@Valid TermPoolInput input) {
        final List<BingoTermInput> terms = input.getCategories().stream()
                .flatMap(c -> c.getTerms().stream().map(t -> BingoTermInput.of(c.getCategory(), t)))
                .collect(Collectors.toList());
        final List<BingoTerm> card = this.termService.createTerms(terms, input.isReplace());
        return !card.isEmpty();
    }

    public boolean restartBingo() {
        return this.cardService.restartBingo();
    }

    public int republishStatus() {
        return this.cardService.publishStatus(Optional.empty());
    }
}