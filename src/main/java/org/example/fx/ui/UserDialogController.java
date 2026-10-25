package org.example.fx.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.fx.ServiceFactory;
import org.example.fx.util.Ui;
import org.example.model.User;
import org.example.service.UserService;

import java.io.IOException;
import java.util.regex.Pattern;

public class UserDialogController {

    private static final int NAME_MAX_LENGTH = 150;
    private static final int EMAIL_MAX_LENGTH = 150;
    private static final int PHONE_MAX_LENGTH = 30;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+()\\-\\s]*$");

    @FXML
    private Label titleLabel;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private Label errorLabel;

    private final UserService userService = ServiceFactory.userService();

    private Stage dialog;
    private User existing;
    private boolean saved;

    public static boolean showAndWait(Window owner, User existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    UserDialogController.class.getResource(
                            "user-dialog.fxml"));

            Parent root = loader.load();

            UserDialogController controller = loader.getController();

            Stage dialog = new Stage();

            controller.setup(dialog, existing);

            dialog.setTitle(existing == null
                    ? "Новый слушатель"
                    : "Редактирование слушателя");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setMinWidth(520);
            dialog.setScene(Ui.sceneWithStyles(root));
            dialog.showAndWait();

            return controller.isSaved();

        } catch (IOException e) {
            Ui.error(owner, e);
            return false;
        }
    }

    public void setup(Stage dialog, User existing) {
        this.dialog = dialog;
        this.existing = existing;

        if (existing == null) {
            titleLabel.setText("Новый слушатель");
        } else {
            titleLabel.setText("Слушатель № " + existing.getId());
            fullNameField.setText(existing.getFullName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
        }
    }

    @FXML
    private void handleSave() {
        String fullName = fullNameField.getText() == null
                ? "" : fullNameField.getText().trim();
        String email = emailField.getText() == null
                ? "" : emailField.getText().trim();
        String phone = phoneField.getText() == null
                ? "" : phoneField.getText().trim();

        String validationError = validate(fullName, email, phone);

        if (validationError != null) {
            showError(validationError);
            return;
        }

        User user = new User(
                fullName,
                email.toLowerCase(),
                phone.isBlank() ? null : phone
        );

        try {
            if (existing == null) {
                userService.create(user);
            } else {
                user.setId(existing.getId());
                userService.update(user);
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

    private String validate(String fullName, String email, String phone) {
        if (fullName.isBlank()) {
            return "Поле «ФИО» обязательно для заполнения.";
        }

        if (fullName.length() > NAME_MAX_LENGTH) {
            return "ФИО не должно превышать " + NAME_MAX_LENGTH +
                    " символов (сейчас " + fullName.length() + ").";
        }

        if (email.isBlank()) {
            return "Поле «Email» обязательно для заполнения.";
        }

        if (email.length() > EMAIL_MAX_LENGTH) {
            return "Email не должен превышать " + EMAIL_MAX_LENGTH +
                    " символов (сейчас " + email.length() + ").";
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Email введён некорректно. Пример: ivanov@mail.ru";
        }

        if (phone.length() > PHONE_MAX_LENGTH) {
            return "Телефон не должен превышать " + PHONE_MAX_LENGTH +
                    " символов (сейчас " + phone.length() + ").";
        }

        if (!phone.isBlank() && !PHONE_PATTERN.matcher(phone).matches()) {
            return "Телефон может содержать только цифры, пробелы " +
                    "и символы + ( ) -. Пример: +79990000001";
        }

        return null;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
