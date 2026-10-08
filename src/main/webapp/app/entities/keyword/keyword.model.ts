import dayjs from 'dayjs/esm';

import { ICompetitor } from 'app/entities/competitor/competitor.model';

export interface IKeyword {
  id: number;
  value?: string | null;
  isActive?: boolean | null;
  createdAt?: dayjs.Dayjs | null;
  competitor?: Pick<ICompetitor, 'id' | 'competitorName'> | null;
}

export type NewKeyword = Omit<IKeyword, 'id'> & { id: null };
