package org.example.fx.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.net.URL;
import java.util.Optional;

public final class Ui {

    private Ui() {
    }

    public static void error(Window owner, Throwable error) {
        Alert alert = new Alert(
                Alert.AlertType.ERROR,
                rootMessage(error),
                ButtonType.OK
        );

        alert.initOwner(owner);
        alert.setHeaderText("Операция не выполнена");
        alert.showAndWait();
    }

    public static void info(Window owner, String message) {
        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                message,
                ButtonType.OK
        );

        alert.initOwner(owner);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static boolean confirm(Window owner, String message) {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                message,
                ButtonType.YES,
                ButtonType.NO
        );

        alert.initOwner(owner);
        alert.setHeaderText("Подтверждение");

        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    public static Scene sceneWithStyles(Parent root) {
        Scene scene = new Scene(root);

        URL css = Ui.class.getResource("/org/example/fx/app.css");

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        return scene;
    }

    public static String rootMessage(Throwable error) {
        Throwable current = error;

        while (current != null) {
            if (current.getMessage() != null &&
                    !current.getMessage().isBlank()) {

                return current.getMessage();
            }
            current = current.getCause();
        }

        return "Неизвестная ошибка";
    }
}
