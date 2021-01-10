package weber.de.example.graphql.bingo.control;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import weber.de.example.graphql.bingo.config.CacheConfiguration;
import weber.de.example.graphql.bingo.entity.BingoTerm;
import weber.de.example.graphql.bingo.entity.dao.BingoTermInput;
import weber.de.example.graphql.bingo.repository.BingoTermRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
@Slf4j
public class BingoTermService {

    @Autowired
    BingoTermRepository bingoTermRepository;

    @CacheEvict(cacheNames = {CacheConfiguration.BINGO_POOL})
    @Transactional
    public List<BingoTerm> createTerms(List<BingoTermInput> terms, boolean replace) {
        if (replace) {
            this.bingoTermRepository.deleteAll();
            this.bingoTermRepository.flush();
        }
        final Function<BingoTermInput, String> keyFunction = (dao) -> dao.getCategory() + " " + dao.getTerm();

        final Set<String> existing = new HashSet<>();
        final List<BingoTerm> newTermsForSaving = new ArrayList<>();
        for (BingoTermInput dao : terms) {
            final String key = keyFunction.apply(dao);
            if (existing.contains(key)) {
                log.warn("Silently skipping duplicated term {}", dao);
                continue;
            }
            existing.add(key);

            BingoTerm newTerm = new BingoTerm();
            newTerm.setCategory(dao.getCategory());
            newTerm.setValue(dao.getTerm());
            newTermsForSaving.add(newTerm);
            log.info("New Term for Saving {}", newTerm);
        }
        return this.bingoTermRepository.saveAll(newTermsForSaving);
    }

}
