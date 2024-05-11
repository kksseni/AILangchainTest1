package com.nure.ua.aiconsultant.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column
    private Long userId;

    @Column(length = 10000)
    private String question;

    @Column(length = 10000)
    private String answer;

    @Column
    private Integer reportRate;

    @Column
    private LocalDateTime reportDate;
}
