package weber.de.example.graphql.bingo.graphql;

import graphql.kickstart.tools.GraphQLMutationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import weber.de.example.graphql.bingo.control.BingoCardService;
import weber.de.example.graphql.bingo.entity.BingoCard;
import weber.de.example.graphql.bingo.graphql.input.CardCheck;
import weber.de.example.graphql.bingo.graphql.input.CardInput;
import weber.de.example.graphql.bingo.infra.ValidateMethod;
import weber.de.example.graphql.bingo.publisher.event.BingoCardCreated;

import javax.validation.Valid;

@Component
public class RootMutationResolver implements GraphQLMutationResolver {

    @Autowired
    BingoCardService cardService;

    @Autowired
    AdminMutationResolver adminMutationResolver;

    @PreAuthorize("isAuthenticated()")
    public AdminMutationResolver getAdmin() {
        return adminMutationResolver;
    }

    @ValidateMethod
    public BingoCardCreated createCard(@Valid CardInput input) {
        final BingoCard card = this.cardService.createCard(input.getOwner());
        return BingoCardCreated.of(card);
    }

    @ValidateMethod
    public boolean check(@Valid CardCheck input) {
        return this.cardService.addCheck(input.getCardId(), input.getCheckId(), input.getChecked().booleanValue()) != null;
    }
}
