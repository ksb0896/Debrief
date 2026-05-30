package com.ksb.feedbackbot.controller;

import com.ksb.feedbackbot.service.FeedbackService;
import com.slack.api.Slack;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import org.springframework.stereotype.Component;

import javax.naming.Context;

@Component
public class SlackCommandController {

    private final FeedbackService feedbackService;
    private final String botToken = System.getenv("SLACK_BOT_TOKEN");

    public SlackCommandController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    public void handleReportCommand(String channelId, String candidateName) {
        String report = feedbackService.generateSummary(candidateName);

        try {
            MethodsClient client = Slack.getInstance().methods();
            client.chatPostMessage(ChatPostMessageRequest.builder()
                    .token(botToken)
                    .channel(channelId)
                    .text(report)
                    .build());
        } catch (Exception e) {
            // Log the error
        }
    }
}
