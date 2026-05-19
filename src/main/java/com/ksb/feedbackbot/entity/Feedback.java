package com.ksb.feedbackbot.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "feedback")
@Data
public class Feedback {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "candidate_name")
    private String candidateName;

    @Column(name = "slack_user_id")
    private String slackUserId;

    @Column(name = "summary")
    private String summary;

}
