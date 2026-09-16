import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';

import { RunStatus } from 'app/entities/enumerations/run-status.model';

export interface ICollectionRun {
  id: number;
  startedAt?: dayjs.Dayjs | null;
  finishedAt?: dayjs.Dayjs | null;
  status?: keyof typeof RunStatus | null;
  foundCount?: number | null;
  processedCount?: number | null;
  errorMessage?: string | null;
  owner?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewCollectionRun = Omit<ICollectionRun, 'id'> & { id: null };
