package com.ksb.feedbackbot.repo;

import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findBySummaryIsNull();
}
