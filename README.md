# GraphQL Example using SpringBoot, Angular

The application is small "Bingo" application used during christmas event. Bingo is reached, when you have 
ticked *all* terms mentioned on you card. Compared to a regular bingo where you need three in a row.

The communication with the backend uses buffered input via GraphQL Mutations. There is a small status interface, which display a bar chart with the status of the different player cards.
The size of the cards is adjustable. 9 worked fine on mobile phones. The status page is driven by GraphQL subscriptions and uses the event stream 
to keep the graph up to date.

Backend exposes Mutations, Queries and Subscriptions via GraphQL. Some Queries, Mutations, Subscriptions are only available for authenticated users.

Still open
- Expire WebSocket Sessions after a fixed time to simulate expiring JWT tokens for example.
