import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ReportView } from '../components/ReportView';
import { ReportResponse } from '../types/api';

const mockReport: ReportResponse = {
  id: 42,
  investigationId: 101,
  summary: 'Checkout service failed due to downstream timeout in payment-service.',
  probableRootCause: 'DEPENDENCY_FAILURE',
  confidence: 0.95,
  evidence: JSON.stringify(['EVID-LOG-chk-003', 'EVID-DEP-pay-001']),
  recommendations: JSON.stringify([
    'Scale up payment-service replicas',
    'Verify circuit breaker fallback in checkout-service',
  ]),
  limitations: 'Downstream payment gateway internal metrics were unavailable.',
  modelName: 'gemini-1.5-flash',
  promptVersion: 'v1.0',
  createdAt: '2026-10-15T12:00:00Z',
};

describe('ReportView', () => {
  it('renders root cause and confidence percentage', () => {
    render(<ReportView report={mockReport} />);
    expect(screen.getByText('Dependency Failure')).toBeInTheDocument();
    expect(screen.getByText('95%')).toBeInTheDocument();
  });

  it('renders executive summary', () => {
    render(<ReportView report={mockReport} />);
    expect(
      screen.getByText(/Checkout service failed due to downstream timeout/i)
    ).toBeInTheDocument();
  });

  it('renders evidence IDs and recommendations', () => {
    render(<ReportView report={mockReport} />);
    expect(screen.getByText('EVID-LOG-chk-003')).toBeInTheDocument();
    expect(screen.getByText('EVID-DEP-pay-001')).toBeInTheDocument();
    expect(screen.getByText('Scale up payment-service replicas')).toBeInTheDocument();
    expect(
      screen.getByText('Verify circuit breaker fallback in checkout-service')
    ).toBeInTheDocument();
  });

  it('renders limitations when provided', () => {
    render(<ReportView report={mockReport} />);
    expect(
      screen.getByText(/Downstream payment gateway internal metrics were unavailable/i)
    ).toBeInTheDocument();
  });
});
