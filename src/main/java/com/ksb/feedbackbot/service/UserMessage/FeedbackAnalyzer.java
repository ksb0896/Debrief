package com.ksb.feedbackbot.service.UserMessage;
import dev.langchain4j.service.UserMessage;

public interface FeedbackAnalyzer {
    @UserMessage("You are a professional HR assistant. Analyze the following interview notes: '{{it}}'. " +
            "Provide a concise summary with: \n" +
            "- Technical Level\n" +
            "- Cultural Fit\n" +
            "- One 'Hire' or 'No Hire' recommendation based on the tone.")
    String analyze(String text);
}
