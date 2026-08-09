import dayjs from 'dayjs/esm';

import { RunStatus } from 'app/entities/enumerations/run-status.model';

export interface ICollectionRun {
  id: number;
  startedAt?: dayjs.Dayjs | null;
  finishedAt?: dayjs.Dayjs | null;
  status?: keyof typeof RunStatus | null;
  foundCount?: number | null;
  processedCount?: number | null;
  errorMessage?: string | null;
}

export type NewCollectionRun = Omit<ICollectionRun, 'id'> & { id: null };
