package com.incidenttriage.agent;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class InvestigationContext {

    private static final ThreadLocal<InvestigationContext> CURRENT = new ThreadLocal<>();

    private final Long investigationId;
    private final Instant deadline;
    private final int maxToolCalls;
    private final AtomicInteger toolCallCount = new AtomicInteger(0);

    public InvestigationContext(Long investigationId, Duration timeout, int maxToolCalls) {
        this.investigationId = investigationId;
        this.deadline = Instant.now().plus(timeout);
        this.maxToolCalls = maxToolCalls;
    }

    public static void set(InvestigationContext context) {
        CURRENT.set(context);
    }

    public static InvestigationContext get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    public Long getInvestigationId() {
        return investigationId;
    }

    public void incrementAndValidateToolCalls() {
        if (Instant.now().isAfter(deadline)) {
            throw new IllegalStateException("Investigation deadline exceeded");
        }
        int count = toolCallCount.incrementAndGet();
        if (count > maxToolCalls) {
            throw new IllegalStateException("Maximum tool call limit (" + maxToolCalls + ") exceeded");
        }
    }

    public int getToolCallCount() {
        return toolCallCount.get();
    }
}
