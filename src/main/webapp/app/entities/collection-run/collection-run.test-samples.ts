import dayjs from 'dayjs/esm';

import { ICollectionRun, NewCollectionRun } from './collection-run.model';

export const sampleWithRequiredData: ICollectionRun = {
  id: 8522,
  startedAt: dayjs('2026-06-22T18:22'),
  status: 'FAILED',
};

export const sampleWithPartialData: ICollectionRun = {
  id: 13357,
  startedAt: dayjs('2026-06-22T18:51'),
  finishedAt: dayjs('2026-06-22T07:17'),
  status: 'RUNNING',
};

export const sampleWithFullData: ICollectionRun = {
  id: 18488,
  startedAt: dayjs('2026-06-22T14:25'),
  finishedAt: dayjs('2026-06-22T01:14'),
  status: 'SUCCESS',
  foundCount: 10416,
  processedCount: 29350,
  errorMessage: 'cutlet wedding dandelion',
};

export const sampleWithNewData: NewCollectionRun = {
  startedAt: dayjs('2026-06-21T20:48'),
  status: 'FAILED',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
