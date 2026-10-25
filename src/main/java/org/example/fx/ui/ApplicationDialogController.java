package org.example.fx.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.example.fx.ServiceFactory;
import org.example.fx.util.Ui;
import org.example.model.ApplicationStatus;
import org.example.model.EducationType;
import org.example.model.TrainingApplication;
import org.example.model.User;
import org.example.service.TrainingApplicationService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public class ApplicationDialogController {

    private static final int COURSE_MAX_LENGTH = 200;
    private static final int COMMENT_MAX_LENGTH = 2000;

    @FXML
    private Label titleLabel;
    @FXML
    private ComboBox<User> userField;
    @FXML
    private TextField courseField;
    @FXML
    private DatePicker dateField;
    @FXML
    private ComboBox<ApplicationStatus> statusField;
    @FXML
    private ComboBox<EducationType> educationField;
    @FXML
    private TextArea commentField;
    @FXML
    private Label errorLabel;

    private final TrainingApplicationService applicationService =
            ServiceFactory.applicationService();

    private Stage dialog;
    private TrainingApplication existing;
    private boolean saved;

    public static boolean showAndWait(
            Window owner,
            List<User> users,
            TrainingApplication existing
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ApplicationDialogController.class.getResource(
                            "application-dialog.fxml"));

            Parent root = loader.load();

            ApplicationDialogController controller = loader.getController();

            Stage dialog = new Stage();

            controller.setup(dialog, users, existing);

            dialog.setTitle(existing == null
                    ? "Новая заявка"
                    : "Редактирование заявки № " + existing.getId());
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setMinWidth(560);
            dialog.setScene(Ui.sceneWithStyles(root));
            dialog.showAndWait();

            return controller.isSaved();

        } catch (IOException e) {
            Ui.error(owner, e);
            return false;
        }
    }

    public void setup(
            Stage dialog,
            List<User> users,
            TrainingApplication existing
    ) {
        this.dialog = dialog;
        this.existing = existing;

        userField.setItems(FXCollections.observableArrayList(users));

        if (existing == null) {
            titleLabel.setText("Новая заявка на обучение");
            userField.getSelectionModel().selectFirst();
            dateField.setValue(LocalDate.now());
            statusField.setValue(ApplicationStatus.NEW);
            educationField.setValue(EducationType.FULL_TIME);
        } else {
            titleLabel.setText("Заявка № " + existing.getId());

            userField.getItems().stream()
                    .filter(user -> user.getId() == existing.getUser().getId())
                    .findFirst()
                    .ifPresent(user -> userField.setValue(user));

            courseField.setText(existing.getCourseName());
            dateField.setValue(existing.getApplicationDate());
            statusField.setValue(existing.getStatus());
            educationField.setValue(existing.getEducationType());
            commentField.setText(existing.getComment());
        }
    }

    @FXML
    private void initialize() {
        userField.setConverter(new StringConverter<>() {
            @Override
            public String toString(User user) {
                return user == null ? "" : user.getFullName();
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });

        userField.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null
                        ? null
                        : user.getFullName() + " — " + user.getEmail());
            }
        });

        userField.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getFullName());
            }
        });

        statusField.setItems(FXCollections.observableArrayList(
                ApplicationStatus.values()));
        statusField.setConverter(new ToStringConverter<>(
                status -> status == null ? "" : status.getDisplayName()));

        educationField.setItems(FXCollections.observableArrayList(
                EducationType.values()));
        educationField.setConverter(new ToStringConverter<>(
                type -> type == null ? "" : type.getDisplayName()));
    }

    @FXML
    private void handleSave() {
        User user = userField.getValue();
        String course = courseField.getText() == null
                ? "" : courseField.getText().trim();
        LocalDate date = dateField.getValue();
        ApplicationStatus status = statusField.getValue();
        EducationType education = educationField.getValue();
        String comment = commentField.getText() == null
                ? "" : commentField.getText().trim();

        String validationError = validate(user, course, date,
                status, education, comment);

        if (validationError != null) {
            showError(validationError);
            return;
        }

        TrainingApplication application = new TrainingApplication(
                user,
                course,
                date,
                status,
                education,
                comment.isBlank() ? null : comment
        );

        try {
            if (existing == null) {
                applicationService.create(application);
            } else {
                application.setId(existing.getId());
                applicationService.update(application);
            }

            saved = true;
            dialog.close();

        } catch (Exception e) {
            showError(Ui.rootMessage(e));
        }
    }

    @FXML
    private void handleCancel() {
        dialog.close();
    }

    public boolean isSaved() {
        return saved;
    }

    private String validate(
            User user,
            String course,
            LocalDate date,
            ApplicationStatus status,
            EducationType education,
            String comment
    ) {
        if (user == null) {
            return "Выберите слушателя из списка.";
        }

        if (course.isBlank()) {
            return "Поле «Название курса» обязательно для заполнения.";
        }

        if (course.length() > COURSE_MAX_LENGTH) {
            return "Название курса не должно превышать " +
                    COURSE_MAX_LENGTH + " символов (сейчас " +
                    course.length() + ").";
        }

        if (date == null) {
            return "Укажите дату заявки.";
        }

        if (date.isAfter(LocalDate.now())) {
            return "Дата заявки не может быть в будущем.";
        }

        if (status == null) {
            return "Выберите статус заявки.";
        }

        if (education == null) {
            return "Выберите форму обучения.";
        }

        if (comment.length() > COMMENT_MAX_LENGTH) {
            return "Комментарий не должен превышать " +
                    COMMENT_MAX_LENGTH + " символов (сейчас " +
                    comment.length() + ").";
        }

        return null;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
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
