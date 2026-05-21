package com.ksb.feedbackbot.service.UserMessage;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SchedulerHealthIndicator implements HealthIndicator {
    public static LocalDateTime lastRunTime = LocalDateTime.now();

    @Override
    public Health health() {
        // If the task hasn't run in 25 hours, flag it as down
        if (lastRunTime.isBefore(LocalDateTime.now().minusHours(25))) {
            return Health.down().withDetail("reason", "Scheduler hasn't run in over 25 hours").build();
        }
        return Health.up().withDetail("lastRun", lastRunTime).build();
    }
}
