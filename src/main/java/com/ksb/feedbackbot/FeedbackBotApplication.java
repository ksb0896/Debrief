package com.ksb.feedbackbot;

import com.ksb.feedbackbot.entity.Feedback;
import com.ksb.feedbackbot.service.FeedbackAnalyzer;
import com.ksb.feedbackbot.service.FeedbackValidatorService;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;

//logs
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// LLM
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
public class FeedbackBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeedbackBotApplication.class, args);
    }
}
