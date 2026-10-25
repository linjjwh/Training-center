package org.example.repository;

import org.example.model.ApplicationStatus;
import org.example.model.EducationType;
import org.example.model.TrainingApplication;
import org.example.model.User;
import org.example.exception.DatabaseException;
import org.example.exception.EntityNotFoundException;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainingApplicationRepositoryImpl
        implements TrainingApplicationRepository {

    private static final String BASE_SELECT = """
            SELECT
                a.id,
                a.user_id,
                u.full_name,
                u.email,
                u.phone,
                a.course_name,
                a.application_date,
                a.status,
                a.education_type,
                a.comment
            FROM training_applications a
            JOIN users u ON u.id = a.user_id
            """;

    @Override
    public TrainingApplication save(TrainingApplication application) {

        String sql = """
                INSERT INTO training_applications
                (user_id, course_name, application_date, status, education_type, comment)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, application.getUser().getId());
            statement.setString(2, application.getCourseName());
            statement.setDate(
                    3,
                    Date.valueOf(application.getApplicationDate())
            );
            statement.setString(4, application.getStatus().name());
            statement.setString(5, application.getEducationType().name());
            statement.setString(6, application.getComment());

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    application.setId(resultSet.getInt("id"));
                }
            }

            return application;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при создании заявки", e
            );
        }
    }

    @Override
    public List<TrainingApplication> findAll() {

        String sql = BASE_SELECT + """
                ORDER BY a.id
                """;

        List<TrainingApplication> applications = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                applications.add(mapRow(resultSet));
            }

            return applications;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при получении заявок", e
            );
        }
    }

    @Override
    public Optional<TrainingApplication> findById(int id) {

        String sql = BASE_SELECT + """
                WHERE a.id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при поиске заявки", e
            );
        }
    }

    @Override
    public TrainingApplication update(TrainingApplication application) {

        String sql = """
                UPDATE training_applications
                SET user_id = ?,
                    course_name = ?,
                    application_date = ?,
                    status = ?,
                    education_type = ?,
                    comment = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, application.getUser().getId());
            statement.setString(2, application.getCourseName());
            statement.setDate(
                    3,
                    Date.valueOf(application.getApplicationDate())
            );
            statement.setString(4, application.getStatus().name());
            statement.setString(5, application.getEducationType().name());
            statement.setString(6, application.getComment());
            statement.setInt(7, application.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new EntityNotFoundException(
                        "Заявка с ID " + application.getId() + " не найдена"
                );
            }

            return application;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при изменении заявки", e
            );
        }
    }

    @Override
    public void deleteById(int id) {

        String sql = """
                DELETE FROM training_applications
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new EntityNotFoundException(
                        "Заявка с ID " + id + " не найдена"
                );
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при удалении заявки", e
            );
        }
    }

    @Override
    public List<TrainingApplication> searchByCourseName(
            String courseName
    ) {

        String sql = BASE_SELECT + """
                WHERE LOWER(a.course_name) LIKE LOWER(?)
                ORDER BY a.course_name
                """;

        return findByStringParameter(sql, "%" + courseName + "%");
    }

    @Override
    public List<TrainingApplication> searchByUserName(
            String userName
    ) {

        String sql = BASE_SELECT + """
                WHERE LOWER(u.full_name) LIKE LOWER(?)
                ORDER BY u.full_name
                """;

        return findByStringParameter(sql, "%" + userName + "%");
    }

    @Override
    public List<TrainingApplication> findByUserId(int userId) {

        String sql = BASE_SELECT + """
                WHERE a.user_id = ?
                ORDER BY a.application_date DESC, a.id DESC
                """;

        List<TrainingApplication> applications = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    applications.add(mapRow(resultSet));
                }
            }

            return applications;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при получении заявок слушателя", e
            );
        }
    }

    @Override
    public List<TrainingApplication> findByStatus(
            ApplicationStatus status
    ) {

        String sql = BASE_SELECT + """
                WHERE a.status = ?
                ORDER BY a.id
                """;

        List<TrainingApplication> applications = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    applications.add(mapRow(resultSet));
                }
            }

            return applications;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при фильтрации заявок по статусу", e
            );
        }
    }

    @Override
    public List<TrainingApplication> findByEducationType(
            EducationType educationType
    ) {

        String sql = BASE_SELECT + """
                WHERE a.education_type = ?
                ORDER BY a.id
                """;

        List<TrainingApplication> applications = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, educationType.name());

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    applications.add(mapRow(resultSet));
                }
            }

            return applications;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при фильтрации заявок по типу обучения", e
            );
        }
    }

    private List<TrainingApplication> findByStringParameter(
            String sql,
            String parameter
    ) {

        List<TrainingApplication> applications = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, parameter);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    applications.add(mapRow(resultSet));
                }
            }

            return applications;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при поиске заявок", e
            );
        }
    }

    private TrainingApplication mapRow(ResultSet resultSet)
            throws SQLException {

        User user = new User(
                resultSet.getInt("user_id"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("phone")
        );

        return new TrainingApplication(
                resultSet.getInt("id"),
                user,
                resultSet.getString("course_name"),
                resultSet.getDate("application_date").toLocalDate(),
                ApplicationStatus.valueOf(
                        resultSet.getString("status")
                ),
                EducationType.valueOf(
                        resultSet.getString("education_type")
                ),
                resultSet.getString("comment")
        );
    }
}