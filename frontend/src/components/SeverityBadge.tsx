import React from 'react';
import { AlertSeverity } from '../types/api';

interface SeverityBadgeProps {
  severity: AlertSeverity | string;
  size?: 'sm' | 'md';
}

const SEVERITY_STYLES: Record<string, { classes: string; dot: string }> = {
  CRITICAL: {
    classes: 'bg-red-500/10 text-red-400 border-red-500/30',
    dot: 'bg-red-500',
  },
  HIGH: {
    classes: 'bg-orange-500/10 text-orange-400 border-orange-500/30',
    dot: 'bg-orange-400',
  },
  MEDIUM: {
    classes: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
    dot: 'bg-amber-400',
  },
  LOW: {
    classes: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
    dot: 'bg-blue-400',
  },
};

export const SeverityBadge: React.FC<SeverityBadgeProps> = ({ severity, size = 'md' }) => {
  const norm = (severity || '').toString().toUpperCase();
  const style = SEVERITY_STYLES[norm] ?? {
    classes: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
    dot: 'bg-slate-400',
  };

  const sizeClass =
    size === 'sm'
      ? 'text-[10px] px-1.5 py-0.5 gap-1'
      : 'text-xs px-2.5 py-0.5 gap-1.5';

  return (
    <span
      className={`inline-flex items-center font-mono font-semibold rounded border ${style.classes} ${sizeClass}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${style.dot} shrink-0`} />
      {norm || 'UNKNOWN'}
    </span>
  );
};
