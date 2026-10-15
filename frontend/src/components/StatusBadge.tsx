import React from 'react';
import { InvestigationStatus } from '../types/api';
import { Clock, RefreshCw, CheckCircle2, AlertTriangle, XCircle } from 'lucide-react';

interface StatusBadgeProps {
  status: InvestigationStatus;
  size?: 'sm' | 'md' | 'lg';
  showIcon?: boolean;
}

const STATUS_CONFIG: Record<InvestigationStatus, {
  label: string;
  classes: string;
  dotClass: string;
  icon: React.FC<{ className?: string }>;
  spin?: boolean;
}> = {
  PENDING: {
    label: 'PENDING',
    classes: 'bg-amber-950/50 border-amber-600/40 text-amber-300',
    dotClass: 'bg-amber-400',
    icon: Clock,
  },
  RUNNING: {
    label: 'RUNNING',
    classes: 'bg-sky-950/50 border-sky-500/40 text-sky-300',
    dotClass: 'bg-sky-400 animate-pulse',
    icon: RefreshCw,
    spin: true,
  },
  COMPLETED: {
    label: 'COMPLETED',
    classes: 'bg-emerald-950/50 border-emerald-500/40 text-emerald-300',
    dotClass: 'bg-emerald-400',
    icon: CheckCircle2,
  },
  INCONCLUSIVE: {
    label: 'INCONCLUSIVE',
    classes: 'bg-orange-950/50 border-orange-500/40 text-orange-300',
    dotClass: 'bg-orange-400',
    icon: AlertTriangle,
  },
  FAILED: {
    label: 'FAILED',
    classes: 'bg-rose-950/50 border-rose-500/40 text-rose-300',
    dotClass: 'bg-rose-400',
    icon: XCircle,
  },
};

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  size = 'md',
  showIcon = true,
}) => {
  const config = STATUS_CONFIG[status] ?? {
    label: status,
    classes: 'bg-slate-800 border-slate-700 text-slate-300',
    dotClass: 'bg-slate-400',
    icon: Clock,
  };

  const Icon = config.icon;

  const sizeClasses = {
    sm: 'text-[10px] px-2 py-0.5 gap-1',
    md: 'text-xs px-2.5 py-1 gap-1.5',
    lg: 'text-sm px-3 py-1.5 gap-2',
  }[size];

  return (
    <span
      className={`inline-flex items-center rounded-md border font-mono tracking-wider font-medium ${config.classes} ${sizeClasses}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${config.dotClass}`} />
      {showIcon && (
        <Icon className={`w-3 h-3 shrink-0 ${config.spin ? 'animate-spin' : ''}`} />
      )}
      <span>{config.label}</span>
    </span>
  );
};
