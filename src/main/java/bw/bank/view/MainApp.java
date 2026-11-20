package bw.bank.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("BAC Banking System");
        stage.setScene(new Scene(new StackPane(new Label("JavaFX is working!")), 500, 300));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
