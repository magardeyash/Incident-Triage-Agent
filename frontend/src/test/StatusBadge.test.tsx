import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StatusBadge } from '../components/StatusBadge';

describe('StatusBadge', () => {
  it('renders COMPLETED badge correctly', () => {
    render(<StatusBadge status="COMPLETED" />);
    expect(screen.getByText('COMPLETED')).toBeInTheDocument();
  });

  it('renders RUNNING badge correctly', () => {
    render(<StatusBadge status="RUNNING" />);
    expect(screen.getByText('RUNNING')).toBeInTheDocument();
  });

  it('renders FAILED badge correctly', () => {
    render(<StatusBadge status="FAILED" />);
    expect(screen.getByText('FAILED')).toBeInTheDocument();
  });

  it('renders INCONCLUSIVE badge correctly', () => {
    render(<StatusBadge status="INCONCLUSIVE" />);
    expect(screen.getByText('INCONCLUSIVE')).toBeInTheDocument();
  });
});
