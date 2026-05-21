package com.ksb.feedbackbot;

import com.ksb.feedbackbot.entity.Feedback;
import com.ksb.feedbackbot.service.UserMessage.FeedbackAnalyzer;
import com.ksb.feedbackbot.service.UserMessage.FeedbackValidatorService;
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

@Configuration
class SlackConfig {

    @Value("${slack.bot.token}")
    private String botToken;

    @Value("${slack.app.token}")
    private String appToken;
    private static final Logger logger = LoggerFactory.getLogger(SlackConfig.class);
    private final FeedbackValidatorService feedbackValidatorService;

    public SlackConfig(FeedbackValidatorService feedbackValidatorService) {
        this.feedbackValidatorService = feedbackValidatorService;
    }

    // Define the App Bean (The logic/commands)
    @Bean
    public App initApp(FeedbackAnalyzer analyzer) {
        logger.info("Initializing Slack Bolt Application...");
        AppConfig config = AppConfig.builder()
                .singleTeamBotToken(botToken)
                .build();

        App app = new App(config);

        // Register your command - test purpose
        app.command("/feedbackuser", (req, ctx) -> {
            return ctx.ack(asBlocks(section(s -> s.text(markdownText("Who would you like to provide feedback for? *Sarah Jenkins*"))),
                    actions(a -> a.elements(asElements(
                            button(b -> b.text(plainText("Strong Hire")).value("hire").actionId("vote_hire").style("primary")),
                            button(b -> b.text(plainText("No Hire")).value("no_hire").actionId("vote_no_hire").style("danger"))
                    )))
            ));
        });

        // Action Handler for "Strong Hire"
        app.blockAction("vote_hire", (req, ctx) -> {
            ctx.respond(res -> res.text("AI is analyzing ..").replaceOriginal(true));
            //non-blocking
            new Thread(()->{
                try{
                    Feedback feedback = new Feedback();
                    feedbackValidatorService.saveFeedback(feedback);
                    ctx.respond(res -> res.text("Feedback saved!").replaceOriginal(true));
                }catch (IllegalArgumentException e){
                    try{
                        ctx.respond(res -> res.text("*Validation Error:* " + e.getMessage()).replaceOriginal(true));
                    }catch (Exception ex){
                        logger.error("Error sending validation response", ex);
                    }
                } catch (Exception e) {
                    logger.error("System error during feedback processing", e);
                    try{
                        ctx.respond(res -> res.text("An internal error occurred. Please try again.").replaceOriginal(true));
                    }catch (Exception ex){
                        logger.error("Error sending system error response", ex);
                    }
                }
            }).start();

//            String username = req.getPayload().getUser().getName();
//            // Modern SDK uses a response object to update the message
//            ctx.respond(res -> res.text("Decision logged: *" + username + "* voted Strong Hire! ✅").replaceOriginal(true));
//            return ctx.ack();
            return ctx.ack(); //3 sec exe.
        });

        // Action Handler for "No Hire"
        app.blockAction("vote_no_hire", (req, ctx) -> {
            String username = req.getPayload().getUser().getName();
            ctx.respond(res -> res.text("Logging decision for *" + username + "*...").replaceOriginal(true));
            //non-blocking
            new Thread(()->{
                try{
                    ctx.respond(res->res.text("Decision logged: *" + username + "* voted No Hire.").replaceOriginal(true));
                } catch (IOException e) {
                    try{
                        ctx.respond(res->res.text("Error logging decision: " + e.getMessage()).replaceOriginal(true));
                    }catch (java.io.IOException ex){
                        ex.printStackTrace();
                    }
                }
            }).start();

            return ctx.ack();
        });

        return app;
    }

    // Define SocketModeApp Bean (The connection)
    @Bean
    public SocketModeApp socketModeApp(App app) throws Exception {
        SocketModeApp socketApp = new SocketModeApp(appToken, app);

        // startAsync() connects to Slack in the background without blocking Spring
        socketApp.startAsync();
        logger.info("⚡Debrief is connected to Slack via Socket Mode!");

        return socketApp;
    }

    @Bean
    public FeedbackAnalyzer feedbackAnalyzer() {
        // Creating the model connection
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("phi3:mini")
                .build();

        // Creating the service
        return AiServices.create(FeedbackAnalyzer.class, model);
    }
}
