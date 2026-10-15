package com.incidenttriage.alert;

public record AlertCreatedEvent(Long alertId, Long investigationId) {
}
