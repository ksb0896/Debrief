package com.ksb.feedbackbot.service.UserMessage;

import com.ksb.feedbackbot.repo.FeedbackRepository;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


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

    @Scheduled(cron = "${app.nudge.cron:0 0 9 * * MON-FRI}")
    public void nudgePendingInterviewer(){
        logger.info("Starting scheduled nudge check...");

        repository.findBySummaryIsNull().forEach(feedback ->{
            try{
                String userId = feedback.getSlackUserId();
                MethodsClient client = Slack.getInstance().methods();

                client.chatPostMessage(r->r.token(botToken).channel(userId)
                        .text("Hi! You have pending feedback for: " + feedback.getCandidateName()));
                logger.info("Nudge successfully sent to user: {}", userId);
            } catch (Exception e) {
                logger.info("Failed to send nudge for feedback ID {}: {}", feedback.getId(), e.getMessage());
            }
        });
    }
}
