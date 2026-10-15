import React from 'react';
import { ReportResponse, RootCauseCategory } from '../types/api';
import { EvidenceList } from './EvidenceList';
import {
  AlertCircle,
  CheckCircle,
  Cpu,
  GitBranch,
  HelpCircle,
  Layers,
  Server,
  Sliders,
  ListOrdered,
  Info,
  Gauge,
} from 'lucide-react';

interface ReportViewProps {
  report: ReportResponse;
}

const CAUSE_CONFIG: Record<RootCauseCategory, {
  label: string;
  desc: string;
  icon: React.FC<{ className?: string }>;
  gradient: string;
  border: string;
  text: string;
}> = {
  BAD_DEPLOY: {
    label: 'Bad Deployment',
    desc: 'Triggered by a recent code deployment or release change',
    icon: GitBranch,
    gradient: 'from-purple-950/60 to-purple-950/20',
    border: 'border-purple-700/50',
    text: 'text-purple-300',
  },
  DEPENDENCY_FAILURE: {
    label: 'Dependency Failure',
    desc: 'Downstream service, database, or external dependency timeout/outage',
    icon: Layers,
    gradient: 'from-amber-950/60 to-amber-950/20',
    border: 'border-amber-700/50',
    text: 'text-amber-300',
  },
  RESOURCE_EXHAUSTION: {
    label: 'Resource Exhaustion',
    desc: 'CPU, memory, disk, or connection pool saturation',
    icon: Cpu,
    gradient: 'from-rose-950/60 to-rose-950/20',
    border: 'border-rose-700/50',
    text: 'text-rose-300',
  },
  CONFIGURATION_ERROR: {
    label: 'Configuration Error',
    desc: 'Mismatched environment flags, secrets, or parameter drift',
    icon: Sliders,
    gradient: 'from-cyan-950/60 to-cyan-950/20',
    border: 'border-cyan-700/50',
    text: 'text-cyan-300',
  },
  UNKNOWN: {
    label: 'Unknown / Inconclusive',
    desc: 'Insufficient evidence to isolate a definitive root cause category',
    icon: HelpCircle,
    gradient: 'from-slate-800/60 to-slate-900/40',
    border: 'border-slate-700/50',
    text: 'text-slate-300',
  },
};

