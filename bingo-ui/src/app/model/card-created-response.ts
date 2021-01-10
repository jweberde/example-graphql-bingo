import { CheckInfo } from './check-info';

export class CardCreatedResponse {
  public cardId!: string;
  public createdAt!: Date;
  public cardOwner!: string;
  public graqhqlTypeName!: string;
  public terms!: CheckInfo[];

  public static fromJSON(json: any): CardCreatedResponse {
    const o = new CardCreatedResponse();
    o.cardId = String(json.cardId);
    o.createdAt = new Date(json.createdAt);
    o.cardOwner = String(json.cardOwner);
    o.graqhqlTypeName = String(json.__typename);
    o.terms = Array.isArray(json.terms)
      ? (json.terms as any[]).map((v) => CheckInfo.fromJSON(v))
      : [];
    return o;
  }
}
