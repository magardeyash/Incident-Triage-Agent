import React from 'react';
import { Loader2 } from 'lucide-react';

interface LoadingStateProps {
  message?: string;
  subtext?: string;
  className?: string;
}

export const LoadingState: React.FC<LoadingStateProps> = ({
  message = 'Loading incident data...',
  subtext,
  className = '',
}) => {
  return (
    <div
      className={`flex flex-col items-center justify-center p-14 text-center rounded-2xl border border-slate-800/60 bg-slate-900/30 ${className}`}
    >
      <div className="relative mb-5">
        {/* Outer ring */}
        <div className="w-14 h-14 rounded-full border-2 border-blue-500/15 border-t-blue-500 animate-spin" />
        {/* Middle ring */}
        <div className="absolute inset-1 rounded-full border-2 border-slate-700/50 border-b-slate-500 animate-spin [animation-direction:reverse] [animation-duration:1.2s]" />
        {/* Center icon */}
        <Loader2 className="w-5 h-5 text-blue-400 absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 animate-pulse" />
      </div>
      <p className="text-sm font-semibold text-slate-200 tracking-tight">{message}</p>
      {subtext && (
        <p className="text-xs text-slate-500 mt-2 max-w-sm leading-relaxed">{subtext}</p>
      )}
      {/* Shimmer dots */}
      <div className="flex gap-1.5 mt-4">
        {[0, 1, 2].map((i) => (
          <span
            key={i}
            className="w-1.5 h-1.5 rounded-full bg-blue-500/60 animate-pulse"
            style={{ animationDelay: `${i * 200}ms` }}
          />
        ))}
      </div>
    </div>
  );
};
