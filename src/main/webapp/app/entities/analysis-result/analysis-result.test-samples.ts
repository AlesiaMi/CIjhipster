import dayjs from 'dayjs/esm';

import { IAnalysisResult, NewAnalysisResult } from './analysis-result.model';

export const sampleWithRequiredData: IAnalysisResult = {
  id: 31086,
  sentiment: 'POSITIVE',
  status: 'PENDING',
};

export const sampleWithPartialData: IAnalysisResult = {
  id: 29860,
  summary: 'quaff pine',
  sentiment: 'NEGATIVE',
  topic: 'boo',
  status: 'FAILED',
  analyzedAt: dayjs('2026-06-22T02:34'),
  errorMessage: 'inside',
};

export const sampleWithFullData: IAnalysisResult = {
  id: 21035,
  summary: 'notarize',
  sentiment: 'NEGATIVE',
  topic: 'fatally',
  entities: 'putrefy majority wing',
  riskSource: 'intently yowza where',
  status: 'SUCCESS',
  modelName: 'scrap better',
  analyzedAt: dayjs('2026-06-22T04:17'),
  errorMessage: 'grandpa',
};

export const sampleWithNewData: NewAnalysisResult = {
  sentiment: 'POSITIVE',
  status: 'SUCCESS',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
