import React, { useState } from 'react';
import { api } from '../services/api';
import { RunbookSearchResult } from '../types/api';
import { LoadingState } from '../components/LoadingState';
import { ErrorState } from '../components/ErrorState';
import {
  BookOpen,
  Search,
  Sparkles,
  Layers,
  Database,
  ArrowRight,
  FileText,
} from 'lucide-react';

const SUGGESTED_QUERIES = [
  'Payment service timeouts and circuit breaking',
  'Checkout service rollback after bad deploy',
  'JVM OutOfMemoryError and high CPU thread dump',
  'Missing configuration keys and token drift',
];

export const RunbookSearchPage: React.FC = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<RunbookSearchResult[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async (searchQuery: string) => {
    if (!searchQuery.trim()) return;
    setLoading(true);
    setError(null);
    try {
      const data = await api.searchRunbooks(searchQuery.trim(), 4, 0.2);
      setResults(data);
    } catch (err: any) {
      setError(err.message || 'Runbook search failed');
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSearch(query);
  };

  return (
    <div className="space-y-8 max-w-5xl mx-auto">
      {/* Header */}
      <div className="border-b border-slate-800/60 pb-6">
        <div className="flex items-center gap-2 mb-1">
          <Database className="w-4 h-4 text-emerald-400" />
          <span className="text-xs font-mono text-emerald-400 uppercase tracking-wider">Knowledge Base</span>
        </div>
        <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2.5">
          <BookOpen className="w-6 h-6 text-emerald-400" />
          Semantic Runbook Search
        </h1>
        <p className="text-sm text-slate-500 mt-1.5">
          Query ingested SRE runbooks and operational SOPs retrieved using pgvector semantic cosine similarity.
        </p>
      </div>

      {/* Search Box */}
      <div className="space-y-3">
        <form onSubmit={onSubmit} className="relative">
          <div className="absolute left-4 top-1/2 -translate-y-1/2">
            <Search className="w-4 h-4 text-slate-500" />
          </div>
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search symptoms, runbook procedures, or error codes..."
            className="w-full pl-11 pr-28 py-3.5 bg-slate-900/60 border border-slate-700/60 rounded-2xl text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-emerald-500/60 focus:bg-slate-900 transition-all duration-200"
          />
          <button
            type="submit"
            disabled={loading || !query.trim()}
            className="absolute right-2 top-1/2 -translate-y-1/2 px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-500 disabled:opacity-40 disabled:cursor-not-allowed rounded-xl transition-all duration-200 flex items-center gap-1.5 shadow-[0_0_10px_rgba(16,185,129,0.3)]"
          >
            <span>Search</span>
            <ArrowRight className="w-3 h-3" />
          </button>
        </form>

        {/* Suggested Queries */}
        <div className="flex flex-wrap items-center gap-2">
          <span className="text-xs text-slate-600 flex items-center gap-1">
            <Sparkles className="w-3 h-3 text-emerald-500" />
            Try:
          </span>
          {SUGGESTED_QUERIES.map((sq, i) => (
            <button
              key={i}
              type="button"
              onClick={() => {
                setQuery(sq);
                handleSearch(sq);
              }}
              className="px-2.5 py-1 rounded-lg border border-slate-800/80 bg-slate-900/50 text-[11px] text-slate-400 hover:text-emerald-300 hover:border-emerald-800/60 hover:bg-emerald-950/20 transition-all duration-200"
            >
              {sq}
            </button>
          ))}
        </div>
      </div>

      {loading && <LoadingState message="Querying pgvector embedding store..." />}
      {error && <ErrorState title="Runbook Search Error" message={error} />}

      {/* Results */}
      {results && !loading && (
        <div className="space-y-4 animate-fade-in-up">
          <div className="flex items-center justify-between">
            <span className="text-sm font-semibold text-slate-200 flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
              Retrieved Context Chunks
              <span className="text-slate-500 font-normal">({results.length})</span>
            </span>
            <span className="text-[11px] font-mono text-emerald-500 bg-emerald-950/30 border border-emerald-800/40 px-2.5 py-1 rounded-lg">
              pgvector · cosine similarity
            </span>
          </div>

          {results.length === 0 ? (
            <div className="py-12 text-center rounded-2xl border border-dashed border-slate-800/60 text-slate-600 text-sm">
              No matching runbook sections found above the similarity threshold.
            </div>
          ) : (
            <div className="space-y-4">
              {results.map((res, idx) => {
                const simPct = Math.round(res.similarity * 100);
                const simColor =
                  simPct >= 80 ? 'text-emerald-400 bg-emerald-950/50 border-emerald-800/50' :
                  simPct >= 60 ? 'text-sky-400 bg-sky-950/50 border-sky-800/50' :
                  'text-amber-400 bg-amber-950/50 border-amber-800/50';

                return (
                  <div
                    key={idx}
                    className="rounded-2xl border border-slate-800/60 bg-slate-900/30 hover:bg-slate-900/50 p-5 space-y-4 transition-all duration-200 hover:border-slate-700/60"
                  >
                    {/* Card Header */}
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex items-start gap-3">
                        <div className="w-8 h-8 rounded-xl bg-emerald-950/40 border border-emerald-800/40 flex items-center justify-center shrink-0 mt-0.5">
                          <FileText className="w-4 h-4 text-emerald-400" />
                        </div>
                        <div>
                          <h3 className="text-sm font-semibold text-white">{res.title}</h3>
                          {res.service && (
                            <span className="inline-flex items-center gap-1 mt-1 text-[10px] font-mono px-2 py-0.5 rounded-md bg-slate-800/80 text-slate-400 border border-slate-700/60">
                              <Layers className="w-2.5 h-2.5" />
                              {res.service}
                            </span>
                          )}
                        </div>
                      </div>
                      <div className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl border font-mono text-xs shrink-0 ${simColor}`}>
                        <Database className="w-3 h-3" />
                        <span className="font-semibold">{simPct}%</span>
                        <span className="opacity-70">match</span>
                      </div>
                    </div>

                    {/* Similarity bar */}
                    <div className="h-0.5 rounded-full bg-slate-800 overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-700 ${
                          simPct >= 80 ? 'bg-emerald-500' : simPct >= 60 ? 'bg-sky-500' : 'bg-amber-500'
                        }`}
                        style={{ width: `${simPct}%` }}
                      />
                    </div>

                    {/* Content */}
                    <pre className="p-4 bg-slate-950/70 rounded-xl text-[11px] font-mono text-slate-300 border border-slate-800/60 whitespace-pre-wrap leading-relaxed overflow-x-auto">
                      {res.content}
                    </pre>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* Initial empty state */}
      {!results && !loading && !error && (
        <div className="flex flex-col items-center justify-center py-16 px-8 text-center rounded-2xl border border-dashed border-slate-800/60 bg-slate-900/20">
          <div className="relative mb-4">
            <div className="w-14 h-14 rounded-2xl bg-emerald-950/30 border border-emerald-800/30 flex items-center justify-center">
              <BookOpen className="w-6 h-6 text-emerald-500" />
            </div>
            <div className="absolute inset-0 rounded-2xl bg-emerald-500/5 blur-xl" />
          </div>
          <h3 className="text-base font-semibold text-slate-200">Semantic Runbook Search</h3>
          <p className="text-sm text-slate-500 mt-2 max-w-sm leading-relaxed">
            Enter a query above or select a suggested scenario to retrieve matching runbook sections via pgvector embeddings.
          </p>
        </div>
      )}
    </div>
  );
};
