package com.nure.ua.aiconsultant.repository;

import com.nure.ua.aiconsultant.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReportRepo extends JpaRepository<Report, Long> {
    @Query("select avg(r.reportRate) from Report r")
    Double getAvgRate();

    @Query("select count(r) from Report r where r.userId = :userId and r.reportDate > :lastHour")
    Long getNumOfMessagesPerHour(@Param("userId") Long userId, @Param("lastHour") LocalDateTime lastHour);
    Report findFirstByUserIdOrderByReportDateDesc(Long userId);
}
