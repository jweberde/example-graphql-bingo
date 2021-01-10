package weber.de.example.graphql.bingo.entity.dao;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BingoCheckInfoTest {

    @Test
    public void testSorting() {
        final BingoCheckInfo valueA = BingoCheckInfo.of(UUID.randomUUID(), "A");
        final BingoCheckInfo valueB = BingoCheckInfo.of(UUID.randomUUID(), "B");
        final BingoCheckInfo valueC = BingoCheckInfo.of(UUID.randomUUID(), "C");

        List<BingoCheckInfo> list = new ArrayList<>();

        list.add(valueB);
        list.add(valueC);
        list.add(valueA);

        Collections.sort(list);

        assertEquals(list.get(0), valueA);
        assertEquals(list.get(1), valueB);
        assertEquals(list.get(2), valueC);
    }

}