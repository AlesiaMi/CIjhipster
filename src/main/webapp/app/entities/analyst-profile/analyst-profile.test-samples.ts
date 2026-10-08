import dayjs from 'dayjs/esm';

import { IAnalystProfile, NewAnalystProfile } from './analyst-profile.model';

export const sampleWithRequiredData: IAnalystProfile = {
  id: 29441,
  displayName: 'toward fail',
  notificationEnabled: true,
  createdAt: dayjs('2026-06-22T09:52'),
};

export const sampleWithPartialData: IAnalystProfile = {
  id: 28397,
  displayName: 'saturate pish',
  telegramChatId: 'massive resort',
  notificationEnabled: true,
  createdAt: dayjs('2026-06-22T03:50'),
};

export const sampleWithFullData: IAnalystProfile = {
  id: 17050,
  displayName: 'barring discourse stitcher',
  telegramChatId: 'amidst',
  notificationEnabled: false,
  createdAt: dayjs('2026-06-22T13:05'),
};

export const sampleWithNewData: NewAnalystProfile = {
  displayName: 'upbeat tuxedo',
  notificationEnabled: true,
  createdAt: dayjs('2026-06-22T01:08'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
