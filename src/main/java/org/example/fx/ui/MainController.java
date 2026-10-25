package org.example.fx.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.util.DatabaseManager;

import java.sql.Connection;

public class MainController {

    @FXML
    private Label dbStatusLabel;

    @FXML
    private void initialize() {
        updateDbStatus();
    }

    @FXML
    private void handleCheckConnection() {
        updateDbStatus();
    }

    private void updateDbStatus() {

        try (Connection connection = DatabaseManager.getConnection()) {

            if (connection.isValid(2)) {
                dbStatusLabel.setText(
                        "База данных: подключено (" +
                                shortUrl(DatabaseManager.getUrl()) + ")"
                );
                dbStatusLabel.getStyleClass().setAll("status-text", "status-ok");
            } else {
                dbStatusLabel.setText("База данных: соединение не отвечает");
                dbStatusLabel.getStyleClass().setAll("status-text", "status-error");
            }

        } catch (Exception e) {
            dbStatusLabel.setText(
                    "База данных: нет подключения — " +
                            org.example.fx.util.Ui.rootMessage(e)
            );
            dbStatusLabel.getStyleClass().setAll("status-text", "status-error");
        }
    }

    private String shortUrl(String url) {
        return url.replace("jdbc:postgresql://", "");
    }
}
