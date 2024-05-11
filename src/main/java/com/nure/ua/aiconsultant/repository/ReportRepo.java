package com.nure.ua.aiconsultant.repository;

import com.nure.ua.aiconsultant.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepo extends JpaRepository<Report, Long> {
    @Query("select avg(r.reportRate) from Report r")
    Double getAvgRate();
    Report findFirstByUserIdOrderByReportDateDesc(Long userId);
}
