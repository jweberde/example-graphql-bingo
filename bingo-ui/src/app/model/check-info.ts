export class CheckInfo {
  public checkId!: string;

  public value!: string;

  public static fromJSON(json: any): CheckInfo {
    const o = new CheckInfo();
    o.checkId = String(json.checkId);
    o.value = String(json.value);
    return o;
  }
}

export class CheckInfoState {
  constructor(
    public readonly cardId: string,
    public check: CheckInfo,
    public state: boolean
  ) {}

  public toUpdate(): CheckUpdate {
    return new CheckUpdate(this.cardId, this.check.checkId, this.state);
  }
}

export class CheckUpdate {
  constructor(
    public readonly cardId: string,
    public readonly checkId: string,
    public readonly state: boolean
  ) {}
}
