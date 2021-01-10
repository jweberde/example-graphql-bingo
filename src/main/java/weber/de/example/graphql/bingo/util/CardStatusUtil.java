package weber.de.example.graphql.bingo.util;

import weber.de.example.graphql.bingo.entity.BingoCard;
import weber.de.example.graphql.bingo.entity.BingoTerm;
import weber.de.example.graphql.bingo.entity.dao.BingoCheckInfo;
import weber.de.example.graphql.bingo.entity.dao.BingoTermCheckInput;
import weber.de.example.graphql.bingo.publisher.event.BingoCardStatus;

import java.util.*;

public class CardStatusUtil {

    public static Optional<BingoCardStatus> calculateStatus(BingoCard bingoCard, List<BingoTermCheckInput> checks, boolean withTerms) {
        if (bingoCard == null) {
            throw new IllegalArgumentException("BingoCard must be not null");
        }
        return calculateStatus(bingoCard.getId(), bingoCard.getOwner(), checks, withTerms);
    }

    public static Optional<BingoCardStatus> calculateStatus(UUID bingoCardId, String bingoCardOwner, List<BingoTermCheckInput> checks, boolean withTerms) {
        if (checks == null) {
            throw new IllegalArgumentException("Checks must be not null");
        }

        final List<BingoCheckInfo> missingTerms = new ArrayList<>();
        final List<BingoCheckInfo> checkedTerms = new ArrayList<>();

        int checkedCount = 0;
        int missingCount = 0;

        for (BingoTermCheckInput t : checks) {
            final UUID theCheckId = t.getCheckId();
            final boolean theCheckedState = t.isCheckState();
            if (withTerms) {
                final BingoTerm theTerm = t.getTerm();
                final BingoCheckInfo checkInfo = BingoCheckInfo.of(theCheckId, theTerm.getValue());
                if (theCheckedState) {
                    checkedTerms.add(checkInfo);
                } else {
                    missingTerms.add(checkInfo);
                }
            }
            if (theCheckedState) {
                checkedCount++;
            } else {
                missingCount++;
            }
        }

        Collections.sort(missingTerms);
        Collections.sort(checkedTerms);

        final BingoCardStatus status = BingoCardStatus.of(
                bingoCardId,
                bingoCardOwner,
                missingCount,
                missingTerms,
                checkedCount,
                checkedTerms
        );
        return Optional.of(status);
    }
}
