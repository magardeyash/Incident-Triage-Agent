import React from 'react';
import { Link } from 'react-router-dom';
import { AlertCircle, ArrowLeft, Home, Compass } from 'lucide-react';

export const NotFoundPage: React.FC = () => {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] px-4 py-16 text-center animate-fade-in-up">
      {/* Visual icon container with ambient glow */}
      <div className="relative mb-6">
        <div className="w-16 h-16 rounded-2xl bg-rose-950/40 border border-rose-800/40 flex items-center justify-center text-rose-400">
          <AlertCircle className="w-8 h-8" />
        </div>
        <div className="absolute inset-0 rounded-2xl bg-rose-500/10 blur-xl -z-10" />
      </div>

      <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full border border-slate-800 bg-slate-900/60 text-slate-400 text-xs font-mono mb-3">
        <Compass className="w-3.5 h-3.5 text-blue-400" />
        <span>HTTP 404 · Resource Not Found</span>
      </div>

      <h1 className="text-3xl font-extrabold text-white tracking-tight sm:text-4xl">
        Page Not Found
      </h1>
      
      <p className="text-sm text-slate-400 mt-3 max-w-md leading-relaxed">
        The route you are navigating to does not exist or has been relocated within the autonomous incident triage platform.
      </p>

      <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
        <Link
          to="/"
          className="inline-flex items-center gap-2 px-5 py-2.5 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-500 rounded-xl shadow-[0_0_16px_rgba(37,99,235,0.35)] hover:shadow-[0_0_20px_rgba(37,99,235,0.5)] transition-all duration-200"
        >
          <Home className="w-3.5 h-3.5" />
          <span>Operations Dashboard</span>
        </Link>
        <button
          onClick={() => window.history.back()}
          className="inline-flex items-center gap-2 px-4 py-2.5 text-xs font-medium text-slate-300 bg-slate-900/80 hover:bg-slate-800 border border-slate-800 hover:border-slate-700 rounded-xl transition-all duration-200"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Go Back</span>
        </button>
      </div>
    </div>
  );
};
