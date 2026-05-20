package com.ksb.feedbackbot.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "feedback")
@Data
public class Feedback {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotBlank(message = "Candidate name cannot be blank!")
    @Size(min = 2, max = 100)
    @Column(name = "candidate_name")
    private String candidateName;

    @NotBlank(message = "Slack User ID is required")
    @Column(name = "slack_user_id")
    private String slackUserId;

    @Column(name = "summary")
    private String summary;

}
