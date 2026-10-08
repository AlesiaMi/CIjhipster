import dayjs from 'dayjs/esm';

import { ICompetitor } from 'app/entities/competitor/competitor.model';
import { SourceType } from 'app/entities/enumerations/source-type.model';

export interface IDataSource {
  id: number;
  sourceName?: string | null;
  url?: string | null;
  sourceType?: keyof typeof SourceType | null;
  isActive?: boolean | null;
  lastCheckedAt?: dayjs.Dayjs | null;
  createdAt?: dayjs.Dayjs | null;
  competitor?: Pick<ICompetitor, 'id' | 'competitorName'> | null;
}

export type NewDataSource = Omit<IDataSource, 'id'> & { id: null };
