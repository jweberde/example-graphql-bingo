import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Apollo, gql } from 'apollo-angular';
import * as Highcharts from 'highcharts';
import { combineLatest, of, ReplaySubject, Subscription } from 'rxjs';
import {
  bufferTime,
  catchError,
  filter,
  map,
  switchMap,
  tap,
} from 'rxjs/operators';
import { BingoCardUpdate } from '../model/bingo-card-update';
import { TopList } from '../model/top-list';
import { BingCardUpdateGQL } from '../service/bing-card-update';
import { BingoRestartGQL } from '../service/bingo-restart';

const UpdateAllState = gql`
  mutation {
    admin {
      republishStatus
    }
  }
`;

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.scss'],
})
export class StatusComponent implements OnInit, OnDestroy {
  Highcharts: typeof Highcharts = Highcharts; // required

  public updateFlag = false; // optional boolean
  public oneToOneFlag = true; // optional boolean, defaults to false

  public clearTextNames = false;

  private userCount = 0;

  public loginForm: FormGroup;

  private userMap = new Map<string, string>();

  private chartInstance!: Highcharts.Chart;

  private $chartLoaded = new ReplaySubject<boolean>(1);

  public selectedCard:
    | (BingoCardUpdate & { symbolicName: string })
    | null = null;

  private $updateStatus = new ReplaySubject(1);

  private topList = new TopList<BingoCardUpdate>(
    10,
    (t) => t.cardId,
    (t) => t.progress,
    BingoCardUpdate.compare
  );

  public chartOptions: Highcharts.Options = {
    chart: {
      type: 'bar',
    },
    credits: {
      enabled: false,
    },
    title: {
      text: 'Bingo',
    },

    yAxis: {
      title: {
        text: '',
      },
      labels: {
        format: '{value}%',
      },
      min: 0,
      max: 100,
    },

    xAxis: {
      type: 'category',
      labels: {
        animate: true,
      } as Highcharts.XAxisGridOptions & { animate: boolean },
    },

    legend: {
      enabled: false,
    },

    series: [
      {
        type: 'bar',
        zoneAxis: 'x',
        events: {
          click: (event) => {
            if (this.loggedIn) {
              this.updateSelectedCard(
                (event.point as any).custom as BingoCardUpdate
              );
            }
          },
        },
        zones: [
          {
            value: 1,
            color: '#ff4d40',
          },
        ],
        dataLabels: {
          enabled: false,
          format: '{y:,.2f}%',
        },
        dataSorting: {
          enabled: true,
          sortKey: 'y', //'custom.value',
        },
        data: [],
      },
    ],
  };
  private subscriptions: Subscription[] = [];
  public url: string;

  public loggedIn: boolean = false;

  constructor(
    private $bingoCardUpdate: BingCardUpdateGQL,
    private $bingoRestart: BingoRestartGQL,
    private apollo: Apollo,
    private snackBar: MatSnackBar,
    formBuilder: FormBuilder,
    router: Router
  ) {
    this.loginForm = formBuilder.group({
      username: [null, [Validators.required, Validators.maxLength(255)]],
      password: [null, [Validators.required, Validators.maxLength(255)]],
    });

    const currentAbsoluteUrl = window.location.href;
    const currentRelativeUrl = router.url;
    const index = currentAbsoluteUrl.indexOf(currentRelativeUrl);
    const baseUrl = currentAbsoluteUrl.substring(0, index);
    this.url = baseUrl + router.createUrlTree(['/']);
  }

  // optional function, defaults to null
  public chartCallback: Highcharts.ChartCallbackFunction = (chartInstance) => {
    this.chartInstance = chartInstance;
    this.$chartLoaded.next(true);
  };

  private updateSelectedCard(bingoCardUpdate: BingoCardUpdate) {
    this.selectedCard = {
      ...bingoCardUpdate,
      symbolicName: this.userMap.get(bingoCardUpdate.cardId) as string,
    };
  }

  public doLogin() {
    if (this.loginForm.invalid) {
      return;
    }
    sessionStorage.setItem(
      'bingo_token',
      btoa(
        this.loginForm.get('username')?.value +
          ':' +
          this.loginForm.get('password')?.value
      )
    );
    this.loggedIn = true;
    this.loginForm.reset();
    this.refresh();
  }

  public doLogout() {
    sessionStorage.removeItem('bingo_token');
    this.loggedIn = false;
    this.hideSelectedUser();
    this.loginForm.reset();
  }

  ngOnInit(): void {
    this.loggedIn = sessionStorage.getItem('bingo_token') != null;

    this.subscriptions.push(
      this.$bingoRestart.subscribe().subscribe((restart) => {
        console.log('Got Restart');
        this.userMap.clear();
        this.newChartDataSnapshot(this.topList.reset());
        this.updateAllStatus().subscribe();
      })
    );

    this.subscriptions.push(
      combineLatest([
        this.$chartLoaded,
        this.$bingoCardUpdate.subscribe().pipe(
          // bufferToggle(openings: Observable, closingSelector: Function),
          map((json: any) =>
            BingoCardUpdate.fromJSON(json.data.bingoCardUpdate)
          ),
          bufferTime(1000), // will emit every second
          filter((buffer) => buffer.length > 0)
        ),
        this.updateAllStatus(),
      ])
        .pipe(
          // Only interested in the values.
          map((c) => c[1])
        )
        .subscribe((values: BingoCardUpdate[]) => {
          this.newChartDataSnapshot(this.topList.update(values));
        })
    );
    setTimeout(() => this.$updateStatus.next(new Date()), 1000);
  }

  private updateAllStatus() {
    return this.$updateStatus.pipe(
      tap((t) => console.log('Update all status')),
      switchMap((_x) =>
        this.apollo
          .mutate({
            mutation: UpdateAllState,
          })
          .pipe(
            catchError((error) => {
              this.snackBar.open('Could not get overall status: ' + error, '', {
                duration: 5000,
              });
              return of(error);
            })
          )
      )
    );
  }

  public hideSelectedUser() {
    this.selectedCard = null;
  }

  public toggleNames() {
    this.clearTextNames = !this.clearTextNames;
    this.newChartDataSnapshot(this.topList.getCurrent());
  }

  public refresh() {
    this.$updateStatus.next(new Date());
  }

  private newChartDataSnapshot(values: BingoCardUpdate[]) {
    const snapshot = values.map((v) => {
      if (!this.userMap.has(v.cardId)) {
        this.userMap.set(v.cardId, 'Retarier ' + ++this.userCount);
      }
      const base: Highcharts.PointOptionsObject = {
        y: v.progress,
        custom: v,
      };
      if (this.selectedCard && this.selectedCard.cardId === v.cardId) {
        this.updateSelectedCard(v);
      }
      if (this.clearTextNames) {
        return { ...base, name: v.owner };
      } else {
        return { ...base, name: this.userMap.get(v.cardId) };
      }
    });
    this.chartInstance.series[0].setData(snapshot);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
