import React from 'react';
import { Database, FileText, Activity, GitCommit, Layers, ChevronRight } from 'lucide-react';

interface EvidenceListProps {
  evidenceRaw: string;
}

const CATEGORY_CONFIG = {
  log: { label: 'LOG', icon: FileText, color: 'text-emerald-300 bg-emerald-950/50 border-emerald-800/50' },
  metric: { label: 'METRIC', icon: Activity, color: 'text-sky-300 bg-sky-950/50 border-sky-800/50' },
  dep: { label: 'DEPENDENCY', icon: Layers, color: 'text-amber-300 bg-amber-950/50 border-amber-800/50' },
  downstream: { label: 'DEPENDENCY', icon: Layers, color: 'text-amber-300 bg-amber-950/50 border-amber-800/50' },
  deploy: { label: 'DEPLOY', icon: GitCommit, color: 'text-purple-300 bg-purple-950/50 border-purple-800/50' },
};

const getEvidenceCategory = (id: string) => {
  const lower = id.toLowerCase();
  for (const [key, config] of Object.entries(CATEGORY_CONFIG)) {
    if (lower.includes(key)) return config;
  }
  return { label: 'DATA', icon: Database, color: 'text-slate-300 bg-slate-800/80 border-slate-700/60' };
};

export const EvidenceList: React.FC<EvidenceListProps> = ({ evidenceRaw }) => {
  let items: string[] = [];
  let isParsedArray = false;

  try {
    const parsed = JSON.parse(evidenceRaw);
    if (Array.isArray(parsed)) {
      items = parsed.map((item) => (typeof item === 'object' ? JSON.stringify(item) : String(item)));
      isParsedArray = true;
    } else if (typeof parsed === 'object' && parsed !== null) {
      items = Object.entries(parsed).map(([k, v]) => `${k}: ${JSON.stringify(v)}`);
      isParsedArray = true;
    }
  } catch {
    if (evidenceRaw?.trim()) {
      items = evidenceRaw.split('\n').map((s) => s.trim()).filter(Boolean);
    }
  }

  if (!evidenceRaw || items.length === 0) {
    return (
      <div className="text-xs text-slate-500 italic p-4 bg-slate-950/40 rounded-xl border border-slate-800/60 text-center">
        No formal evidence citations recorded for this investigation.
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {items.map((item, idx) => {
        const category = getEvidenceCategory(item);
        const Icon = category.icon;
        return (
          <div
            key={idx}
            className={`group flex items-center gap-3 px-3.5 py-2.5 rounded-xl border font-mono text-xs transition-all duration-150 hover:brightness-110 ${category.color}`}
          >
            <div className="shrink-0 w-6 h-6 rounded-md bg-black/20 flex items-center justify-center">
              <Icon className="w-3.5 h-3.5" />
            </div>
            <span className="text-[10px] tracking-widest font-bold opacity-60 shrink-0">
              [{category.label}]
            </span>
            <ChevronRight className="w-3 h-3 opacity-30 shrink-0" />
            <span className="select-all text-slate-200 text-[11px] leading-relaxed truncate" title={item}>
              {item}
            </span>
          </div>
        );
      })}
      {!isParsedArray && (
        <details className="mt-2">
          <summary className="text-[11px] text-slate-500 cursor-pointer hover:text-slate-400 transition-colors font-mono select-none">
            ▸ Show raw evidence text
          </summary>
          <pre className="mt-2 p-3 bg-slate-950/80 rounded-xl text-[11px] font-mono text-slate-400 border border-slate-800 overflow-x-auto whitespace-pre-wrap leading-relaxed">
            {evidenceRaw}
          </pre>
        </details>
      )}
    </div>
  );
};
