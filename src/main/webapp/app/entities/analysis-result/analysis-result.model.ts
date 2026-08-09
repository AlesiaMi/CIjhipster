import dayjs from 'dayjs/esm';

import { AnalysisStatus } from 'app/entities/enumerations/analysis-status.model';
import { Sentiment } from 'app/entities/enumerations/sentiment.model';
import { INewsItem } from 'app/entities/news-item/news-item.model';

export interface IAnalysisResult {
  id: number;
  summary?: string | null;
  sentiment?: keyof typeof Sentiment | null;
  topic?: string | null;
  entities?: string | null;
  riskSource?: string | null;
  status?: keyof typeof AnalysisStatus | null;
  modelName?: string | null;
  analyzedAt?: dayjs.Dayjs | null;
  errorMessage?: string | null;
  newsItem?: Pick<INewsItem, 'id' | 'title'> | null;
}

export type NewAnalysisResult = Omit<IAnalysisResult, 'id'> & { id: null };
