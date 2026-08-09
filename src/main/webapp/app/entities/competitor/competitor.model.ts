import { IAnalystProfile } from 'app/entities/analyst-profile/analyst-profile.model';

export interface ICompetitor {
  id: number;
  competitorName?: string | null;
  websiteUrl?: string | null;
  industry?: string | null;
  description?: string | null;
  isActive?: boolean | null;
  analystProfileses?: Pick<IAnalystProfile, 'id' | 'displayName'>[] | null;
}

export type NewCompetitor = Omit<ICompetitor, 'id'> & { id: null };
