package bw.bank.view;

import bw.bank.controller.AuthController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoginView {
    private final VBox root = new VBox(12);

    public LoginView(MainApp app, AuthController auth) {
        Label title = new Label("BAC Banking System");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        Label subtitle = new Label("Please log in");

        ToggleGroup role = new ToggleGroup();
        RadioButton customerBtn = new RadioButton("Customer");
        RadioButton adminBtn = new RadioButton("Bank Administrator");
        customerBtn.setToggleGroup(role);
        adminBtn.setToggleGroup(role);
        customerBtn.setSelected(true);

        TextField username = new TextField();
        username.setPromptText("Username");
        username.setMaxWidth(280);
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setMaxWidth(280);

        Button login = new Button("Log in");
        login.setDefaultButton(true);
        login.setMaxWidth(280);
        login.setOnAction(e -> {
            try {
                if (adminBtn.isSelected()) {
                    app.showAdminDashboard(auth.loginAdmin(username.getText(), password.getText()));
                } else {
                    app.showCustomerDashboard(auth.loginCustomer(username.getText(), password.getText()));
                }
            } catch (Exception ex) {
                Ui.error(ex.getMessage());
            }
        });

        Hyperlink register = new Hyperlink("New customer? Register here");
        register.setOnAction(e -> app.showRegister(app::showLogin));

        root.getChildren().addAll(title, subtitle, new HBox(15, customerBtn, adminBtn),
                username, password, login, register);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
    }

    public Parent getRoot() {
        return root;
    }
}
