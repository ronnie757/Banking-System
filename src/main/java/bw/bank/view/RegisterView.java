package bw.bank.view;

import bw.bank.controller.AuthController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RegisterView {
    private final VBox root = new VBox(15);

    public RegisterView(AuthController auth, Runnable onBack) {
        TextField firstName = new TextField();
        TextField surname = new TextField();
        TextField address = new TextField();
        TextField phone = new TextField();
        TextField email = new TextField();
        TextField username = new TextField();
        PasswordField password = new PasswordField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        String[] labels = {"First name", "Surname", "Address", "Phone number", "Email", "Username",
                "Password (min 6)"};
        TextField[] fields = {firstName, surname, address, phone, email, username, password};
        for (int i = 0; i < labels.length; i++) {
            form.add(new Label(labels[i]), 0, i);
            form.add(fields[i], 1, i);
            fields[i].setPrefWidth(280);
        }

        Button register = new Button("Register");
        register.setDefaultButton(true);
        register.setOnAction(e -> {
            try {
                auth.registerCustomer(firstName.getText(), surname.getText(), address.getText(),
                        phone.getText(), email.getText(), username.getText(), password.getText());
                Ui.info("Customer registered successfully. You can now log in and open an account.");
                onBack.run();
            } catch (Exception ex) {
                Ui.error(ex.getMessage());
            }
        });
        Button back = new Button("Back");
        back.setOnAction(e -> onBack.run());

        root.getChildren().addAll(Ui.heading("Register new customer"), form, new HBox(10, register, back));
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
    }

    public Parent getRoot() {
        return root;
    }
}
