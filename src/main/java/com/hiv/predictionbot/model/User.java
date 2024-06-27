package com.hiv.predictionbot.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column
    private int age;

    @Column(name = "hiv_positive")
    private boolean hivPositive;

    @Column(name = "time_caught_virus")
    private LocalDate timeCaughtVirus;

    @Column(name = "on_art_drugs")
    private boolean onArtDrugs;

    @Column(name = "time_started_art")
    private LocalDate timeStartedArt;

    @Column(unique = true)
    private String email;
    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column
    private Role role;
    @Column(name = "predicted_lifespan")
    private Integer predictedLifespan;
}