package com.incidenttriage.common.config;

import com.incidenttriage.investigation.InvestigationRepository;
import com.incidenttriage.investigation.InvestigationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "investigationTaskExecutor")
    public ThreadPoolTaskExecutor investigationTaskExecutor(InvestigationRepository investigationRepository) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("investigation-worker-");

        // Custom rejection handler to prevent orphan PENDING tasks during queue saturation
        executor.setRejectedExecutionHandler((r, poolExecutor) -> {
            log.error("Task rejected due to thread pool saturation");
            throw new IllegalStateException("System overloaded: investigation queue capacity exceeded");
        });

        executor.initialize();
        return executor;
    }
}
