import React, { useState } from 'react';
import { api } from '../services/api';
import { EvaluationSummary } from '../types/api';
import { LoadingState } from '../components/LoadingState';
import { ErrorState } from '../components/ErrorState';
import {
  FlaskConical,
  Play,
  CheckCircle2,
  XCircle,
  ShieldCheck,
  Target,
  Clock,
  Sparkles,
  TrendingUp,
  Zap,
} from 'lucide-react';

export const EvaluationPage: React.FC = () => {
  const [running, setRunning] = useState(false);
  const [summary, setSummary] = useState<EvaluationSummary | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleRunEvaluation = async () => {
    setRunning(true);
    setError(null);
    try {
      const res = await api.runEvaluation();
      setSummary(res);
    } catch (err: any) {
      setError(err.message || 'Evaluation suite run failed');
    } finally {
      setRunning(false);
    }
  };

  const scorecards = summary
    ? [
        {
          label: 'Diagnosis Accuracy',
          value: `${Math.round(summary.accuracyRate * 100)}%`,
          sub: `${summary.passedScenarios} of ${summary.totalScenarios} scenarios matched`,
          icon: Target,
          color: 'text-purple-400',
          border: 'border-purple-800/50',
          bg: 'bg-purple-950/20',
          iconBg: 'bg-purple-500/10 border-purple-500/20',
        },
        {
          label: 'Citation Validity',
          value: `${Math.round(summary.averageCitationValidity * 100)}%`,
          sub: 'Zero hallucinated evidence IDs',
          icon: CheckCircle2,
          color: 'text-emerald-400',
          border: 'border-emerald-800/50',
          bg: 'bg-emerald-950/20',
          iconBg: 'bg-emerald-500/10 border-emerald-500/20',
        },
        {
          label: 'Injection Defense',
          value: `${Math.round(summary.promptInjectionDefenseRate * 100)}%`,
          sub: 'Adversarial jailbreaks rejected',
          icon: ShieldCheck,
          color: 'text-sky-400',
          border: 'border-sky-800/50',
          bg: 'bg-sky-950/20',
          iconBg: 'bg-sky-500/10 border-sky-500/20',
        },
        {
          label: 'Execution Time',
          value: `${(summary.totalDurationMs / 1000).toFixed(2)}s`,
          sub: 'Deterministic replay latency',
          icon: Clock,
          color: 'text-slate-300',
          border: 'border-slate-700/50',
          bg: 'bg-slate-800/30',
          iconBg: 'bg-slate-700/40 border-slate-600/30',
        },
      ]
    : [];

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 border-b border-slate-800/60 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <TrendingUp className="w-4 h-4 text-purple-400" />
            <span className="text-xs font-mono text-purple-400 uppercase tracking-wider">QA & Evaluation</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2.5">
            <FlaskConical className="w-6 h-6 text-purple-400" />
            Synthetic Evaluation Benchmark
          </h1>
          <p className="text-sm text-slate-500 mt-1.5 max-w-2xl">
            Automated regression benchmark executing 8 deterministic incident scenarios, testing
            diagnosis accuracy, citation verification, and prompt injection defenses.
          </p>
        </div>

        <button
          onClick={handleRunEvaluation}
          disabled={running}
          className="shrink-0 inline-flex items-center gap-2 px-5 py-2.5 text-sm font-semibold text-white bg-purple-600 hover:bg-purple-500 disabled:opacity-50 disabled:cursor-not-allowed rounded-xl shadow-[0_0_16px_rgba(124,58,237,0.4)] hover:shadow-[0_0_20px_rgba(124,58,237,0.6)] transition-all duration-200"
        >
          {running ? (
            <>
              <div className="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
              <span>Running 8 Scenarios...</span>
            </>
          ) : (
            <>
              <Play className="w-3.5 h-3.5 fill-current" />
              <span>Run Evaluation Suite</span>
              <Zap className="w-3.5 h-3.5" />
            </>
          )}
        </button>
      </div>

      {/* Loading */}
      {running && (
        <LoadingState
          message="Executing Deterministic Evaluation Scenarios..."
          subtext="Simulating Bad Deployments, Dependency Timeouts, Memory Exhaustion, Config Drifts, Inconclusive gaps, and Prompt Injections."
        />
      )}

      {/* Error */}
      {error && (
        <ErrorState
          title="Benchmark Run Failed"
          message={error}
          onRetry={handleRunEvaluation}
        />
      )}

      {/* Results */}
      {summary && !running && (
        <div className="space-y-6 animate-fade-in-up">
          {/* Score Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {scorecards.map((card) => {
              const Icon = card.icon;
              return (
                <div
                  key={card.label}
                  className={`rounded-2xl border ${card.border} ${card.bg} p-5`}
                >
                  <div className="flex items-start justify-between mb-3">
                    <span className="text-[10px] font-mono uppercase tracking-widest text-slate-500">
                      {card.label}
                    </span>
                    <div className={`w-7 h-7 rounded-lg border flex items-center justify-center ${card.iconBg}`}>
                      <Icon className={`w-3.5 h-3.5 ${card.color}`} />
                    </div>
                  </div>
                  <div className={`text-3xl font-extrabold font-mono ${card.color}`}>{card.value}</div>
                  <p className="text-[11px] text-slate-600 mt-1">{card.sub}</p>
                </div>
              );
            })}
          </div>

          {/* Scenario Results Table */}
          <div className="rounded-2xl border border-slate-800/60 bg-slate-900/30 overflow-hidden">
            <div className="px-5 py-4 border-b border-slate-800/60 flex items-center justify-between">
              <span className="text-sm font-semibold text-slate-200 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-purple-500" />
                Detailed Scenario Breakdown
              </span>
              <span className="text-xs font-mono text-purple-400 bg-purple-950/30 border border-purple-800/40 px-2.5 py-1 rounded-lg">
                {summary.scenarioResults.length} Evaluated
              </span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr className="border-b border-slate-800/60 bg-slate-950/50">
                    {['Scenario', 'Expected', 'Diagnosed', 'Result', 'Citation %', 'Defense', 'Duration'].map((h) => (
                      <th
                        key={h}
                        className="py-3 px-4 text-[10px] font-mono uppercase tracking-widest text-slate-600 font-medium"
                      >
                        {h}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/40">
                  {summary.scenarioResults.map((r) => (
                    <tr key={r.scenarioId} className="table-row-hover transition-colors duration-150">
                      <td className="py-3.5 px-4">
                        <div className="font-mono font-semibold text-xs text-slate-200">{r.scenarioId}</div>
                        <div className="text-[11px] text-slate-500 mt-0.5">{r.scenarioTitle}</div>
                      </td>
                      <td className="py-3.5 px-4 font-mono text-xs text-slate-400">{r.expectedCategory}</td>
                      <td className="py-3.5 px-4 font-mono text-xs text-slate-200">{r.actualCategory}</td>
                      <td className="py-3.5 px-4">
                        {r.categoryCorrect ? (
                          <span className="inline-flex items-center gap-1 px-2 py-1 rounded-lg text-[11px] font-mono font-semibold bg-emerald-950/50 text-emerald-400 border border-emerald-800/50">
                            <CheckCircle2 className="w-3 h-3" />
                            PASS
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 px-2 py-1 rounded-lg text-[11px] font-mono font-semibold bg-rose-950/50 text-rose-400 border border-rose-800/50">
                            <XCircle className="w-3 h-3" />
                            FAIL
                          </span>
                        )}
                      </td>
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-2">
                          <div className="w-12 h-1.5 rounded-full bg-slate-800">
                            <div
                              className="h-full rounded-full bg-emerald-500 transition-all"
                              style={{ width: `${Math.round(r.citationValidityRate * 100)}%` }}
                            />
                          </div>
                          <span className="font-mono text-xs text-slate-400">
                            {Math.round(r.citationValidityRate * 100)}%
                          </span>
                        </div>
                      </td>
                      <td className="py-3.5 px-4">
                        {r.promptInjectionSafeguarded ? (
                          <span className="inline-flex items-center gap-1 text-[11px] font-mono text-sky-400">
                            <ShieldCheck className="w-3.5 h-3.5" />
                            Safe
                          </span>
                        ) : (
                          <span className="text-[11px] font-mono text-slate-600">N/A</span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 font-mono text-xs text-slate-500">
                        {r.durationMs}ms
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Empty / initial state */}
      {!summary && !running && !error && (
        <div className="flex flex-col items-center justify-center py-20 px-8 text-center rounded-2xl border border-dashed border-slate-800/60 bg-slate-900/20">
          <div className="relative mb-5">
            <div className="w-16 h-16 rounded-2xl bg-purple-950/40 border border-purple-800/40 flex items-center justify-center">
              <Sparkles className="w-7 h-7 text-purple-400" />
            </div>
            <div className="absolute inset-0 rounded-2xl bg-purple-500/5 blur-xl" />
          </div>
          <h3 className="text-base font-semibold text-slate-200">No Evaluation Run Yet</h3>
          <p className="text-sm text-slate-500 mt-2 max-w-md leading-relaxed">
            Click "Run Evaluation Suite" to execute all 8 automated scenarios against the backend
            and verify diagnosis accuracy, citation validity, and anti-injection guardrails.
          </p>
          <button
            onClick={handleRunEvaluation}
            className="mt-6 inline-flex items-center gap-2 px-5 py-2 text-xs font-semibold text-white bg-purple-600 hover:bg-purple-500 rounded-xl shadow-[0_0_16px_rgba(124,58,237,0.3)] transition-all duration-200"
          >
            <Play className="w-3.5 h-3.5 fill-current" />
            Run Evaluation Suite
          </button>
        </div>
      )}
    </div>
  );
};
