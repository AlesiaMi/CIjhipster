import dayjs from 'dayjs/esm';

import { ICollectionRun } from 'app/entities/collection-run/collection-run.model';
import { ICompetitor } from 'app/entities/competitor/competitor.model';
import { IDataSource } from 'app/entities/data-source/data-source.model';

export interface INewsItem {
  id: number;
  externalId?: string | null;
  title?: string | null;
  url?: string | null;
  originalText?: string | null;
  publishedAt?: dayjs.Dayjs | null;
  collectedAt?: dayjs.Dayjs | null;
  isDuplicate?: boolean | null;
  dataSource?: Pick<IDataSource, 'id' | 'sourceName'> | null;
  competitor?: Pick<ICompetitor, 'id' | 'competitorName'> | null;
  collectionRun?: Pick<ICollectionRun, 'id'> | null;
}

export type NewNewsItem = Omit<INewsItem, 'id'> & { id: null };
