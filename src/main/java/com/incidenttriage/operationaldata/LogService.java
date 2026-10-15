package com.incidenttriage.operationaldata;

import com.incidenttriage.evidence.EvidenceItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class LogService {

    private static final Duration MAX_WINDOW = Duration.ofHours(24);
    private static final int MAX_RESULTS = 100;

    private final SyntheticLogRepository logRepository;

    public LogService(SyntheticLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Transactional(readOnly = true)
    public List<SyntheticLog> queryLogs(String serviceName, Instant startTime, Instant endTime) {
        validateWindow(startTime, endTime);
        List<SyntheticLog> logs = logRepository.findByServiceNameAndTimestampBetweenOrderByTimestampAsc(
                serviceName, startTime, endTime);
        if (logs.size() > MAX_RESULTS) {
            logs = logs.subList(0, MAX_RESULTS);
        }
        return logs;
    }

    public List<EvidenceItem> queryLogsAsEvidence(String serviceName, Instant startTime, Instant endTime) {
        return queryLogs(serviceName, startTime, endTime).stream()
                .map(log -> EvidenceItem.fromLog(
                        log.getLogId(),
                        log.getServiceName(),
                        log.getTimestamp(),
                        log.getLevel(),
                        log.getMessage()
                ))
                .toList();
    }

    private void validateWindow(Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time must not be null");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must not be after end time");
        }
        if (Duration.between(startTime, endTime).compareTo(MAX_WINDOW) > 0) {
            throw new IllegalArgumentException("Time window cannot exceed " + MAX_WINDOW.toHours() + " hours");
        }
    }
}
