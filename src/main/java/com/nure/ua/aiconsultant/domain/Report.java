package com.nure.ua.aiconsultant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report {

    @Id
    private Long id;

    @Column
    private String question;

    @Column
    private String answer;

    @Column
    private Integer rate;

    @Column
    private LocalDateTime date;
}
