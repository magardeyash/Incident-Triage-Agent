import React from 'react';
import { Inbox, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

interface EmptyStateProps {
  title: string;
  description: string;
  actionText?: string;
  actionHref?: string;
  icon?: React.ReactNode;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  description,
  actionText,
  actionHref,
  icon,
}) => {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-8 text-center rounded-2xl border border-dashed border-slate-800/80 bg-slate-900/20">
      <div className="relative mb-5">
        <div className="w-14 h-14 rounded-2xl bg-slate-800/60 border border-slate-700/40 flex items-center justify-center text-slate-400">
          {icon || <Inbox className="w-6 h-6" />}
        </div>
        <div className="absolute inset-0 rounded-2xl bg-blue-500/5 blur-xl" />
      </div>
      <h3 className="text-base font-semibold text-slate-200 tracking-tight">{title}</h3>
      <p className="text-sm text-slate-500 mt-2 max-w-sm leading-relaxed">{description}</p>
      {actionText && actionHref && (
        <Link
          to={actionHref}
          className="mt-6 inline-flex items-center gap-2 px-5 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-500 rounded-xl shadow-[0_0_16px_rgba(37,99,235,0.4)] hover:shadow-[0_0_20px_rgba(37,99,235,0.6)] transition-all duration-200"
        >
          {actionText}
          <ArrowRight className="w-3.5 h-3.5" />
        </Link>
      )}
    </div>
  );
};
