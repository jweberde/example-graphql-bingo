import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Apollo, gql } from 'apollo-angular';
import { map } from 'rxjs/operators';
import { CardCreatedResponse } from '../model/card-created-response';

const CreateCardRepository = gql`
  mutation CreateCardRepository($owner: String!) {
    createCard(input: { owner: $owner }) {
      cardId
      createdAt
      cardOwner
    }
  }
`;

@Injectable({
  providedIn: 'root',
})
export class CardService {
  constructor(private apollo: Apollo, private router: Router) {}

  public createNewCard(name: string, redirect: boolean) {
    return this.apollo
      .mutate({
        mutation: CreateCardRepository,
        variables: {
          owner: name,
        },
      })
      .pipe(
        map(({ data }) => {
          return CardCreatedResponse.fromJSON((data as any).createCard);
        }),
        map((newCard: CardCreatedResponse) => {
          if (redirect) {
            this.redirectToSession(newCard.cardId);
          }
          return newCard;
        })
      );
  }

  private redirectToSession(cardId: string): void {
    if (!cardId) {
      return;
    }
    this.router.navigate(['/session', cardId]);
  }
}
