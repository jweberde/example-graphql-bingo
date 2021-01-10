import { CheckInfo, CheckInfoState } from './check-info';

export class BingoCardUpdate {
  public cardId!: string;
  public owner!: string;
  public checkedCount!: number;
  public checkedTerms!: CheckInfo[];
  public missingCount!: number;
  public missingTerms!: CheckInfo[];
  public progress = 0;

  public terms!: CheckInfoState[];

  static compare = (a: BingoCardUpdate, b: BingoCardUpdate) => {
    const compare1 = a.progress - b.progress;
    if (compare1 !== 0) {
      return compare1;
    } else {
      // TIE Breaker
      return a.cardId.localeCompare(b.cardId);
    }
  };

  public static fromJSON(input: any): BingoCardUpdate {
    const o = new BingoCardUpdate();
    o.cardId = String(input.cardId);
    o.owner = String(input.owner);

    o.checkedCount = Number(input.checkedCount);
    o.checkedTerms = Array.isArray(input.checkedTerms)
      ? (input.checkedTerms as any[]).map((v) => CheckInfo.fromJSON(v))
      : [];

    o.missingCount = Number(input.missingCount);
    o.missingTerms = Array.isArray(input.missingTerms)
      ? (input.missingTerms as any[]).map((v) => CheckInfo.fromJSON(v))
      : [];

    o.progress = Math.round(
      (o.checkedCount / (o.missingCount + o.checkedCount)) * 100
    );

    o.terms = o.missingTerms
      .map((t) => new CheckInfoState(o.cardId, t, false))
      .concat(o.checkedTerms.map((t) => new CheckInfoState(o.cardId, t, true)))
      .sort((a, b) => a.check.checkId.localeCompare(b.check.checkId));
    return o;
  }
}
