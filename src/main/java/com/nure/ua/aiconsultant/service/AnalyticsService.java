package com.nure.ua.aiconsultant.service;

import com.nure.ua.aiconsultant.domain.Report;

public interface AnalyticsService {
    Double getAvgRate();
    void insert(Report report);
    void update(Report report);
    Long getNumOfMessagesPerHour(Long userId);
    Report getPrevByChatId(Long id);
}
