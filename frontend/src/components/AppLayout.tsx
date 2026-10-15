import React from 'react';
import { Outlet } from 'react-router-dom';
import { AppHeader } from './AppHeader';
import { Shield, Zap, Database } from 'lucide-react';

export const AppLayout: React.FC = () => {
  return (
    <div className="min-h-screen flex flex-col bg-[#05090f] text-slate-100 grid-bg">
      <AppHeader />
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in-up">
        <Outlet />
      </main>
      <footer className="border-t border-slate-800/40 bg-[#03060b]/80 py-5">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-3 text-[11px] text-slate-500">
            <div className="flex items-center gap-4">
              <span className="font-mono tracking-wider">
                Incident Triage Agent · Autonomous Production Root Cause Analysis
              </span>
            </div>
            <div className="flex items-center gap-4 font-mono">
              <span className="flex items-center gap-1.5">
                <Shield className="w-3 h-3 text-blue-500" />
                <span className="text-slate-400">Anti-Hallucination Grounding</span>
              </span>
              <span className="text-slate-700">·</span>
              <span className="flex items-center gap-1.5">
                <Database className="w-3 h-3 text-emerald-500" />
                <span className="text-slate-400">pgvector RAG</span>
              </span>
              <span className="text-slate-700">·</span>
              <span className="flex items-center gap-1.5">
                <Zap className="w-3 h-3 text-purple-500" />
                <span className="text-slate-400">Gemini 1.5</span>
              </span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};
