import dayjs from 'dayjs/esm';

import { INewsItem, NewNewsItem } from './news-item.model';

export const sampleWithRequiredData: INewsItem = {
  id: 18758,
  title: 'carelessly',
  url: 'https://curly-siege.name/',
  collectedAt: dayjs('2026-06-22T18:18'),
  isDuplicate: false,
};

export const sampleWithPartialData: INewsItem = {
  id: 24894,
  title: 'encode',
  url: 'https://superb-hunger.net',
  originalText: 'ravel solidly unnecessarily',
  publishedAt: dayjs('2026-06-22T13:17'),
  collectedAt: dayjs('2026-06-22T12:45'),
  isDuplicate: true,
};

export const sampleWithFullData: INewsItem = {
  id: 26321,
  externalId: 'pigpen shoddy',
  title: 'pro apropos deeply',
  url: 'https://sociable-gray.info',
  originalText: 'frightfully',
  publishedAt: dayjs('2026-06-21T22:38'),
  collectedAt: dayjs('2026-06-21T23:14'),
  isDuplicate: false,
};

export const sampleWithNewData: NewNewsItem = {
  title: 'superb restfully',
  url: 'https://excitable-vibration.info',
  collectedAt: dayjs('2026-06-22T05:43'),
  isDuplicate: false,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
