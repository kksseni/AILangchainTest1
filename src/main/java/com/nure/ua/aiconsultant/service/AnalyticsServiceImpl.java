package com.nure.ua.aiconsultant.service;

import com.nure.ua.aiconsultant.domain.Report;
import com.nure.ua.aiconsultant.repository.ReportRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AnalyticsServiceImpl implements AnalyticsService{

    private final ReportRepo reportRepo;

    public AnalyticsServiceImpl(ReportRepo reportRepo) {
        this.reportRepo = reportRepo;
    }

    @Override
    public Double getAvgRate() {
        return reportRepo.getAvgRate();
    }

    @Override
    public void insert(Report report) {
        reportRepo.save(report);
    }

    @Override
    public void update(Report report) {
        reportRepo.save(report);
    }

    @Override
    public Long getNumOfMessagesPerHour(Long userId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        reportRepo.getNumOfMessagesPerHour(userId, oneHourAgo);
        return null;
    }

    @Override
    public Report getPrevByChatId(Long id) {
        return reportRepo.findFirstByUserIdOrderByReportDateDesc(id);
    }
}
