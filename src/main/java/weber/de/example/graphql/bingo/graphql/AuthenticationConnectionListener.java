package weber.de.example.graphql.bingo.graphql;

import graphql.kickstart.execution.subscriptions.SubscriptionSession;
import graphql.kickstart.execution.subscriptions.apollo.ApolloSubscriptionConnectionListener;
import graphql.kickstart.execution.subscriptions.apollo.OperationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

// @Component
@Slf4j
class AuthenticationConnectionListener implements ApolloSubscriptionConnectionListener {

    @Override
    public void onConnect(SubscriptionSession session, OperationMessage message) {
        log.debug("onConnect with payload {}", message.getPayload().getClass());
        String token = ((Map<String, String>) message.getPayload()).get("authToken");
        log.info("Token: {}", token);
        Authentication authentication = new UsernamePasswordAuthenticationToken(token, null);
        session.getUserProperties().put("CONNECT_TOKEN", authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}