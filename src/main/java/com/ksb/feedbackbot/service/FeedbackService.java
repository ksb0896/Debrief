package com.ksb.feedbackbot.service;

import com.ksb.feedbackbot.entity.Feedback;
import com.ksb.feedbackbot.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public String generateSummary(String candidateName){
        List<Feedback> feedbacks = feedbackRepository.findByCandidateNameIgnoreCase(candidateName);

        if (feedbacks.isEmpty()){
            return "No feedback is found for candidate: *" + candidateName + "*";
        }
        StringBuilder summary = new StringBuilder("*Feedback Report for " + candidateName + ":*\n\n");

        for(Feedback f : feedbacks){
            summary.append(". ").append(f.getSummary()).append("\n");
        }
        return summary.toString();
    }
}
