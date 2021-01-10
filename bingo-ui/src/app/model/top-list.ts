import { throwServerError } from '@apollo/client/core';

export class TopList<T> {
  private currentMin = 0;
  private currentMax = 0;
  private currentData: T[] = [];

  constructor(
    private readonly limit: number,
    private readonly idFunc: (a: T) => string,
    private readonly rankFunc: (a: T) => number,
    private readonly compareFunc: (a: T, b: T) => number
  ) {}

  public getCurrent(): T[] {
    return this.currentData;
  }

  public reset(): T[] {
    this.currentMin = 0;
    this.currentMax = 0;
    this.currentData.length = 0;
    return this.getCurrent();
  }

  public update(newValues: T[]): T[] {
    const max = this.currentMax;
    const min = this.currentMin;
    // Add new Elements.

    const newValueMap = new Map();
    const newList: T[] = [];
    // Latest Events First.
    newValues.reverse().forEach((v) => {
      if (newValueMap.has(this.idFunc(v))) {
        return;
      }
      newValueMap.set(this.idFunc(v), v);
      newList.push(v);
    });

    // Remove duplicated
    const sortedList = newList
      .concat(this.currentData.filter((v) => !newValueMap.has(this.idFunc(v))))
      .sort(this.compareFunc);

    this.currentData = sortedList
      .reverse()
      .slice(0, Math.min(this.limit, sortedList.length));
    return this.currentData;
  }
}
