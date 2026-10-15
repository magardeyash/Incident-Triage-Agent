import {
  CreateAlertRequest,
  CreateAlertResponse,
  AlertResponse,
  InvestigationResponse,
  ReportResponse,
  EvaluationSummary,
  RunbookSearchResult,
} from '../types/api';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export class ApiError extends Error {
  status: number;
  data: any;

  constructor(message: string, status: number, data?: any) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let errorData: any = null;
    let message = `Request failed with status ${response.status}`;
    try {
      errorData = await response.json();
      if (errorData.message) {
        message = errorData.message;
      } else if (errorData.error) {
        message = errorData.error;
      }
    } catch {
      // not JSON
    }
    throw new ApiError(message, response.status, errorData);
  }
  return response.json() as Promise<T>;
}

export const api = {
  async submitAlert(request: CreateAlertRequest): Promise<CreateAlertResponse> {
    const res = await fetch(`${BASE_URL}/api/alerts`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });
    return handleResponse<CreateAlertResponse>(res);
  },

  async getAlert(id: number): Promise<AlertResponse> {
    const res = await fetch(`${BASE_URL}/api/alerts/${id}`, {
      headers: { 'Content-Type': 'application/json' },
    });
    return handleResponse<AlertResponse>(res);
  },

  async listInvestigations(): Promise<InvestigationResponse[]> {
    const res = await fetch(`${BASE_URL}/api/investigations`, {
      headers: { 'Content-Type': 'application/json' },
    });
    return handleResponse<InvestigationResponse[]>(res);
  },

  async getInvestigation(id: number): Promise<InvestigationResponse> {
    const res = await fetch(`${BASE_URL}/api/investigations/${id}`, {
      headers: { 'Content-Type': 'application/json' },
    });
    return handleResponse<InvestigationResponse>(res);
  },

  async getReport(investigationId: number): Promise<ReportResponse> {
    const res = await fetch(`${BASE_URL}/api/investigations/${investigationId}/report`, {
      headers: { 'Content-Type': 'application/json' },
    });
    return handleResponse<ReportResponse>(res);
  },

  async runEvaluation(): Promise<EvaluationSummary> {
    const res = await fetch(`${BASE_URL}/api/evaluations/run`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
    });
    return handleResponse<EvaluationSummary>(res);
  },

  async searchRunbooks(
    query: string,
    topK = 3,
    minSimilarity = 0.3
  ): Promise<RunbookSearchResult[]> {
    const params = new URLSearchParams({
      q: query,
      topK: topK.toString(),
      minSimilarity: minSimilarity.toString(),
    });
    const res = await fetch(`${BASE_URL}/api/runbooks/search?${params.toString()}`, {
      headers: { 'Content-Type': 'application/json' },
    });
    return handleResponse<RunbookSearchResult[]>(res);
  },
};
