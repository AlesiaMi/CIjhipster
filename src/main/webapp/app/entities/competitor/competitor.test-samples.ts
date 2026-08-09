import { ICompetitor, NewCompetitor } from './competitor.model';

export const sampleWithRequiredData: ICompetitor = {
  id: 11279,
  competitorName: 'corrupt spiffy',
  isActive: false,
};

export const sampleWithPartialData: ICompetitor = {
  id: 23621,
  competitorName: 'unlike',
  websiteUrl: 'slip scale',
  isActive: false,
};

export const sampleWithFullData: ICompetitor = {
  id: 9429,
  competitorName: 'amidst',
  websiteUrl: 'bah',
  industry: 'consequently jubilantly',
  description: 'down proceed',
  isActive: true,
};

export const sampleWithNewData: NewCompetitor = {
  competitorName: 'ack censor near',
  isActive: true,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
