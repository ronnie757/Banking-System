package bw.bank.view;

import bw.bank.controller.AccountController;
import bw.bank.controller.AdminController;
import bw.bank.controller.AuthController;
import bw.bank.model.BankAdministrator;
import bw.bank.model.Customer;
import bw.bank.db.Database;
import bw.bank.db.SampleData;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage stage;
    private final AuthController authController = new AuthController();
    private final AccountController accountController = new AccountController();
    private final AdminController adminController = new AdminController();

    @Override
    public void start(Stage primaryStage) {
        Database.init();
        SampleData.seedIfEmpty();
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

    public void showCustomerDashboard(Customer customer) {
        show(new CustomerDashboardView(this, customer, accountController).getRoot());
    }

    public void showAdminDashboard(BankAdministrator admin) {
        show(new AdminDashboardView(this, admin, adminController, authController).getRoot());
    }

    private void show(Parent root) {
        stage.setScene(new Scene(root, 960, 720));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
