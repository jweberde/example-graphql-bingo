package weber.de.example.graphql.bingo.graphql.error;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.GraphqlErrorException;
import org.springframework.security.access.AccessDeniedException;

public class ExceptionUtil {

    public static GraphQLError accessDenied(ExceptionWhileDataFetching error, AccessDeniedException ex) {
        return GraphqlErrorException.newErrorException()
                .errorClassification(error.getErrorType())
                .message(ex.getMessage())
                .cause(ex)
                .sourceLocations(error.getLocations())
                .path(error.getPath())
                .build();
    }

}