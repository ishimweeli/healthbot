package com.hiv.predictionbot.service;

import com.hiv.predictionbot.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class PredictionService {
    private static final int RWANDAN_LIFESPAN = 66;
    private static final int SURVIVAL_RATE_WITHOUT_ART = 5;
    private static final double ART_SURVIVAL_RATE = 0.9;
    private static final double YEARLY_REDUCTION_RATE = 0.05;

    public int predictLifespan(User user) {
        if (!user.isHivPositive()) {
            return RWANDAN_LIFESPAN;
        }

        if (!user.isOnArtDrugs()) {
            int yearsSinceDiagnosis = LocalDate.now().getYear() - user.getTimeCaughtVirus().getYear();
            int ageUserHad = user.getAge() - yearsSinceDiagnosis;
            return ageUserHad + SURVIVAL_RATE_WITHOUT_ART;
        }

        long yearBetweenDelayed = ChronoUnit.YEARS.between(user.getTimeCaughtVirus(), user.getTimeStartedArt());
        long yearsDelayed=yearBetweenDelayed-1;
        if (yearsDelayed >5) {
            int yearsSinceDiagnosis = LocalDate.now().getYear() - user.getTimeCaughtVirus().getYear();
            int ageUserHad = user.getAge() - yearsSinceDiagnosis;
            return ageUserHad + SURVIVAL_RATE_WITHOUT_ART;
        }
//        int yearsSinceDiagnosis = LocalDate.now().getYear() - user.getTimeCaughtVirus().getYear();
//        int ageAtDiagnosis = user.getAge() - yearsSinceDiagnosis;
        int remainingYears = RWANDAN_LIFESPAN - user.getAge();
        double survivalRate = ART_SURVIVAL_RATE - (yearsDelayed * YEARLY_REDUCTION_RATE);
        int predictedRemainingYears = (int) Math.round(remainingYears * survivalRate);

        return user.getAge() + predictedRemainingYears;

    }
}