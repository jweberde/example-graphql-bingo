import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { Apollo, gql } from 'apollo-angular';
import { EMPTY, empty, of, Subject, Subscription, zip } from 'rxjs';
import { bufferTime, filter, map, mergeMap, switchMap } from 'rxjs/operators';
import { BingoCardUpdate } from '../model/bingo-card-update';
import { CardCreatedResponse } from '../model/card-created-response';
import { CheckInfoState, CheckUpdate } from '../model/check-info';
import { BingoRestartGQL } from '../service/bingo-restart';
import { CardService } from '../service/card.service';

const CurrentStatus = gql`
  query status($cardId: ID) {
    cardStatus: status(cardId: $cardId) {
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

const UpdateState = gql`
  mutation UpdateState($input: CardCheck!) {
    cardCheck: check(input: $input)
  }
`;

@Component({
  selector: 'app-session',
  templateUrl: './session.component.html',
  styleUrls: ['./session.component.scss'],
})
export class SessionComponent implements OnInit, OnDestroy {
  public items: CheckInfoState[] = [];
  public loading = true;
  public newCard!: CardCreatedResponse;

  public cardId: string | undefined;

  public columns = 2;

  private subscriptions: Subscription[] = [];

  private $update = new Subject<CheckUpdate>();

  public bingo = false;
  public card: BingoCardUpdate | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apollo: Apollo,
    private breakpointObserver: BreakpointObserver,
    private snackBar: MatSnackBar,
    private $bingoRestart: BingoRestartGQL,
    private cardService: CardService
  ) {}

  ngOnInit(): void {
    this.subscriptions.push(
      zip(
        this.breakpointObserver.observe([Breakpoints.XSmall]),
        this.breakpointObserver.observe([Breakpoints.Small])
      ).subscribe((result) => {
        if (result[0].matches) {
          this.columns = 1;
        } else if (result[1].matches) {
          this.columns = 2;
        } else {
          this.columns = 3;
        }
      })
    );

    this.$bingoRestart.subscribe().subscribe((value) => {
      console.log('Got Restart', value);
      const currentOwner =
        this.card != null && this.card.owner ? this.card.owner : null;

      this.loading = true;
      this.items.length = 0;
      this.card = null;
      this.checkForBingo();

      if (currentOwner == null) {
        this.router.navigate(['/']);
      } else {
        this.cardService.createNewCard(currentOwner, true).subscribe(
          (newCard: CardCreatedResponse) => {
            this.newCard = newCard;
          },
          (error) => {
            console.error('Restart Error', error);
            this.snackBar.open(
              'Something went wrong, try again: ' + error,
              '',
              {
                duration: 2000,
              }
            );
          }
        );
      }
    });

    this.subscriptions.push(
      this.$update
        .pipe(
          bufferTime(500),
          map((cards) => {
            if (cards.length <= 0) {
              return cards;
            }
            // Make unique
            const result: CheckUpdate[] = [];
            const map = new Map();
            const lastFirst = cards.reverse();
            for (const item of lastFirst) {
              if (!map.has(item.checkId)) {
                map.set(item.checkId, true); // set any value to Map
                result.push(item);
              }
            }
            return result;
          }),
          filter((cards) => cards.length > 0),
          mergeMap((cards: CheckUpdate[]) => {
            return zip.apply(
              this,
              cards.map((c) => {
                if (this.loading) {
                  return EMPTY;
                }
                return this.apollo.mutate({
                  mutation: UpdateState,
                  variables: {
                    input: {
                      cardId: c.cardId,
                      checkId: c.checkId,
                      checked: c.state,
                    },
                  },
                });
              })
            );
          })
        )
        .subscribe(
          (result) => {
            // console.log(result);
          },
          (error) => {
            this.snackBar.open(
              'Something went wrong, try again: ' + error,
              '',
              {
                duration: 5000,
              }
            );
          }
        )
    );

    this.subscriptions.push(
      this.route.params
        .pipe(
          filter((p) => p.cardId),
          switchMap((p) => {
            return this.apollo.watchQuery<any>({
              query: CurrentStatus,
              variables: {
                cardId: p.cardId,
              },
            }).valueChanges;
          })
        )
        .subscribe(
          ({ data, loading }) => {
            this.loading = loading;
            const cardStatus = BingoCardUpdate.fromJSON(data.cardStatus);
            this.items = cardStatus.terms;
            this.card = cardStatus;
            this.checkForBingo();
          },
          (error) => {
            this.snackBar.open(
              'Something went wrong, try again: ' + error,
              '',
              {
                duration: 5000,
              }
            );
            this.router.navigate(['/']);
          }
        )
    );
  }

  private checkForBingo() {
    this.bingo = this.items.find((v) => v.state === false) === undefined;
  }

  public updateCheck(card: CheckInfoState): void {
    card.state = !card.state;
    this.checkForBingo();
    this.$update.next(card.toUpdate());
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
    console.log('DESTROY');
  }
}
