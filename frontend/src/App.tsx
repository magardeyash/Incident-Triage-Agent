import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AppLayout } from './components/AppLayout';
import { DashboardPage } from './pages/DashboardPage';
import { CreateIncidentPage } from './pages/CreateIncidentPage';
import { InvestigationPage } from './pages/InvestigationPage';
import { EvaluationPage } from './pages/EvaluationPage';
import { RunbookSearchPage } from './pages/RunbookSearchPage';
import { NotFoundPage } from './pages/NotFoundPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="incidents/new" element={<CreateIncidentPage />} />
          <Route path="investigations/:id" element={<InvestigationPage />} />
          <Route path="evaluation" element={<EvaluationPage />} />
          <Route path="runbooks" element={<RunbookSearchPage />} />
          <Route path="404" element={<NotFoundPage />} />
          <Route path="*" element={<Navigate to="/404" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};
