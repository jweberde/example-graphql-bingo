package weber.de.example.graphql.bingo.control;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import weber.de.example.graphql.bingo.config.CacheConfiguration;
import weber.de.example.graphql.bingo.entity.BingoTerm;
import weber.de.example.graphql.bingo.entity.dao.BingoPool;
import weber.de.example.graphql.bingo.repository.BingoTermRepository;

import java.util.List;

@Service
@Slf4j
public class BingoPoolService {


    private BingoTermRepository bingoTermRepository;

    public BingoPoolService(BingoTermRepository bingoTermRepository) {
        this.bingoTermRepository = bingoTermRepository;
    }

    @Cacheable(CacheConfiguration.BINGO_POOL)
    public BingoPool getBingoTermPool() {
        final List<BingoTerm> all = this.bingoTermRepository.findAll();
        log.info("Build BingoTermPool with {} terms", all.size());
        return new BingoPool(all);
    }

}
