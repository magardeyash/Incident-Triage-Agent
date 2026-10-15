package com.incidenttriage.report;

import com.incidenttriage.common.exception.ResourceNotFoundException;
import com.incidenttriage.report.dto.ReportResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportByInvestigationId(Long investigationId) {
        InvestigationReport report = reportRepository.findByInvestigationId(investigationId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found for investigation ID: " + investigationId));
        return ReportResponse.from(report);
    }

    @Transactional
    public InvestigationReport saveReport(InvestigationReport report) {
        return reportRepository.save(report);
    }
}
