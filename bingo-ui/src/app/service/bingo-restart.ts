import { Injectable } from '@angular/core';
import { gql, Subscription } from 'apollo-angular';

@Injectable({
  providedIn: 'root',
})
export class BingoRestartGQL extends Subscription {
  document = gql`
    subscription {
      bingoRestart {
        restart
        createdAt
      }
    }
  `;
}
