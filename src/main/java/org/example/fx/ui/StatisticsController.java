package org.example.fx.ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.fx.ServiceFactory;
import org.example.fx.util.Ui;
import org.example.service.StatisticsService;

import java.util.List;

public class StatisticsController {

    @FXML
    private FlowPane cardsPane;

    private final StatisticsService statisticsService =
            ServiceFactory.statisticsService();

    @FXML
    private void initialize() {
        refresh();
    }

    @FXML
    private void handleRefresh() {
        refresh();
    }

    public void refresh() {
        try {
            List<StatisticsService.StatisticRow> rows =
                    statisticsService.calculate();

            cardsPane.getChildren().clear();

            for (StatisticsService.StatisticRow row : rows) {

                Label title = new Label(row.title());
                title.getStyleClass().add("stat-title");

                Label value = new Label(row.value());
                value.getStyleClass().add("stat-value");

                VBox card = new VBox(6, title, value);
                card.getStyleClass().add("stat-card");
                card.setPadding(new Insets(14, 18, 14, 18));
                card.setPrefWidth(240);

                cardsPane.getChildren().add(card);
            }

        } catch (Exception e) {
            Ui.error(cardsPane.getScene() == null
                    ? null
                    : cardsPane.getScene().getWindow(), e);
        }
    }
}
