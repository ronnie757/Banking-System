package bw.bank.view;

import bw.bank.controller.AdminController;
import bw.bank.controller.AuthController;
import bw.bank.model.Account;
import bw.bank.model.BankAdministrator;
import bw.bank.model.Customer;
import bw.bank.model.Transaction;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Boundary class: administrator dashboard. Talks ONLY to AdminController. */
public class AdminDashboardView {
    private final ScrollPane root = new ScrollPane();
    private final AdminController controller;
    private final TextField searchField = new TextField();
    private final TableView<Customer> customerTable = new TableView<>();
    private final TableView<Account> accountTable = new TableView<>();
    private final TableView<Transaction> historyTable = new TableView<>();
    private final TextField address = new TextField();
    private final TextField phone = new TextField();
    private final TextField email = new TextField();

    public AdminDashboardView(MainApp app, BankAdministrator admin, AdminController controller, AuthController auth) {
        this.controller = controller;

        Label welcome = new Label("Administrator: " + admin.getName());
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button registerBtn = new Button("Register customer");
        registerBtn.setOnAction(e -> app.showRegister(() -> app.showAdminDashboard(admin)));
        Button interestBtn = new Button("Pay monthly interest");
        interestBtn.setOnAction(e -> {
            try {
                int n = controller.payMonthlyInterest();
                Ui.info("Interest paid to " + n + " account(s).");
                loadAccounts();
            } catch (Exception ex) {
                Ui.error(ex.getMessage());
            }
        });
        Button logout = new Button("Log out");
        logout.setOnAction(e -> app.showLogin());
        HBox header = new HBox(10, welcome, spacer, registerBtn, interestBtn, logout);
        header.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Search by customer ID, name or account number");
        searchField.setPrefWidth(340);
        Button search = new Button("Search");
        search.setDefaultButton(true);
        search.setOnAction(e -> loadCustomers());
        Button showAll = new Button("Show all");
        showAll.setOnAction(e -> {
            searchField.clear();
            loadCustomers();
        });

        customerTable.getColumns().add(Ui.<Customer>column("ID", Customer::getCustomerId, 90));
        customerTable.getColumns().add(Ui.<Customer>column("Name", Customer::getFullName, 160));
        customerTable.getColumns().add(Ui.<Customer>column("Username", Customer::getUsername, 100));
        customerTable.getColumns().add(Ui.<Customer>column("Phone", Customer::getPhoneNumber, 110));
        customerTable.getColumns().add(Ui.<Customer>column("Email", Customer::getEmail, 180));
        customerTable.getColumns().add(Ui.<Customer>column("Address", Customer::getAddress, 180));
        customerTable.setPrefHeight(190);
        customerTable.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> {
            if (c != null) {
                address.setText(c.getAddress());
                phone.setText(c.getPhoneNumber());
                email.setText(c.getEmail());
            }
            loadAccounts();
        });

        address.setPromptText("Address");
        phone.setPromptText("Phone");
        email.setPromptText("Email");
        Button update = new Button("Update details");
        update.setOnAction(e -> {
            Customer c = customerTable.getSelectionModel().getSelectedItem();
            if (c == null) {
                Ui.error("Select a customer first.");
                return;
            }
            try {
                controller.updateCustomer(c.getCustomerId(), address.getText(), phone.getText(), email.getText());
                Ui.info("Customer details updated.");
                loadCustomers();
            } catch (Exception ex) {
                Ui.error(ex.getMessage());
            }
        });

        accountTable.getColumns().add(Ui.<Account>column("Account No", Account::getAccountNumber, 120));
        accountTable.getColumns().add(Ui.<Account>column("Type", Account::getAccountType, 100));
        accountTable.getColumns().add(Ui.<Account>column("Opened", a -> a.getOpeningDate().toString(), 100));
        accountTable.getColumns().add(Ui.<Account>column("Balance", a -> Ui.money(a.getBalance()), 130));
        accountTable.getColumns().add(Ui.<Account>column("Status", a -> a.isActive() ? "Active" : "Closed", 80));
        accountTable.setPrefHeight(130);
        accountTable.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> loadHistory());

        Button close = new Button("Close account");
        close.setOnAction(e -> changeStatus(false));
        Button reopen = new Button("Re-open account");
        reopen.setOnAction(e -> changeStatus(true));

        historyTable.getColumns().add(Ui.<Transaction>column("Date", t -> t.getDate().toLocalDate().toString(), 100));
        historyTable.getColumns().add(Ui.<Transaction>column("Type", t -> t.getType().name(), 110));
        historyTable.getColumns().add(Ui.<Transaction>column("Amount", t -> Ui.money(t.getAmount()), 120));
        historyTable.getColumns().add(Ui.<Transaction>column("Balance after", t -> Ui.money(t.getBalanceAfter()), 130));
        historyTable.setPrefHeight(150);

        VBox content = new VBox(12, header, Ui.heading("Customers"),
                new HBox(10, searchField, search, showAll), customerTable,
                new HBox(10, address, phone, email, update),
                Ui.heading("Accounts of selected customer"), accountTable, new HBox(10, close, reopen),
                Ui.heading("Transactions of selected account"), historyTable);
        content.setPadding(new Insets(20));
        root.setContent(content);
        root.setFitToWidth(true);

        loadCustomers();
    }

    public Parent getRoot() {
        return root;
    }

    private void loadCustomers() {
        customerTable.setItems(FXCollections.observableArrayList(controller.searchCustomers(searchField.getText())));
    }

    private void loadAccounts() {
        Customer c = customerTable.getSelectionModel().getSelectedItem();
        accountTable.setItems(c == null ? FXCollections.observableArrayList()
                : FXCollections.observableArrayList(controller.getAccounts(c.getCustomerId())));
        loadHistory();
    }

    private void loadHistory() {
        Account a = accountTable.getSelectionModel().getSelectedItem();
        historyTable.setItems(a == null ? FXCollections.observableArrayList()
                : FXCollections.observableArrayList(controller.getHistory(a.getAccountNumber())));
    }

    private void changeStatus(boolean active) {
        Account a = accountTable.getSelectionModel().getSelectedItem();
        if (a == null) {
            Ui.error("Select an account first.");
            return;
        }
        try {
            if (active) {
                controller.reopenAccount(a.getAccountNumber());
            } else {
                controller.closeAccount(a.getAccountNumber());
            }
            loadAccounts();
        } catch (Exception ex) {
            Ui.error(ex.getMessage());
        }
    }
}
