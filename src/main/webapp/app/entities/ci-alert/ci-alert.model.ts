import dayjs from 'dayjs/esm';

import { IAnalysisResult } from 'app/entities/analysis-result/analysis-result.model';
import { IAnalystProfile } from 'app/entities/analyst-profile/analyst-profile.model';
import { AlertStatus } from 'app/entities/enumerations/alert-status.model';
import { Severity } from 'app/entities/enumerations/severity.model';

export interface ICiAlert {
  id: number;
  title?: string | null;
  message?: string | null;
  severity?: keyof typeof Severity | null;
  status?: keyof typeof AlertStatus | null;
  createdAt?: dayjs.Dayjs | null;
  readAt?: dayjs.Dayjs | null;
  analysisResult?: Pick<IAnalysisResult, 'id'> | null;
  analystProfile?: Pick<IAnalystProfile, 'id' | 'displayName'> | null;
}

export type NewCiAlert = Omit<ICiAlert, 'id'> & { id: null };
