package weber.de.example.graphql.bingo.entity.dao;

import weber.de.example.graphql.bingo.entity.BingoTerm;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

public class BingoPool {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Category, BingoTerm
     */
    private final Map<String, Set<UUID>> pool = new HashMap<>();
    private final Set<String> categoryRegister = new LinkedHashSet<>();
    private int termCount = 0;

    public BingoPool(List<BingoTerm> terms) {
        this.build(terms);
    }


    private BingoPool build(List<BingoTerm> terms) {
        this.clear();
        // General Group is always first so the terms are "guaranteed"
        this.categoryRegister.add(BingoTerm.GENERAL_CATEGORY);
        this.pool.put(BingoTerm.GENERAL_CATEGORY, new HashSet<>());

        for (BingoTerm t : terms) {
            if (!this.pool.containsKey(t.getCategory())) {
                this.categoryRegister.add(t.getCategory());
                this.pool.put(t.getCategory(), new HashSet<>());
            }
            if (this.pool.get(t.getCategory()).add(t.getId())) {
                this.termCount++;
            }
        }
        return this;
    }

    public List<UUID> pullRandom(int size) {
        if (termCount < size) {
            throw new IllegalArgumentException("Term Pool smaller than " + size);
        }
        final Map<String, Queue<UUID>> tempPool = this.pool.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    final LinkedList<UUID> queueList = new LinkedList<>(e.getValue());
                    Collections.shuffle(queueList, RANDOM);
                    return queueList;
                }));

        final List<UUID> list = new ArrayList<>();

        while (list.size() < size) {
            for (String category : categoryRegister) {
                final Queue<UUID> termIds = tempPool.get(category);
                final UUID randomTerm = termIds.poll();
                if (randomTerm == null) {
                    continue;
                }
                list.add(randomTerm);
                if (list.size() >= size) {
                    // Jump Out Of Category Iteration.
                    break;
                }
            }
        }
        // in combination with the entry condition that should always the case
        assert list.size() == size;

        return list;
    }

    public void clear() {
        this.pool.clear();
        this.categoryRegister.clear();
    }
}
