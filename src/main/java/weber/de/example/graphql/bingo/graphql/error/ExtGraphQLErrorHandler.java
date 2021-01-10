package weber.de.example.graphql.bingo.graphql.error;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.kickstart.execution.error.DefaultGraphQLErrorHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

public class ExtGraphQLErrorHandler extends DefaultGraphQLErrorHandler {

    @Override
    @ExceptionHandler
    public List<GraphQLError> processErrors(List<GraphQLError> errors) {
        final List<GraphQLError> wrappedErrors = errors.stream().map(this::wrap).collect(Collectors.toList());
        return super.processErrors(wrappedErrors);
    }

    @Override
    protected List<GraphQLError> filterGraphQLErrors(List<GraphQLError> errors) {
        return super.filterGraphQLErrors(errors);
    }

    public GraphQLError wrap(GraphQLError error) {
        if (error instanceof ExceptionWhileDataFetching) {
            return wrap(ExceptionWhileDataFetching.class.cast(error));
        }
        return error;
    }

    public GraphQLError wrap(ExceptionWhileDataFetching error) {
        if (error.getException() instanceof AccessDeniedException) {
            return ExceptionUtil.accessDenied(error, (AccessDeniedException) error.getException());
        }
        return error;
    }

    @Override
    protected boolean isClientError(GraphQLError error) {
        if (super.isClientError(error)) {
            return true;
        } else if (error instanceof ExceptionWhileDataFetching) {
            return ((ExceptionWhileDataFetching) error).getException() instanceof ConstraintViolationException;
        } else {
            return false;
        }
    }
}