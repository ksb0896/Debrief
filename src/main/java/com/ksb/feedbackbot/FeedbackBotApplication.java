package com.ksb.feedbackbot;

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

@SpringBootApplication
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

    // 1. Define the App Bean (The logic/commands)
    @Bean
    public App initApp() {
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
            String username = req.getPayload().getUser().getName();
            // Modern SDK uses a response object to update the message
            ctx.respond(res -> res.text("Decision logged: *" + username + "* voted Strong Hire! ✅").replaceOriginal(true));
            return ctx.ack();
        });

        // Action Handler for "No Hire"
        app.blockAction("vote_no_hire", (req, ctx) -> {
            String username = req.getPayload().getUser().getName();
            ctx.respond(res -> res.text("Decision logged: *" + username + "* voted No Hire. ❌").replaceOriginal(true));
            return ctx.ack();
        });

        return app;
    }

    // 2. Define the SocketModeApp Bean (The connection)
    // Spring will automatically "inject" the App bean we defined above
    @Bean
    public SocketModeApp socketModeApp(App app) throws Exception {
        SocketModeApp socketApp = new SocketModeApp(appToken, app);

        // startAsync() connects to Slack in the background without blocking Spring
        socketApp.startAsync();
        System.out.println("⚡️ Debrief is connected to Slack via Socket Mode!");

        return socketApp;
    }
}
