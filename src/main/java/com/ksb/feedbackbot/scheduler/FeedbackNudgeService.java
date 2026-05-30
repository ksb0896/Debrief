package com.ksb.feedbackbot.scheduler;

import com.ksb.feedbackbot.customIndicators.SchedulerHealthIndicator;
import com.ksb.feedbackbot.entity.Feedback;
import com.ksb.feedbackbot.repository.FeedbackRepository;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class FeedbackNudgeService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackNudgeService.class);
    private final FeedbackRepository repository;
    private final String botToken;

    //constructor injection
    public FeedbackNudgeService(FeedbackRepository repository, @Value("${SLACK_BOT_TOKEN}") String botToken){
        this.repository=repository;
        this.botToken=botToken;
    }

    @Scheduled(cron = "${app.nudge.cron}")
    public void nudgePendingInterviewer(){
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        List<Feedback> toNudge = repository.findBySummaryIsNullAndLastNudgeDateBeforeOrLastNudgeDateIsNull(twentyFourHoursAgo);

        logger.info("Found {} candidates needing a nudge.", toNudge.size());

        for(Feedback feedback: toNudge) {
            try {
                String userId = feedback.getSlackUserId();
                MethodsClient client = Slack.getInstance().methods();

                // Send the message
                client.chatPostMessage(r -> r
                        .token(botToken)
                        .channel(userId)
                        .text("Hi! You have pending feedback for: " + feedback.getCandidateName())
                );
                // Update database
                feedback.setLastNudgeDate(LocalDateTime.now());
                repository.save(feedback);

                logger.info("SUCCESS: Nudge sent to user {} for candidate {}", userId, feedback.getCandidateName());
            } catch (Exception e) {
                logger.info("CRITICAL: Failed to nudge user {}. Reason: {}", feedback.getSlackUserId(), e.getMessage());
            }
        }
        SchedulerHealthIndicator.lastRunTime = LocalDateTime.now();
    }
}
