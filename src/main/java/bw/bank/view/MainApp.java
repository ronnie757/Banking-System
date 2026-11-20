package bw.bank.view;

import bw.bank.controller.AuthController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage stage;
    private final AuthController authController = new AuthController();

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("BAC Banking System");
        showLogin();
        stage.show();
    }

    public void showLogin() {
        show(new LoginView(this, authController).getRoot());
    }

    public void showRegister(Runnable onBack) {
        show(new RegisterView(authController, onBack).getRoot());
    }

    private void show(Parent root) {
        stage.setScene(new Scene(root, 960, 720));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
