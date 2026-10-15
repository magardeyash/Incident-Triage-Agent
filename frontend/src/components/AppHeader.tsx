import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  ShieldAlert,
  PlusCircle,
  LayoutDashboard,
  FlaskConical,
  BookOpen,
  Search,
  ArrowRight,
  Activity,
  Menu,
  X,
} from 'lucide-react';

export const AppHeader: React.FC = () => {
  const [searchId, setSearchId] = useState('');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = searchId.trim();
    if (trimmed && !isNaN(Number(trimmed))) {
      navigate(`/investigations/${trimmed}`);
      setSearchId('');
    }
  };

  const navLinkBase =
    'relative flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-all duration-200';

  return (
    <header className="sticky top-0 z-50 border-b border-slate-800/60 bg-[#05090f]/80 backdrop-blur-xl">
      {/* Top accent line */}
      <div className="absolute top-0 left-0 right-0 h-[1px] bg-gradient-to-r from-transparent via-blue-500/40 to-transparent" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-14 flex items-center justify-between gap-4">
        {/* Brand */}
        <div className="flex items-center gap-6">
          <NavLink to="/" className="flex items-center gap-2.5 group shrink-0">
            <div className="relative w-8 h-8 rounded-lg bg-gradient-to-br from-blue-600/30 to-blue-800/20 border border-blue-500/30 flex items-center justify-center text-blue-400 group-hover:border-blue-400/60 group-hover:shadow-[0_0_12px_rgba(59,130,246,0.3)] transition-all duration-300">
              <ShieldAlert className="w-4 h-4" />
              <span className="absolute -top-0.5 -right-0.5 w-2 h-2 rounded-full bg-emerald-500 border border-[#05090f]">
                <span className="absolute inset-0 rounded-full bg-emerald-400 animate-ping opacity-75" />
              </span>
            </div>
            <div>
              <span className="block font-bold text-sm tracking-tight text-white group-hover:text-blue-300 transition-colors">
                Incident Triage
              </span>
              <span className="block text-[9px] font-mono text-slate-500 tracking-widest uppercase">
                Autonomous RCA · AI
              </span>
            </div>
          </NavLink>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center gap-0.5">
            <NavLink
              to="/"
              end
              className={({ isActive }) =>
                `${navLinkBase} ${
                  isActive
                    ? 'bg-slate-800 text-white'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`
              }
            >
              <LayoutDashboard className="w-3.5 h-3.5" />
              <span>Dashboard</span>
            </NavLink>

            <NavLink
              to="/incidents/new"
              className={({ isActive }) =>
                `${navLinkBase} ${
                  isActive
                    ? 'bg-blue-600/20 text-blue-300 border border-blue-500/30'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`
              }
            >
              <PlusCircle className="w-3.5 h-3.5" />
              <span>New Incident</span>
            </NavLink>

            <NavLink
              to="/evaluation"
              className={({ isActive }) =>
                `${navLinkBase} ${
                  isActive
                    ? 'bg-purple-600/20 text-purple-300 border border-purple-500/30'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`
              }
            >
              <FlaskConical className="w-3.5 h-3.5 text-purple-400" />
              <span>Benchmark</span>
            </NavLink>

            <NavLink
              to="/runbooks"
              className={({ isActive }) =>
                `${navLinkBase} ${
                  isActive
                    ? 'bg-emerald-600/20 text-emerald-300 border border-emerald-500/30'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`
              }
            >
              <BookOpen className="w-3.5 h-3.5 text-emerald-400" />
              <span>Runbooks</span>
            </NavLink>
          </nav>
        </div>

        {/* Right section */}
        <div className="flex items-center gap-2">
          {/* Search */}
          <form onSubmit={handleSearch} className="relative hidden sm:block">
            <Search className="w-3.5 h-3.5 text-slate-500 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            <input
              type="text"
              placeholder="Jump to ID #..."
              value={searchId}
              onChange={(e) => setSearchId(e.target.value)}
              className="w-40 lg:w-52 pl-8 pr-8 py-1.5 text-xs bg-slate-900/80 border border-slate-700/60 rounded-lg text-slate-200 placeholder-slate-600 focus:outline-none focus:border-blue-500/60 focus:bg-slate-900 font-mono transition-all"
            />
            {searchId && (
              <button
                type="submit"
                className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-400 hover:text-blue-400 transition-colors"
                title="Go"
              >
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            )}
          </form>

          {/* CTA Button */}
          <NavLink
            to="/incidents/new"
            className="hidden sm:inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-500 rounded-lg transition-all duration-200 shadow-[0_0_12px_rgba(37,99,235,0.4)] hover:shadow-[0_0_16px_rgba(37,99,235,0.6)]"
          >
            <Activity className="w-3.5 h-3.5" />
            <span>Trigger Alert</span>
          </NavLink>

          {/* Mobile menu toggle */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            {mobileMenuOpen ? <X className="w-4 h-4" /> : <Menu className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-slate-800/60 bg-[#05090f]/95 px-4 py-3 space-y-1 animate-fade-in-up">
          {[
            { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
            { to: '/incidents/new', label: 'New Incident', icon: PlusCircle },
            { to: '/evaluation', label: 'Benchmark Suite', icon: FlaskConical },
            { to: '/runbooks', label: 'Runbooks', icon: BookOpen },
          ].map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              onClick={() => setMobileMenuOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-colors ${
                  isActive ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
                }`
              }
            >
              <Icon className="w-4 h-4" />
              <span>{label}</span>
            </NavLink>
          ))}
          <form onSubmit={handleSearch} className="relative mt-2">
            <Search className="w-3.5 h-3.5 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Jump to investigation ID..."
              value={searchId}
              onChange={(e) => setSearchId(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-sm bg-slate-900 border border-slate-700 rounded-lg text-slate-200 placeholder-slate-600 focus:outline-none focus:border-blue-500 font-mono"
            />
          </form>
        </div>
      )}
    </header>
  );
};
