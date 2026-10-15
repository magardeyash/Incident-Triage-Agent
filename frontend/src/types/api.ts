export type InvestigationStatus =
  | 'PENDING'
  | 'RUNNING'
  | 'COMPLETED'
  | 'INCONCLUSIVE'
  | 'FAILED';

export type RootCauseCategory =
  | 'BAD_DEPLOY'
  | 'DEPENDENCY_FAILURE'
  | 'RESOURCE_EXHAUSTION'
  | 'CONFIGURATION_ERROR'
  | 'UNKNOWN';

export type AlertSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface CreateAlertRequest {
  service: string;
  severity: AlertSeverity | string;
  message: string;
  timestamp?: string;
  payload?: string;
}

export interface CreateAlertResponse {
  alertId: number;
  investigationId: number;
  status: InvestigationStatus;
}

export interface AlertResponse {
  id: number;
  serviceName: string;
  severity: string;
  message: string;
  occurredAt: string;
  payload: string | null;
  createdAt: string;
}

export interface InvestigationResponse {
  id: number;
  alertId: number;
  status: InvestigationStatus;
  startedAt: string | null;
  finishedAt: string | null;
  errorMessage: string | null;
  createdAt: string;
}

export interface ReportResponse {
  id: number;
  investigationId: number;
  summary: string;
  probableRootCause: RootCauseCategory;
  confidence: number;
  evidence: string; // JSON string or raw text
  recommendations: string; // JSON string or raw text
  limitations: string | null;
  modelName: string | null;
  promptVersion: string | null;
  createdAt: string;
}

export interface EvaluationResult {
  scenarioId: string;
  scenarioTitle: string;
  expectedCategory: RootCauseCategory;
  actualCategory: RootCauseCategory;
  categoryCorrect: boolean;
  citationValidityRate: number;
  evidenceCoverage: number;
  confidence: number;
  uncertaintyAppropriate: boolean;
  promptInjectionSafeguarded: boolean;
  durationMs: number;
  summaryNotes: string;
}

export interface EvaluationSummary {
  totalScenarios: number;
  passedScenarios: number;
  accuracyRate: number;
  averageCitationValidity: number;
  averageEvidenceCoverage: number;
  promptInjectionDefenseRate: number;
  totalDurationMs: number;
  scenarioResults: EvaluationResult[];
}

export interface RunbookSearchResult {
  title: string;
  service: string;
  content: string;
  similarity: number;
}
