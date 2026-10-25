package org.example.service;

import org.example.model.ApplicationStatus;
import org.example.model.EducationType;
import org.example.model.TrainingApplication;
import org.example.repository.TrainingApplicationRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsService {

    private final TrainingApplicationRepository repository;

    public StatisticsService(
            TrainingApplicationRepository repository
    ) {
        this.repository = repository;
    }

    public record StatisticRow(String title, String value) {
    }

    public List<StatisticRow> calculate() {

        List<TrainingApplication> applications =
                repository.findAll();

        List<StatisticRow> rows = new ArrayList<>();

        rows.add(new StatisticRow(
                "Всего заявок",
                String.valueOf(applications.size())
        ));

        for (ApplicationStatus status : ApplicationStatus.values()) {
            rows.add(new StatisticRow(
                    "Статус: " + status.getDisplayName(),
                    String.valueOf(countByStatus(applications, status))
            ));
        }

        for (EducationType type : EducationType.values()) {
            rows.add(new StatisticRow(
                    "Форма: " + type.getDisplayName(),
                    String.valueOf(countByEducation(applications, type))
            ));
        }

        rows.add(new StatisticRow(
                "Доля одобренных",
                percent(
                        applications.isEmpty() ? 0
                                : countByStatus(
                                applications,
                                ApplicationStatus.APPROVED
                        ),
                        applications.size()
                )
        ));

        LocalDate monthAgo = LocalDate.now().minusDays(30);

        long lastMonth = applications.stream()
                .filter(a -> !a.getApplicationDate().isBefore(monthAgo))
                .count();

        rows.add(new StatisticRow(
                "Заявок за последние 30 дней",
                String.valueOf(lastMonth)
        ));

        rows.add(new StatisticRow(
                "Самый популярный курс",
                mostPopularCourse(applications)
        ));

        rows.add(new StatisticRow(
                "Среднее число заявок на слушателя",
                averagePerUser(applications)
        ));

        return rows;
    }

    public void printStatistics() {

        List<StatisticRow> rows = calculate();

        System.out.println();
        System.out.println("========== СТАТИСТИКА ==========");

        for (StatisticRow row : rows) {
            System.out.printf(
                    "%s: %s%n",
                    row.title(),
                    row.value()
            );
        }

        System.out.println("================================");
        System.out.println();
    }

    private long countByStatus(
            List<TrainingApplication> applications,
            ApplicationStatus status
    ) {
        return applications.stream()
                .filter(a -> a.getStatus() == status)
                .count();
    }

    private long countByEducation(
            List<TrainingApplication> applications,
            EducationType type
    ) {
        return applications.stream()
                .filter(a -> a.getEducationType() == type)
                .count();
    }

    private String percent(long part, long total) {
        if (total == 0) {
            return "0 %";
        }

        return String.format(
                "%.1f %%",
                part * 100.0 / total
        );
    }

    private String mostPopularCourse(
            List<TrainingApplication> applications
    ) {
        if (applications.isEmpty()) {
            return "—";
        }

        Map<String, Long> byCourse = applications.stream()
                .collect(Collectors.groupingBy(
                        TrainingApplication::getCourseName,
                        Collectors.counting()
                ));

        return byCourse.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry ->
                        entry.getKey() +
                                " (" + entry.getValue() + " шт.)"
                )
                .orElse("—");
    }

    private String averagePerUser(
            List<TrainingApplication> applications
    ) {
        long users = applications.stream()
                .map(a -> a.getUser().getId())
                .distinct()
                .count();

        if (users == 0) {
            return "0.00";
        }

        return String.format(
                "%.2f",
                applications.size() / (double) users
        );
    }
}
