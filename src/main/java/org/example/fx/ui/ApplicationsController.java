package org.example.fx.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.example.fx.ServiceFactory;
import org.example.fx.util.Ui;
import org.example.model.ApplicationStatus;
import org.example.model.EducationType;
import org.example.model.TrainingApplication;
import org.example.model.User;
import org.example.service.TrainingApplicationService;
import org.example.service.UserService;
import org.example.util.ExcelExporter;

import java.io.File;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ApplicationsController {

    @FXML
    private TableView<TrainingApplication> applicationTable;
    @FXML
    private TableColumn<TrainingApplication, Integer> idColumn;
    @FXML
    private TableColumn<TrainingApplication, String> userColumn;
    @FXML
    private TableColumn<TrainingApplication, String> courseColumn;
    @FXML
    private TableColumn<TrainingApplication, LocalDate> dateColumn;
    @FXML
    private TableColumn<TrainingApplication, String> statusColumn;
    @FXML
    private TableColumn<TrainingApplication, String> educationColumn;
    @FXML
    private TableColumn<TrainingApplication, String> commentColumn;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<ApplicationStatus> statusFilter;
    @FXML
    private ComboBox<EducationType> educationFilter;
    @FXML
    private DatePicker dateFromFilter;
    @FXML
    private DatePicker dateToFilter;
    @FXML
    private ComboBox<String> sortBox;
    @FXML
    private Label countLabel;

    private final TrainingApplicationService applicationService =
            ServiceFactory.applicationService();
    private final UserService userService = ServiceFactory.userService();

    private final ObservableList<TrainingApplication> source =
            FXCollections.observableArrayList();
    private FilteredList<TrainingApplication> filtered;

    private final Map<String, Comparator<TrainingApplication>> sortOptions =
            new LinkedHashMap<>();

    @FXML
    private void initialize() {
        configureColumns();
        configureFilters();
        configureSort();
        configureTable();

        loadApplications();
    }

    private void configureColumns() {
        idColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getId()));

        userColumn.setCellValueFactory(data ->
                new SimpleStringProperty(userName(data.getValue())));

        courseColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullToEmpty(
                        data.getValue().getCourseName())));

        dateColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getApplicationDate()));

        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus()
                        .getDisplayName()));

        educationColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEducationType()
                        .getDisplayName()));

        commentColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullToEmpty(
                        data.getValue().getComment())));
    }

    private void configureFilters() {
        statusFilter.getItems().add(null);
        statusFilter.getItems().addAll(ApplicationStatus.values());
        statusFilter.setPromptText("Все статусы");
        statusFilter.setConverter(new ToStringConverter<>(
                status -> status == null ? "" : status.getDisplayName()));

        educationFilter.getItems().add(null);
        educationFilter.getItems().addAll(EducationType.values());
        educationFilter.setPromptText("Все формы");
        educationFilter.setConverter(new ToStringConverter<>(
                type -> type == null ? "" : type.getDisplayName()));

        searchField.textProperty().addListener((o, old, value) -> applyFilters());
        statusFilter.valueProperty().addListener((o, old, value) -> applyFilters());
        educationFilter.valueProperty().addListener((o, old, value) -> applyFilters());
        dateFromFilter.valueProperty().addListener((o, old, value) -> applyFilters());
        dateToFilter.valueProperty().addListener((o, old, value) -> applyFilters());
    }

    private void configureSort() {
        sortOptions.put("По номеру заявки",
                Comparator.comparingInt(TrainingApplication::getId));
        sortOptions.put("По дате (сначала новые)",
                Comparator.comparing(
                                TrainingApplication::getApplicationDate)
                        .reversed());
        sortOptions.put("По дате (сначала старые)",
                Comparator.comparing(
                                TrainingApplication::getApplicationDate));
        sortOptions.put("По курсу (А → Я)",
                Comparator.comparing(
                        TrainingApplication::getCourseName,
                        String.CASE_INSENSITIVE_ORDER));
        sortOptions.put("По курсу (Я → А)",
                Comparator.comparing(
                                TrainingApplication::getCourseName,
                                String.CASE_INSENSITIVE_ORDER)
                        .reversed());
        sortOptions.put("По слушателю (А → Я)",
                Comparator.comparing(this::userName,
                        String.CASE_INSENSITIVE_ORDER));

        sortBox.getItems().addAll(sortOptions.keySet());
        sortBox.getSelectionModel().selectFirst();
    }

    private void configureTable() {
        filtered = new FilteredList<>(source, application -> true);

        SortedList<TrainingApplication> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(
                applicationTable.comparatorProperty());
        applicationTable.setItems(sorted);

        applicationTable.setRowFactory(table -> {
            TableRow<TrainingApplication> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleView();
                }
            });

            return row;
        });
    }

    @FXML
    private void handleRefresh() {
        loadApplications();
    }

    private void loadApplications() {
        try {
            source.setAll(applicationService.findAll());
            applyFilters();
        } catch (Exception e) {
            Ui.error(owner(), e);
        }
    }

    @FXML
    private void handleAdd() {
        openApplicationDialog(null);
    }

    @FXML
    private void handleEdit() {
        TrainingApplication selected = selectedApplication();

        if (selected == null) {
            Ui.info(owner(), "Выберите заявку в таблице.");
            return;
        }

        openApplicationDialog(selected);
    }

    @FXML
    private void handleView() {
        TrainingApplication selected = selectedApplication();

        if (selected == null) {
            Ui.info(owner(), "Выберите заявку в таблице.");
            return;
        }

        Ui.info(owner(),
                "Заявка № " + selected.getId() +
                        "\nСлушатель: " + userName(selected) +
                        "\nEmail: " + (selected.getUser() == null
                        ? "—" : nullToEmpty(selected.getUser().getEmail())) +
                        "\nТелефон: " + (selected.getUser() == null
                        ? "—" : nullToEmpty(selected.getUser().getPhone())) +
                        "\nКурс: " + nullToEmpty(selected.getCourseName()) +
                        "\nДата: " + selected.getApplicationDate() +
                        "\nСтатус: " + selected.getStatus().getDisplayName() +
                        "\nФорма обучения: " +
                        selected.getEducationType().getDisplayName() +
                        "\nКомментарий: " +
                        (selected.getComment() == null
                                ? "—" : selected.getComment()));
    }

    @FXML
    private void handleDelete() {
        TrainingApplication selected = selectedApplication();

        if (selected == null) {
            Ui.info(owner(), "Выберите заявку в таблице.");
            return;
        }

        boolean confirmed = Ui.confirm(owner(),
                "Удалить заявку № " + selected.getId() + "?\n\n" +
                        "Слушатель: " + userName(selected) +
                        "\nКурс: " + selected.getCourseName() +
                        "\n\nЭто действие нельзя отменить.");

        if (!confirmed) {
            return;
        }

        try {
            applicationService.delete(selected.getId());
            loadApplications();
            Ui.info(owner(), "Заявка № " + selected.getId() + " удалена.");
        } catch (Exception e) {
            Ui.error(owner(), e);
        }
    }

    @FXML
    private void handleSortChange() {
        Comparator<TrainingApplication> comparator =
                sortOptions.get(sortBox.getValue());

        if (comparator == null) {
            return;
        }

        applicationTable.getSortOrder().clear();
        source.sort(comparator);
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilter.setValue(null);
        educationFilter.setValue(null);
        dateFromFilter.setValue(null);
        dateToFilter.setValue(null);
    }

    @FXML
    private void handleExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить заявки в Excel");
        chooser.setInitialFileName("training_applications.xlsx");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Книга Excel (*.xlsx)", "*.xlsx"));

        File file = chooser.showSaveDialog(owner());

        if (file == null) {
            return;
        }

        try {
            ExcelExporter.export(filtered, file);
            Ui.info(owner(),
                    "Сохранено заявок: " + filtered.size() +
                            "\nФайл: " + file.getAbsolutePath());
        } catch (Exception e) {
            Ui.error(owner(), e);
        }
    }

    private void openApplicationDialog(TrainingApplication existing) {
        List<User> users;

        try {
            users = userService.findAll();
        } catch (Exception e) {
            Ui.error(owner(), e);
            return;
        }

        if (users.isEmpty()) {
            Ui.info(owner(),
                    "Сначала добавьте слушателя на вкладке «Слушатели».");
            return;
        }

        if (ApplicationDialogController.showAndWait(owner(), users, existing)) {
            loadApplications();
        }
    }

    private void applyFilters() {
        if (filtered == null) {
            return;
        }

        String query = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase();

        ApplicationStatus status = statusFilter.getValue();
        EducationType education = educationFilter.getValue();

        LocalDate dateFrom = dateFromFilter.getValue();
        LocalDate dateTo = dateToFilter.getValue();

        boolean rangeValid = dateFrom == null || dateTo == null
                || !dateFrom.isAfter(dateTo);

        markInvalid(dateFromFilter, !rangeValid);
        markInvalid(dateToFilter, !rangeValid);

        boolean useDates = rangeValid &&
                (dateFrom != null || dateTo != null);

        final LocalDate from = dateFrom;
        final LocalDate to = dateTo;

        filtered.setPredicate(application -> {

            boolean matchesText = query.isBlank()
                    || userName(application).toLowerCase().contains(query)
                    || nullToEmpty(application.getCourseName())
                    .toLowerCase().contains(query)
                    || nullToEmpty(application.getComment())
                    .toLowerCase().contains(query);

            boolean matchesStatus =
                    status == null || application.getStatus() == status;

            boolean matchesEducation =
                    education == null
                            || application.getEducationType() == education;

            boolean matchesDates = true;

            if (useDates) {
                LocalDate date = application.getApplicationDate();

                if (from != null && date.isBefore(from)) {
                    matchesDates = false;
                }

                if (to != null && date.isAfter(to)) {
                    matchesDates = false;
                }
            }

            return matchesText && matchesStatus
                    && matchesEducation && matchesDates;
        });

        updateCount();
    }

    private void updateCount() {
        countLabel.setText("Показано заявок: " + filtered.size()
                + " из " + source.size());
    }

    private void markInvalid(DatePicker picker, boolean invalid) {
        boolean has = picker.getStyleClass().contains("invalid-field");

        if (invalid && !has) {
            picker.getStyleClass().add("invalid-field");
        } else if (!invalid && has) {
            picker.getStyleClass().remove("invalid-field");
        }
    }

    private Window owner() {
        return applicationTable.getScene().getWindow();
    }

    private TrainingApplication selectedApplication() {
        return applicationTable.getSelectionModel().getSelectedItem();
    }

    private String userName(TrainingApplication application) {
        return application.getUser() == null
                ? "" : nullToEmpty(application.getUser().getFullName());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static final class ToStringConverter<T>
            extends StringConverter<T> {

        private final Function<T, String> toString;

        private ToStringConverter(Function<T, String> toString) {
            this.toString = toString;
        }

        @Override
        public String toString(T object) {
            return object == null ? null : toString.apply(object);
        }

        @Override
        public T fromString(String string) {
            return null;
        }
    }
}
