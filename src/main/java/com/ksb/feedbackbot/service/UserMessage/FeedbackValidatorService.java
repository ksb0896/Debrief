package com.ksb.feedbackbot.service.UserMessage;

import com.ksb.feedbackbot.entity.Feedback;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import com.ksb.feedbackbot.repo.FeedbackRepository;

import java.util.stream.Collectors;

public class FeedbackValidatorService {

    private final Validator validator;
    private final FeedbackRepository repository;

    public FeedbackValidatorService(FeedbackRepository repository){
        this.repository = repository;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    public void validate(Feedback feedback){
        var violations = validator.validate(feedback);
        if(!violations.isEmpty()){
            String errorMessage = violations.stream()
                    .map(v -> v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Validation Failed: " + errorMessage);
        }
    }

    public void saveFeedback(Feedback feedback){
        this.validate(feedback);
        repository.save(feedback);
    }
}
