import dayjs from 'dayjs/esm';

import { ICompetitor } from 'app/entities/competitor/competitor.model';
import { IUser } from 'app/entities/user/user.model';

export interface IAnalystProfile {
  id: number;
  displayName?: string | null;
  telegramChatId?: string | null;
  notificationEnabled?: boolean | null;
  createdAt?: dayjs.Dayjs | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
  competitorses?: Pick<ICompetitor, 'id' | 'competitorName'>[] | null;
}

export type NewAnalystProfile = Omit<IAnalystProfile, 'id'> & { id: null };
