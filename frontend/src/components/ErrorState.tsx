import React from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';

interface ErrorStateProps {
  title?: string;
  message: string;
  onRetry?: () => void;
  className?: string;
}

export const ErrorState: React.FC<ErrorStateProps> = ({
  title = 'An error occurred',
  message,
  onRetry,
  className = '',
}) => {
  return (
    <div
      className={`rounded-2xl border border-rose-900/40 bg-gradient-to-br from-rose-950/30 to-rose-950/10 p-6 ${className}`}
    >
      <div className="flex items-start gap-4">
        <div className="w-9 h-9 rounded-xl bg-rose-950/60 border border-rose-800/50 flex items-center justify-center shrink-0">
          <AlertCircle className="w-4.5 h-4.5 text-rose-400" />
        </div>
        <div className="flex-1 min-w-0">
          <h3 className="text-sm font-semibold text-rose-300 tracking-tight">{title}</h3>
          <p className="text-xs text-rose-200/70 mt-1.5 font-mono break-all leading-relaxed">
            {message}
          </p>
          {onRetry && (
            <button
              onClick={onRetry}
              className="mt-4 inline-flex items-center gap-2 px-3 py-1.5 text-xs font-medium text-rose-300 bg-rose-900/30 hover:bg-rose-900/50 border border-rose-700/40 rounded-lg transition-all duration-200 hover:border-rose-600/60"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              Retry Request
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
