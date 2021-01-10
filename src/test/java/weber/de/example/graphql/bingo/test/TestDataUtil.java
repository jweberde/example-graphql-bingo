package weber.de.example.graphql.bingo.test;

import weber.de.example.graphql.bingo.entity.BingoTerm;
import weber.de.example.graphql.bingo.entity.dao.BingoTermInput;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TestDataUtil {

    private static final SecureRandom random = new SecureRandom();

    public static List<BingoTermInput> generateTerms() {
        final List<String> categories = Arrays.asList(BingoTerm.GENERAL_CATEGORY, "A", "B", "C", "D");
        final List<BingoTermInput> list = new ArrayList<>();
        for (String cat : categories) {
            for (int x = 1; x < 100 + random.nextInt(10); x++) {
                list.add(BingoTermInput.of(cat, "Term-" + cat + "-" + x));
            }
        }
        return list;
    }
}