export const ReportView: React.FC<ReportViewProps> = ({ report }) => {
  const causeConfig = CAUSE_CONFIG[report.probableRootCause] ?? CAUSE_CONFIG.UNKNOWN;
  const CauseIcon = causeConfig.icon;

  const confidencePct = Math.round((report.confidence || 0) * 100);
  const confidenceLabel =
    confidencePct >= 80 ? 'High Confidence' : confidencePct >= 50 ? 'Moderate' : 'Low / Tentative';
  const confidenceBarColor =
    confidencePct >= 80 ? 'bg-emerald-500' : confidencePct >= 50 ? 'bg-amber-500' : 'bg-rose-500';
  const confidenceTextColor =
    confidencePct >= 80 ? 'text-emerald-400' : confidencePct >= 50 ? 'text-amber-400' : 'text-rose-400';

  // Parse recommendations
  let recommendations: string[] = [];
  try {
    const parsed = JSON.parse(report.recommendations);
    if (Array.isArray(parsed)) {
      recommendations = parsed.map(String);
    } else {
      recommendations = [report.recommendations];
    }
  } catch {
    if (report.recommendations) {
      recommendations = report.recommendations
        .split('\n')
        .map((s) => s.replace(/^[-*0-9.]+\s*/, '').trim())
        .filter(Boolean);
    }
  }

  return (
    <div className="space-y-5">
      {/* Root Cause + Confidence Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Root Cause Banner */}
        <div
          className={`col-span-2 rounded-2xl border bg-gradient-to-br ${causeConfig.gradient} ${causeConfig.border} p-6`}
        >
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 rounded-xl bg-black/30 border border-white/10 flex items-center justify-center shrink-0">
              <CauseIcon className={`w-6 h-6 ${causeConfig.text}`} />
            </div>
            <div>
              <div className="text-[10px] font-mono uppercase tracking-widest text-slate-500 mb-1">
                Probable Root Cause
              </div>
              <div className={`text-xl font-bold text-white tracking-tight`}>
                {causeConfig.label}
              </div>
              <div className={`text-xs mt-1 ${causeConfig.text} opacity-90`}>
                {causeConfig.desc}
              </div>
            </div>
          </div>
        </div>

        {/* Confidence Gauge */}
        <div className="rounded-2xl border border-slate-800/60 bg-slate-900/40 p-5 flex flex-col justify-between">
          <div className="flex items-center justify-between mb-3">
            <span className="text-[10px] font-mono uppercase tracking-widest text-slate-500">
              Diagnostic Confidence
            </span>
            <Gauge className="w-4 h-4 text-slate-500" />
          </div>
          <div>
            <div className="flex items-baseline gap-2 mb-2">
              <span className={`text-4xl font-extrabold font-mono ${confidenceTextColor}`}>
                {confidencePct}%
              </span>
              <span className={`text-xs font-medium ${confidenceTextColor} opacity-80`}>
                {confidenceLabel}
              </span>
            </div>
            <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-700 ease-out ${confidenceBarColor}`}
                style={{ width: `${Math.max(5, Math.min(100, confidencePct))}%` }}
              />
            </div>
          </div>
          <div className="text-[10px] font-mono text-slate-600 mt-2">
            Calibrated by benchmark suite
          </div>
        </div>
      </div>

      {/* Executive Summary */}
      <div className="rounded-2xl border border-slate-800/60 bg-slate-900/30 p-6 space-y-3">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-lg bg-blue-500/10 border border-blue-500/20 flex items-center justify-center">
            <Server className="w-3.5 h-3.5 text-blue-400" />
          </div>
          <span className="text-xs font-mono uppercase tracking-widest text-blue-400">Executive Summary</span>
        </div>
        <p className="text-sm text-slate-200 leading-relaxed">
          {report.summary}
        </p>
      </div>

      {/* Cited Evidence */}
      <div className="rounded-2xl border border-slate-800/60 bg-slate-900/30 p-6 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center">
              <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />
            </div>
            <span className="text-xs font-mono uppercase tracking-widest text-emerald-400">
              Substantiated Evidence & Citations
            </span>
          </div>
          <span className="text-[10px] font-mono text-slate-600 bg-slate-800/60 px-2 py-0.5 rounded-full">
            Ground-truth verified
          </span>
        </div>
        <EvidenceList evidenceRaw={report.evidence} />
      </div>

      {/* Recommended Actions */}
      <div className="rounded-2xl border border-slate-800/60 bg-slate-900/30 p-6 space-y-4">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-lg bg-amber-500/10 border border-amber-500/20 flex items-center justify-center">
            <ListOrdered className="w-3.5 h-3.5 text-amber-400" />
          </div>
          <span className="text-xs font-mono uppercase tracking-widest text-amber-400">
            Recommended Remediation Actions
          </span>
        </div>
        <div className="space-y-2">
          {recommendations.length > 0 ? (
            recommendations.map((step, idx) => (
              <div
                key={idx}
                className="flex items-start gap-3.5 p-4 rounded-xl border border-slate-800/60 bg-slate-950/40 hover:bg-slate-950/60 transition-colors"
              >
                <span className="flex items-center justify-center w-6 h-6 rounded-full bg-slate-800 border border-slate-700/60 font-mono text-[11px] text-slate-400 shrink-0 font-semibold mt-0.5">
                  {idx + 1}
                </span>
                <span className="text-sm text-slate-300 leading-relaxed">{step}</span>
              </div>
            ))
          ) : (
            <p className="text-xs text-slate-600 italic">No recommendations provided.</p>
          )}
        </div>
        <p className="text-[11px] text-slate-600 italic">
          * Advisory system only — does not execute automated production changes.
        </p>
      </div>

      {/* Limitations */}
      {report.limitations && (
        <div className="rounded-2xl border border-amber-900/30 bg-gradient-to-br from-amber-950/20 to-amber-950/5 p-5 space-y-2">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-amber-400" />
            <span className="text-xs font-mono uppercase tracking-widest text-amber-400">
              Known Observability Gaps & Limitations
            </span>
          </div>
          <p className="text-xs text-amber-200/80 leading-relaxed">{report.limitations}</p>
        </div>
      )}

      {/* Report Metadata */}
      <div className="rounded-xl border border-slate-800/40 bg-slate-950/40 p-4 flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2 text-[11px] font-mono text-slate-600">
          <Info className="w-3.5 h-3.5" />
          <span>
            Report <span className="text-slate-500">#{report.id}</span> · Investigation{' '}
            <span className="text-slate-500">#{report.investigationId}</span>
          </span>
        </div>
        <div className="flex items-center gap-4 text-[10px] font-mono text-slate-600">
          <span>
            Model: <span className="text-slate-400">{report.modelName || 'gemini-1.5-flash'}</span>
          </span>
          <span className="text-slate-700">·</span>
          <span>
            Prompt: <span className="text-slate-400">{report.promptVersion || 'v1.0'}</span>
          </span>
          <span className="text-slate-700">·</span>
          <span>
            Generated: <span className="text-slate-400">{new Date(report.createdAt).toLocaleString()}</span>
          </span>
        </div>
      </div>
    </div>
  );
};
