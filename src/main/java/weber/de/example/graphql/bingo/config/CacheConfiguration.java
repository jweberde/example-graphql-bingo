package weber.de.example.graphql.bingo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("caching")
@EnableCaching
@Configuration
public class CacheConfiguration {
    public static final String BINGO_POOL = "BINGO_POOL";
}
