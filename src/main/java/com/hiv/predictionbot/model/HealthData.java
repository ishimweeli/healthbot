package com.hiv.predictionbot.model;

import com.hiv.predictionbot.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class HealthData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    private String firstName;
    private String lastName;
    private int age;
    private boolean hasHiv;
    private LocalDate hivDiagnosisDate;
    private boolean onArtDrugs;
    private LocalDate artStartDate;
}