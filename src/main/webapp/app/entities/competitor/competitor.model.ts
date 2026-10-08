import { IAnalystProfile } from 'app/entities/analyst-profile/analyst-profile.model';
import { IUser } from 'app/entities/user/user.model';

export interface ICompetitor {
  id: number;
  competitorName?: string | null;
  websiteUrl?: string | null;
  industry?: string | null;
  description?: string | null;
  isActive?: boolean | null;
  owner?: Pick<IUser, 'id' | 'login'> | null;
  analystProfileses?: Pick<IAnalystProfile, 'id' | 'displayName'>[] | null;
}

export type NewCompetitor = Omit<ICompetitor, 'id'> & { id: null };
