import dayjs from 'dayjs/esm';

import { IKeyword, NewKeyword } from './keyword.model';

export const sampleWithRequiredData: IKeyword = {
  id: 28392,
  value: 'under',
  isActive: false,
  createdAt: dayjs('2026-06-21T23:15'),
};

export const sampleWithPartialData: IKeyword = {
  id: 8342,
  value: 'tooth override bitter',
  isActive: false,
  createdAt: dayjs('2026-06-21T21:16'),
};

export const sampleWithFullData: IKeyword = {
  id: 1040,
  value: 'yarmulke',
  isActive: false,
  createdAt: dayjs('2026-06-22T13:23'),
};

export const sampleWithNewData: NewKeyword = {
  value: 'ditch beautifully barring',
  isActive: false,
  createdAt: dayjs('2026-06-22T17:26'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
