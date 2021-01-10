package weber.de.example.graphql.bingo.config;

import graphql.Scalars;
import graphql.kickstart.spring.web.boot.GraphQLWebAutoConfiguration;
import graphql.schema.GraphQLScalarType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import weber.de.example.graphql.bingo.graphql.error.ExtGraphQLErrorHandler;

@Configuration
@ConditionalOnClass(GraphQLWebAutoConfiguration.class)
public class GraphQLConfiguration {

    @Bean
    ExtGraphQLErrorHandler graphQLErrorHandler() {
        return new ExtGraphQLErrorHandler();
    }

    @Bean
    GraphQLScalarType principalScalar() {
        return GraphQLScalarType.newScalar(Scalars.GraphQLString)
                .name("DateTimeISO8601").description("String representing an ISO-8601 Date Time with Timezone").build();
    }
}
