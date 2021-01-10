import { Injector, NgModule } from '@angular/core';
import { APOLLO_OPTIONS } from 'apollo-angular';
import {
  ApolloClientOptions,
  ApolloLink,
  InMemoryCache,
  split,
} from '@apollo/client/core';
import { HttpLink } from 'apollo-angular/http';
import { getMainDefinition } from '@apollo/client/utilities';
import { WebSocketLink } from '@apollo/client/link/ws';
import { SubscriptionClient } from 'subscriptions-transport-ws';
import { APP_BASE_HREF } from '@angular/common';
import { setContext } from '@apollo/client/link/context';

export function createApollo(httpLink: HttpLink): ApolloClientOptions<any> {
  const basic = setContext((operation, context) => ({
    headers: {
      Accept: 'charset=utf-8',
    },
  }));

  const auth = setContext((operation, context) => {
    const token = sessionStorage.getItem('bingo_token');

    if (token === null) {
      return {};
    } else {
      return {
        headers: {
          Authorization: `Basic ${token}`,
        },
      };
    }
  });

  // see https://apollo-angular.com/docs/recipes/authentication/

  const http = ApolloLink.from([
    basic,
    auth,
    httpLink.create({ uri: '/bingo/api/graphql' }),
  ]);

  const baseUrl = location.origin.substr(location.origin.indexOf(':') + 3);
  const protocol = location.origin.startsWith('https') ? 'wss' : 'ws';
  const wsClient = new SubscriptionClient(
    `${protocol}://${baseUrl}/bingo/api/subscriptions`,
    {
      reconnect: true,
    }
  );

  const ws = new WebSocketLink(wsClient);

  // using the ability to split links, you can send data to each link
  // depending on what kind of operation is being sent
  const link = split(
    // split based on operation type
    ({ query }) => {
      const definition = getMainDefinition(query);
      return (
        definition.kind === 'OperationDefinition' &&
        definition.operation === 'subscription'
      );
    },
    ws,
    http
  );

  return {
    link: link, //httpLink.create({uri}),
    cache: new InMemoryCache(),
  };
}

@NgModule({
  providers: [
    {
      provide: APOLLO_OPTIONS,
      useFactory: createApollo,
      deps: [HttpLink],
    },
  ],
})
export class GraphQLModule {}
