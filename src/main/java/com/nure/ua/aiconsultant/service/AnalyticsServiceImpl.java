package com.nure.ua.aiconsultant.service;

import com.nure.ua.aiconsultant.domain.Report;
import com.nure.ua.aiconsultant.repository.ReportRepo;
import org.springframework.stereotype.Service;

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
    public Report getPrevByChatId(Long id) {
        return reportRepo.findFirstByUserIdOrderByReportDateDesc(id);
    }
}
