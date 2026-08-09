import dayjs from 'dayjs/esm';

import { ICiAlert, NewCiAlert } from './ci-alert.model';

export const sampleWithRequiredData: ICiAlert = {
  id: 28921,
  title: 'gah',
  message: 'sedately around',
  severity: 'MEDIUM',
  status: 'ARCHIVED',
  createdAt: dayjs('2026-06-22T10:05'),
};

export const sampleWithPartialData: ICiAlert = {
  id: 14015,
  title: 'oh while',
  message: 'meager down really',
  severity: 'LOW',
  status: 'NEW',
  createdAt: dayjs('2026-06-22T07:21'),
  readAt: dayjs('2026-06-22T01:51'),
};

export const sampleWithFullData: ICiAlert = {
  id: 21201,
  title: 'misreport always',
  message: 'woot',
  severity: 'LOW',
  status: 'ARCHIVED',
  createdAt: dayjs('2026-06-21T23:54'),
  readAt: dayjs('2026-06-22T03:12'),
};

export const sampleWithNewData: NewCiAlert = {
  title: 'by yet known',
  message: 'pfft upliftingly futon',
  severity: 'MEDIUM',
  status: 'ARCHIVED',
  createdAt: dayjs('2026-06-22T00:35'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
