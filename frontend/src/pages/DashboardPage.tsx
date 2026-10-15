import React, { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import { InvestigationResponse } from '../types/api';
import { StatusBadge } from '../components/StatusBadge';
import { LoadingState } from '../components/LoadingState';
import { ErrorState } from '../components/ErrorState';
import { EmptyState } from '../components/EmptyState';
import {
  Activity,
  PlusCircle,
  RefreshCw,
  ArrowUpRight,
  ShieldCheck,
  Clock,
  AlertTriangle,
  FlaskConical,
  TrendingUp,
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const [investigations, setInvestigations] = useState<InvestigationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchInvestigations = useCallback(async (isManual = false) => {
    if (isManual) setRefreshing(true);
    try {
      setError(null);
      const data = await api.listInvestigations();
      setInvestigations(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load investigations');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchInvestigations();
  }, [fetchInvestigations]);

  const total = investigations.length;
  const completed = investigations.filter((i) => i.status === 'COMPLETED').length;
  const inProgress = investigations.filter((i) => i.status === 'RUNNING' || i.status === 'PENDING').length;
  const flagged = investigations.filter((i) => i.status === 'FAILED' || i.status === 'INCONCLUSIVE').length;

  const stats = [
    {
      label: 'Total Ingested',
      value: total,
      sub: 'Recorded investigations',
      icon: Activity,
      iconColor: 'text-blue-400',
      iconBg: 'bg-blue-500/10 border-blue-500/20',
      valueColor: 'text-white',
    },
    {
      label: 'Diagnosed',
      value: completed,
      sub: 'Full reports generated',
      icon: ShieldCheck,
      iconColor: 'text-emerald-400',
      iconBg: 'bg-emerald-500/10 border-emerald-500/20',
      valueColor: 'text-emerald-400',
    },
    {
      label: 'In Flight',
      value: inProgress,
      sub: 'Pending or diagnosing',
      icon: Clock,
      iconColor: 'text-sky-400',
      iconBg: 'bg-sky-500/10 border-sky-500/20',
      valueColor: 'text-sky-400',
    },
    {
      label: 'Inconclusive / Failed',
      value: flagged,
      sub: 'Uncertain or error states',
      icon: AlertTriangle,
      iconColor: 'text-amber-400',
      iconBg: 'bg-amber-500/10 border-amber-500/20',
      valueColor: 'text-amber-400',
    },
  ];

  return (
    <div className="space-y-8">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <TrendingUp className="w-4 h-4 text-blue-400" />
            <span className="text-xs font-mono text-blue-400 uppercase tracking-wider">Operations Center</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-white">
            Incident Operations Dashboard
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Real-time status of autonomous investigations, evidence citations, and root cause analyses.
          </p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <button
            onClick={() => fetchInvestigations(true)}
            disabled={refreshing || loading}
            className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-slate-400 bg-slate-900 hover:bg-slate-800 hover:text-slate-200 border border-slate-700/60 rounded-xl transition-all duration-200 disabled:opacity-40"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
          <Link
            to="/evaluation"
            className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-purple-300 bg-purple-950/30 hover:bg-purple-900/40 border border-purple-800/50 rounded-xl transition-all duration-200"
          >
            <FlaskConical className="w-3.5 h-3.5" />
            <span>Benchmark</span>
          </Link>
          <Link
            to="/incidents/new"
            className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-500 rounded-xl shadow-[0_0_16px_rgba(37,99,235,0.35)] hover:shadow-[0_0_20px_rgba(37,99,235,0.5)] transition-all duration-200"
          >
            <PlusCircle className="w-3.5 h-3.5" />
            <span>Trigger Alert</span>
          </Link>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {stats.map((s) => {
          const Icon = s.icon;
          return (
            <div
              key={s.label}
              className="group rounded-2xl border border-slate-800/60 bg-slate-900/40 hover:bg-slate-900/70 p-5 transition-all duration-200 hover:border-slate-700/60"
            >
              <div className="flex items-start justify-between mb-3">
                <span className="text-[10px] font-mono uppercase tracking-widest text-slate-500">
                  {s.label}
                </span>
                <div className={`w-7 h-7 rounded-lg border flex items-center justify-center ${s.iconBg}`}>
                  <Icon className={`w-3.5 h-3.5 ${s.iconColor} ${s.label === 'In Flight' && inProgress > 0 ? 'animate-pulse' : ''}`} />
                </div>
              </div>
              <div className={`text-3xl font-extrabold font-mono ${s.valueColor}`}>{s.value}</div>
              <p className="text-[11px] text-slate-500 mt-1">{s.sub}</p>
              {/* Progress bar */}
              {total > 0 && (
                <div className="mt-3 h-0.5 rounded-full bg-slate-800 overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-700 ${
                      s.valueColor === 'text-emerald-400' ? 'bg-emerald-500' :
                      s.valueColor === 'text-sky-400' ? 'bg-sky-500' :
                      s.valueColor === 'text-amber-400' ? 'bg-amber-500' :
                      'bg-blue-500'
                    }`}
                    style={{ width: `${Math.max(0, Math.min(100, (s.value / total) * 100))}%` }}
                  />
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Investigations Table */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-blue-500" />
            Recent Investigations
          </h2>
          <span className="text-xs font-mono text-slate-500">
            {investigations.length} record{investigations.length !== 1 ? 's' : ''}
          </span>
        </div>

        {loading ? (
          <LoadingState message="Fetching investigations from backend..." />
        ) : error ? (
          <ErrorState
            title="Failed to retrieve investigations"
            message={error}
            onRetry={() => fetchInvestigations(true)}
          />
        ) : investigations.length === 0 ? (
          <EmptyState
            title="No investigations recorded"
            description="Trigger a simulated alert or run the benchmark suite to initiate automated incident analysis."
            actionText="Trigger First Incident"
            actionHref="/incidents/new"
          />
        ) : (
          <div className="rounded-2xl border border-slate-800/60 bg-slate-900/30 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr className="border-b border-slate-800/60 bg-slate-950/50">
                    {['Investigation ID', 'Alert Reference', 'Status', 'Created', 'Duration', ''].map((h) => (
                      <th
                        key={h}
                        className="py-3 px-4 text-[10px] font-mono uppercase tracking-widest text-slate-500 font-medium last:text-right"
                      >
                        {h}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/40">
                  {investigations.map((inv) => {
                    const createdDate = new Date(inv.createdAt);
                    let durationText = '—';
                    if (inv.startedAt && inv.finishedAt) {
                      const ms =
                        new Date(inv.finishedAt).getTime() - new Date(inv.startedAt).getTime();
                      durationText = `${(ms / 1000).toFixed(1)}s`;
                    } else if (inv.startedAt && !inv.finishedAt) {
                      durationText = 'Running...';
                    }

                    return (
                      <tr
                        key={inv.id}
                        className="table-row-hover transition-colors duration-150 group"
                      >
                        <td className="py-3.5 px-4">
                          <Link
                            to={`/investigations/${inv.id}`}
                            className="inline-flex items-center gap-1.5 font-mono font-semibold text-sm text-slate-200 hover:text-blue-400 transition-colors"
                          >
                            <span className="text-slate-500">#</span>
                            <span>{inv.id}</span>
                          </Link>
                        </td>
                        <td className="py-3.5 px-4">
                          <span className="font-mono text-xs text-slate-400">
                            Alert <span className="text-slate-300">#{inv.alertId}</span>
                          </span>
                        </td>
                        <td className="py-3.5 px-4">
                          <StatusBadge status={inv.status} size="sm" />
                        </td>
                        <td className="py-3.5 px-4 font-mono text-[11px] text-slate-500">
                          {createdDate.toLocaleString()}
                        </td>
                        <td className="py-3.5 px-4">
                          <span className={`font-mono text-xs ${
                            durationText === 'Running...'
                              ? 'text-sky-400 animate-pulse'
                              : 'text-slate-400'
                          }`}>
                            {durationText}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          <Link
                            to={`/investigations/${inv.id}`}
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-[11px] font-semibold text-blue-400 hover:text-white bg-blue-950/30 hover:bg-blue-600 rounded-lg border border-blue-900/50 hover:border-transparent transition-all duration-200 group-hover:opacity-100 opacity-70"
                          >
                            <span>Inspect</span>
                            <ArrowUpRight className="w-3 h-3" />
                          </Link>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
