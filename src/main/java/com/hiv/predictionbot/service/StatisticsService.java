package com.hiv.predictionbot.service;

import com.hiv.predictionbot.model.User;
import com.hiv.predictionbot.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getMetrics() {
        List<User> allUsers = userRepository.findAll();
        List<User> hivPositiveUsers = allUsers.stream().filter(User::isHivPositive).collect(Collectors.toList());
        List<User> hivPositiveWithMeds = hivPositiveUsers.stream().filter(User::isOnArtDrugs).collect(Collectors.toList());
        List<User> hivPositiveWithoutMeds = hivPositiveUsers.stream().filter(user -> !user.isOnArtDrugs()).collect(Collectors.toList());

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUsers", allUsers.size());
        metrics.put("totalPredictions", allUsers.stream().filter(user -> user.getPredictedLifespan() != null).count());
        metrics.put("hivPositiveUsers", hivPositiveUsers.size());
        metrics.put("usersOnARVs", hivPositiveWithMeds.size());
        metrics.put("accuracyRate", 89); // This should be calculated or fetched from an API
        metrics.put("avgLifespanAllUsers", calculateAverage(allUsers));
        metrics.put("avgLifespanHIVPositive", calculateAverage(hivPositiveUsers));
        metrics.put("avgLifespanWithMeds", calculateAverage(hivPositiveWithMeds));
        metrics.put("avgLifespanWithoutMeds", calculateAverage(hivPositiveWithoutMeds));
        metrics.put("medianLifespanAllUsers", calculateMedian(allUsers));
        metrics.put("medianLifespanHIVPositive", calculateMedian(hivPositiveUsers));
        metrics.put("medianLifespanWithMeds", calculateMedian(hivPositiveWithMeds));
        metrics.put("medianLifespanWithoutMeds", calculateMedian(hivPositiveWithoutMeds));

        return metrics;
    }

    public byte[] generateStatistics() throws IOException {
        List<User> allUsers = userRepository.findAll();
        Map<String, Object> metrics = getMetrics();

        Workbook workbook = new XSSFWorkbook();
        try {
            Sheet peopleSheet = workbook.createSheet("People");
            createHeaderRow(peopleSheet, "ID", "First Name", "Last Name", "Age", "Predicted Lifespan", "HIV Positive", "On ART Drugs");
            populatePeopleSheet(peopleSheet, allUsers);

            Sheet statsSheet = workbook.createSheet("Statistics");
            populateStatisticsSheet(statsSheet, metrics);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } finally {
            workbook.close();
        }
    }

    private void createHeaderRow(Sheet sheet, String... headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }

    private void populatePeopleSheet(Sheet sheet, List<User> allUsers) {
        int rowNum = 1;
        for (User user : allUsers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getFirstName());
            row.createCell(2).setCellValue(user.getLastName());
            row.createCell(3).setCellValue(user.getAge());
            row.createCell(4).setCellValue(user.getPredictedLifespan());
            row.createCell(5).setCellValue(user.isHivPositive());
            row.createCell(6).setCellValue(user.isOnArtDrugs());
        }
    }

    private void populateStatisticsSheet(Sheet sheet, Map<String, Object> metrics) {
        createHeaderRow(sheet, "Metric", "Value");
        int rowNum = 1;
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            Cell valueCell = row.createCell(1);
            if (entry.getValue() instanceof Number) {
                valueCell.setCellValue(((Number) entry.getValue()).doubleValue());
            } else {
                valueCell.setCellValue(String.valueOf(entry.getValue()));
            }
        }
    }

    private double calculateAverage(List<User> users) {
        return users.stream()
                .mapToDouble(User::getPredictedLifespan)
                .average()
                .orElse(0.0);
    }

    private double calculateMedian(List<User> users) {
        List<Integer> lifespans = users.stream()
                .map(User::getPredictedLifespan)
                .sorted()
                .collect(Collectors.toList()).reversed();
        int size = lifespans.size();
        if (size == 0) return 0.0;
        if (size % 2 == 0) {
            return (lifespans.get(size / 2 - 1) + lifespans.get(size / 2)) / 2.0;
        } else {
            return lifespans.get(size / 2);
        }
    }
}