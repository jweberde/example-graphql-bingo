import { Injectable } from '@angular/core';
import { gql, Subscription } from 'apollo-angular';

@Injectable({
  providedIn: 'root',
})
export class BingCardUpdateGQL extends Subscription {
  document = gql`
    subscription {
      bingoCardUpdate {
        cardId
        owner
        checkedCount
        checkedTerms {
          checkId
          value
        }
        missingCount
        missingTerms {
          checkId
          value
        }
      }
    }
  `;
}
