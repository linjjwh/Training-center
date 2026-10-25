package org.example.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TrainingCentreFxApplication extends Application {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        stage.setTitle("Training Centre — JavaFX");
        stage.setMinWidth(980);
        stage.setMinHeight(680);

        showMain();
        stage.show();
    }

    private void showMain() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/fx/ui/main-view.fxml"));

            Parent root = loader.load();

            setScene(new Scene(root, 1280, 780));
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Не удалось загрузить главный экран", e
            );
        }
    }

    private void setScene(Scene scene) {
        var css = getClass().getResource("/org/example/fx/app.css");

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
