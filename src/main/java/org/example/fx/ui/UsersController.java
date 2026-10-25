package org.example.fx.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Window;
import org.example.fx.ServiceFactory;
import org.example.fx.util.Ui;
import org.example.model.TrainingApplication;
import org.example.model.User;
import org.example.service.TrainingApplicationService;
import org.example.service.UserService;

import java.util.List;

public class UsersController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> fullNameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> phoneColumn;

    @FXML
    private TextField searchField;
    @FXML
    private Label countLabel;

    private final UserService userService = ServiceFactory.userService();
    private final TrainingApplicationService applicationService =
            ServiceFactory.applicationService();

    private final ObservableList<User> source =
            FXCollections.observableArrayList();
    private FilteredList<User> filtered;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getId()));
        fullNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullToEmpty(
                        data.getValue().getFullName())));
        emailColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullToEmpty(
                        data.getValue().getEmail())));
        phoneColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullToEmpty(
                        data.getValue().getPhone())));

        filtered = new FilteredList<>(source, user -> true);

        SortedList<User> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sorted);

        searchField.textProperty().addListener((o, old, value) ->
                applySearch());

        userTable.setRowFactory(table -> {
            TableRow<User> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleShowApplications();
                }
            });

            return row;
        });

        loadUsers();
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    private void loadUsers() {
        try {
            source.setAll(userService.findAll());
            applySearch();
        } catch (Exception e) {
            Ui.error(owner(), e);
        }
    }

    @FXML
    private void handleAdd() {
        if (UserDialogController.showAndWait(owner(), null)) {
            loadUsers();
        }
    }

    @FXML
    private void handleEdit() {
        User selected = selectedUser();

        if (selected == null) {
            Ui.info(owner(), "Выберите слушателя в таблице.");
            return;
        }

        if (UserDialogController.showAndWait(owner(), selected)) {
            loadUsers();
        }
    }

    @FXML
    private void handleDelete() {
        User selected = selectedUser();

        if (selected == null) {
            Ui.info(owner(), "Выберите слушателя в таблице.");
            return;
        }

        int applicationsCount;

        try {
            applicationsCount = applicationService
                    .findByUserId(selected.getId())
                    .size();
        } catch (Exception e) {
            Ui.error(owner(), e);
            return;
        }

        String warning = applicationsCount == 0
                ? ""
                : "\n\nУ слушателя " + applicationsCount +
                " заявк(и/а). Они будут удалены вместе со слушателем.";

        boolean confirmed = Ui.confirm(owner(),
                "Удалить слушателя «" + selected.getFullName() + "»?\n" +
                        "Это действие нельзя отменить." + warning);

        if (!confirmed) {
            return;
        }

        try {
            userService.delete(selected.getId());
            loadUsers();
            Ui.info(owner(),
                    "Слушатель «" + selected.getFullName() + "» удалён.");
        } catch (Exception e) {
            Ui.error(owner(), e);
        }
    }

    @FXML
    private void handleShowApplications() {
        User selected = selectedUser();

        if (selected == null) {
            Ui.info(owner(), "Выберите слушателя в таблице.");
            return;
        }

        List<TrainingApplication> applications;

        try {
            applications = applicationService
                    .findByUserId(selected.getId());
        } catch (Exception e) {
            Ui.error(owner(), e);
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner());
        dialog.setTitle("Заявки слушателя");
        dialog.setHeaderText(selected.getFullName() + " — заявок: "
                + applications.size());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setContent(
                buildApplicationsTable(applications));
        dialog.getDialogPane().setPrefSize(760, 380);
        dialog.showAndWait();
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
    }

    private TableView<TrainingApplication> buildApplicationsTable(
            List<TrainingApplication> applications
    ) {
        TableView<TrainingApplication> table = new TableView<>();
        table.setPlaceholder(new Label("У слушателя пока нет заявок."));
        table.setItems(FXCollections.observableArrayList(applications));

        TableColumn<TrainingApplication, Integer> idColumn =
                new TableColumn<>("№");
        idColumn.setPrefWidth(50);
        idColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getId()));

        TableColumn<TrainingApplication, String> courseColumn =
                new TableColumn<>("Курс");
        courseColumn.setPrefWidth(200);
        courseColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullToEmpty(
                        data.getValue().getCourseName())));

        TableColumn<TrainingApplication, String> dateColumn =
                new TableColumn<>("Дата");
        dateColumn.setPrefWidth(100);
        dateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(
                        data.getValue().getApplicationDate())));

        TableColumn<TrainingApplication, String> statusColumn =
                new TableColumn<>("Статус");
        statusColumn.setPrefWidth(140);
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus()
                        .getDisplayName()));

        TableColumn<TrainingApplication, String> educationColumn =
                new TableColumn<>("Форма");
        educationColumn.setPrefWidth(110);
        educationColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEducationType()
                        .getDisplayName()));

        table.getColumns().addAll(idColumn, courseColumn, dateColumn,
                statusColumn, educationColumn);

        return table;
    }

    private void applySearch() {
        if (filtered == null) {
            return;
        }

        String query = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase();

        filtered.setPredicate(user -> query.isBlank()
                || nullToEmpty(user.getFullName())
                .toLowerCase().contains(query)
                || nullToEmpty(user.getEmail())
                .toLowerCase().contains(query)
                || nullToEmpty(user.getPhone())
                .toLowerCase().contains(query));

        countLabel.setText("Показано слушателей: " + filtered.size());
    }

    private Window owner() {
        return userTable.getScene().getWindow();
    }

    private User selectedUser() {
        return userTable.getSelectionModel().getSelectedItem();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
