import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../services/api';
import { InvestigationResponse, AlertResponse, ReportResponse } from '../types/api';
import { StatusBadge } from '../components/StatusBadge';
import { SeverityBadge } from '../components/SeverityBadge';
import { ReportView } from '../components/ReportView';
import { LoadingState } from '../components/LoadingState';
import { ErrorState } from '../components/ErrorState';
import {
  ArrowLeft,
  RefreshCw,
  Radio,
  FileCode,
  AlertTriangle,
  ChevronDown,
  ChevronUp,
  Clock,
  Layers,
} from 'lucide-react';

const PIPELINE_STEPS = [
  { id: 1, label: 'Telemetry Ingest' },
  { id: 2, label: 'pgvector RAG' },
  { id: 3, label: 'Gemini Inference' },
  { id: 4, label: 'Grounding Check' },
];

export const InvestigationPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const investigationId = Number(id);

  const [investigation, setInvestigation] = useState<InvestigationResponse | null>(null);
  const [alert, setAlert] = useState<AlertResponse | null>(null);
  const [report, setReport] = useState<ReportResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [reportLoading, setReportLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [reportError, setReportError] = useState<string | null>(null);
  const [showAlertPayload, setShowAlertPayload] = useState(false);
  const [activePipelineStep, setActivePipelineStep] = useState(0);
  const pollTimerRef = useRef<number | null>(null);
  const pollCountRef = useRef<number>(0);
  const pipelineTimerRef = useRef<number | null>(null);

  const stopPolling = useCallback(() => {
    if (pollTimerRef.current !== null) {
      window.clearInterval(pollTimerRef.current);
      pollTimerRef.current = null;
    }
    if (pipelineTimerRef.current !== null) {
      window.clearInterval(pipelineTimerRef.current);
      pipelineTimerRef.current = null;
    }
  }, []);

  const fetchReport = useCallback(async (invId: number) => {
    setReportLoading(true);
    setReportError(null);
    try {
      const rep = await api.getReport(invId);
      setReport(rep);
    } catch (err: any) {
      setReportError(err.message || 'Report not yet generated or not found');
    } finally {
      setReportLoading(false);
    }
  }, []);

  const fetchInvestigation = useCallback(
    async (isManual = false) => {
      if (!investigationId || isNaN(investigationId)) {
        setError('Invalid investigation ID');
        setLoading(false);
        return;
      }
      if (isManual) setRefreshing(true);
      try {
        const inv = await api.getInvestigation(investigationId);
        setInvestigation(inv);

        if (inv.alertId) {
          api.getAlert(inv.alertId)
            .then(setAlert)
            .catch(() => {/* optional */});
        }

        if (inv.status === 'COMPLETED' || inv.status === 'INCONCLUSIVE') {
          stopPolling();
          fetchReport(inv.id);
        } else if (inv.status === 'FAILED') {
          stopPolling();
        }
      } catch (err: any) {
        setError(err.message || 'Failed to retrieve investigation record');
        stopPolling();
      } finally {
        setLoading(false);
        setRefreshing(false);
      }
    },
    [investigationId, stopPolling, fetchReport]
  );

  useEffect(() => {
    fetchInvestigation();

    pollCountRef.current = 0;
    pollTimerRef.current = window.setInterval(async () => {
      pollCountRef.current += 1;
      if (pollCountRef.current > 60) {
        stopPolling();
        return;
      }
      try {
        if (!investigationId || isNaN(investigationId)) return;
        const currentInv = await api.getInvestigation(investigationId);
        setInvestigation(currentInv);
        if (
          currentInv.status === 'COMPLETED' ||
          currentInv.status === 'INCONCLUSIVE' ||
          currentInv.status === 'FAILED'
        ) {
          stopPolling();
          if (currentInv.status !== 'FAILED') {
            fetchReport(currentInv.id);
          }
        }
      } catch {
        // silently ignore transient errors
      }
    }, 2500);

    // Animate pipeline steps while running
    pipelineTimerRef.current = window.setInterval(() => {
      setActivePipelineStep((s) => (s + 1) % PIPELINE_STEPS.length);
    }, 1800);

    return () => { stopPolling(); };
  }, [investigationId, fetchInvestigation, stopPolling, fetchReport]);

  if (loading) {
    return <LoadingState message={`Fetching Investigation #${id}...`} />;
  }

  if (error || !investigation) {
    return (
      <div className="space-y-4">
        <Link to="/" className="inline-flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-200 transition-colors">
          <ArrowLeft className="w-3.5 h-3.5" />
          Back to Dashboard
        </Link>
        <ErrorState
          title="Investigation Not Found"
          message={error || `Could not find an investigation record with ID #${id}`}
          onRetry={() => fetchInvestigation(true)}
        />
      </div>
    );
  }

  const isTerminal =
    investigation.status === 'COMPLETED' ||
    investigation.status === 'INCONCLUSIVE' ||
    investigation.status === 'FAILED';

  const durationMs =
    investigation.startedAt && investigation.finishedAt
      ? new Date(investigation.finishedAt).getTime() - new Date(investigation.startedAt).getTime()
      : null;

  return (
    <div className="space-y-6">
      {/* Breadcrumb */}
      <div className="flex items-center justify-between">
        <Link
          to="/"
          className="inline-flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-200 transition-colors group"
        >
          <ArrowLeft className="w-3.5 h-3.5 group-hover:-translate-x-0.5 transition-transform" />
          Back to Dashboard
        </Link>
        <button
          onClick={() => fetchInvestigation(true)}
          disabled={refreshing}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-400 bg-slate-900 hover:bg-slate-800 hover:text-slate-200 border border-slate-700/60 rounded-xl transition-all duration-200 disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Investigation Header Card */}
      <div className="rounded-2xl border border-slate-800/60 bg-slate-900/40 p-6 space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
          <div>
            <div className="flex items-center gap-3 mb-1">
              <h1 className="text-xl font-bold text-white font-mono">
                Investigation #{investigation.id}
              </h1>
              <StatusBadge status={investigation.status} size="md" />
            </div>
            <p className="text-xs text-slate-500 flex items-center gap-1.5">
              <Clock className="w-3 h-3" />
              Created {new Date(investigation.createdAt).toLocaleString()}
            </p>
          </div>

          {/* Time details */}
          <div className="flex items-center gap-5 text-xs font-mono text-slate-400 shrink-0">
            {investigation.startedAt && (
              <div className="text-right">
                <span className="block text-[10px] text-slate-600 uppercase tracking-wider mb-0.5">Started</span>
                <span className="text-slate-300">{new Date(investigation.startedAt).toLocaleTimeString()}</span>
              </div>
            )}
            {investigation.finishedAt && (
              <div className="text-right">
                <span className="block text-[10px] text-slate-600 uppercase tracking-wider mb-0.5">Finished</span>
                <span className="text-slate-300">{new Date(investigation.finishedAt).toLocaleTimeString()}</span>
              </div>
            )}
            {durationMs !== null && (
              <div className="text-right">
                <span className="block text-[10px] text-slate-600 uppercase tracking-wider mb-0.5">Duration</span>
                <span className="text-emerald-400 font-semibold">{(durationMs / 1000).toFixed(1)}s</span>
              </div>
            )}
          </div>
        </div>

        {/* Live Pipeline Indicator */}
        {!isTerminal && (
          <div className="rounded-xl border border-sky-900/40 bg-gradient-to-br from-sky-950/30 to-sky-950/10 p-5">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2 text-xs font-semibold text-sky-300">
                <Radio className="w-4 h-4 text-sky-400 animate-pulse" />
                Autonomous Agent Investigation In Progress
              </div>
              <span className="text-[10px] font-mono text-sky-600 bg-sky-950/60 px-2 py-0.5 rounded-full">
                Polling every 2.5s
              </span>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
              {PIPELINE_STEPS.map((step, i) => (
                <div
                  key={step.id}
                  className={`flex items-center gap-2 p-2.5 rounded-lg border text-xs font-mono transition-all duration-500 ${
                    i === activePipelineStep
                      ? 'border-sky-500/60 bg-sky-950/60 text-sky-200 shadow-[0_0_8px_rgba(14,165,233,0.2)]'
                      : i < activePipelineStep
                      ? 'border-emerald-800/40 bg-emerald-950/20 text-emerald-400'
                      : 'border-slate-800/60 bg-slate-950/40 text-slate-600'
                  }`}
                >
                  <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${
                    i === activePipelineStep ? 'bg-sky-400 animate-pulse' :
                    i < activePipelineStep ? 'bg-emerald-500' : 'bg-slate-700'
                  }`} />
                  <span>{step.id}. {step.label}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Failure Banner */}
        {investigation.status === 'FAILED' && (
          <div className="rounded-xl border border-rose-900/50 bg-gradient-to-br from-rose-950/30 to-rose-950/10 p-5 space-y-2">
            <div className="flex items-center gap-2 text-xs font-bold text-rose-400">
              <AlertTriangle className="w-4 h-4" />
              Investigation Failed
            </div>
            <p className="text-xs font-mono text-rose-200/80 whitespace-pre-wrap leading-relaxed">
              {investigation.errorMessage || 'An internal agent error occurred during diagnosis.'}
            </p>
          </div>
        )}
      </div>

      {/* Associated Alert Details */}
      {alert && (
        <div className="rounded-2xl border border-slate-800/60 bg-slate-900/30 p-5 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Layers className="w-4 h-4 text-slate-500" />
              <span className="text-xs font-mono uppercase tracking-widest text-slate-500">
                Triggered Alert
              </span>
              <span className="text-xs font-mono text-slate-600">#{alert.id}</span>
            </div>
            <SeverityBadge severity={alert.severity} size="sm" />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {[
              { label: 'Service', value: alert.serviceName, mono: true },
              { label: 'Alert Message', value: alert.message, mono: false },
              { label: 'Occurred At', value: new Date(alert.occurredAt || alert.createdAt).toLocaleString(), mono: true },
            ].map((f) => (
              <div key={f.label}>
                <span className="block text-[10px] font-mono uppercase tracking-widest text-slate-600 mb-1">
                  {f.label}
                </span>
                <span className={`text-sm text-slate-300 ${f.mono ? 'font-mono' : ''}`}>
                  {f.value}
                </span>
              </div>
            ))}
          </div>

          {alert.payload && (
            <div className="border-t border-slate-800/60 pt-3">
              <button
                type="button"
                onClick={() => setShowAlertPayload(!showAlertPayload)}
                className="flex items-center gap-2 text-xs text-slate-500 hover:text-slate-300 font-mono transition-colors"
              >
                <FileCode className="w-3.5 h-3.5" />
                <span>{showAlertPayload ? 'Hide' : 'Show'} raw alert payload</span>
                {showAlertPayload ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
              </button>
              {showAlertPayload && (
                <pre className="mt-3 p-4 bg-slate-950/80 rounded-xl text-[11px] font-mono text-slate-300 border border-slate-800/60 overflow-x-auto whitespace-pre-wrap leading-relaxed">
                  {alert.payload}
                </pre>
              )}
            </div>
          )}
        </div>
      )}

      {/* Report Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <span className="w-1.5 h-1.5 rounded-full bg-blue-500" />
          <h2 className="text-sm font-semibold text-slate-200">Agent Diagnostic Report</h2>
        </div>

        {reportLoading ? (
          <LoadingState message="Compiling investigation report & verifying evidence..." />
        ) : reportError ? (
          <ErrorState
            title="Failed to load report"
            message={reportError}
            onRetry={() => fetchReport(investigation.id)}
          />
        ) : report ? (
          <ReportView report={report} />
        ) : isTerminal && investigation.status !== 'FAILED' ? (
          <div className="p-10 text-center rounded-2xl border border-dashed border-slate-800/60 text-slate-600 text-sm">
            No diagnostic report was generated for this investigation.
          </div>
        ) : (
          <div className="p-10 text-center rounded-2xl border border-dashed border-slate-800/60 text-slate-600 text-sm font-mono">
            Report will appear automatically once diagnosis concludes.
          </div>
        )}
      </div>
    </div>
  );
};
