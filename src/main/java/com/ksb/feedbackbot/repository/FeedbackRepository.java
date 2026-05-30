package com.ksb.feedbackbot.repository;

import com.ksb.feedbackbot.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findBySummaryIsNull();
    List<Feedback> findBySummaryIsNullAndLastNudgeDateBeforeOrLastNudgeDateIsNull(LocalDateTime date);
    List<Feedback> findByCandidateNameIgnoreCase(String candidateName);
}
