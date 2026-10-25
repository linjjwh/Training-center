package org.example.repository;

import org.example.model.User;
import org.example.exception.DatabaseException;
import org.example.exception.EntityNotFoundException;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    @Override
    public User save(User user) {

        String sql = """
                INSERT INTO users (full_name, email, phone)
                VALUES (?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    user.setId(resultSet.getInt("id"));
                }
            }

            return user;

        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при создании пользователя", e);
        }
    }

    @Override
    public List<User> findAll() {

        String sql = """
                SELECT id, full_name, email, phone
                FROM users
                ORDER BY id
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                User user = mapRow(resultSet);
                users.add(user);
            }

            return users;

        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при получении пользователей", e);
        }
    }

    @Override
    public Optional<User> findById(int id) {

        String sql = """
                SELECT id, full_name, email, phone
                FROM users
                WHERE id = ?
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
            throw new DatabaseException("Ошибка при поиске пользователя", e);
        }
    }

    @Override
    public User update(User user) {

        String sql = """
                UPDATE users
                SET full_name = ?, email = ?, phone = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setInt(4, user.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new EntityNotFoundException(
                        "Пользователь с ID " + user.getId() + " не найден"
                );
            }

            return user;

        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при изменении пользователя", e);
        }
    }

    @Override
    public void deleteById(int id) {

        String sql = """
                DELETE FROM users
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new EntityNotFoundException(
                        "Пользователь с ID " + id + " не найден"
                );
            }

        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при удалении пользователя", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {

        String sql = """
                SELECT id, full_name, email, phone
                FROM users
                WHERE email = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при поиске пользователя по email", e
            );
        }
    }

    @Override
    public List<User> searchByName(String name) {

        String sql = """
                SELECT id, full_name, email, phone
                FROM users
                WHERE LOWER(full_name) LIKE LOWER(?)
                ORDER BY full_name
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + name + "%");

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    users.add(mapRow(resultSet));
                }
            }

            return users;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при поиске пользователя по имени", e
            );
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {

        return new User(
                resultSet.getInt("id"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("phone")
        );
    }
}