import dayjs from 'dayjs/esm';

import { IDataSource, NewDataSource } from './data-source.model';

export const sampleWithRequiredData: IDataSource = {
  id: 24507,
  sourceName: 'amid sleepily',
  url: 'https://lovable-airport.biz',
  sourceType: 'WEBSITE',
  isActive: false,
  createdAt: dayjs('2026-06-22T13:12'),
};

export const sampleWithPartialData: IDataSource = {
  id: 13593,
  sourceName: 'drat anneal steeple',
  url: 'https://separate-pneumonia.net',
  sourceType: 'API',
  isActive: false,
  lastCheckedAt: dayjs('2026-06-22T16:03'),
  createdAt: dayjs('2026-06-22T14:56'),
};

export const sampleWithFullData: IDataSource = {
  id: 25445,
  sourceName: 'raw',
  url: 'https://interesting-pillow.name/',
  sourceType: 'WEBSITE',
  isActive: false,
  lastCheckedAt: dayjs('2026-06-22T18:56'),
  createdAt: dayjs('2026-06-22T12:20'),
};

export const sampleWithNewData: NewDataSource = {
  sourceName: 'bowling',
  url: 'https://multicolored-dish.org/',
  sourceType: 'API',
  isActive: false,
  createdAt: dayjs('2026-06-22T04:20'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
