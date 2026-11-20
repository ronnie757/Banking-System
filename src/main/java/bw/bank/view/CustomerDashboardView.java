package bw.bank.view;

import bw.bank.controller.AccountController;
import bw.bank.model.Account;
import bw.bank.model.Customer;
import bw.bank.model.Transaction;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/** Boundary class: customer dashboard. Talks ONLY to AccountController. */
public class CustomerDashboardView {
    private final ScrollPane root = new ScrollPane();
    private final Customer customer;
    private final AccountController controller;
    private final TableView<Account> accountTable = new TableView<>();
    private final TableView<Transaction> historyTable = new TableView<>();
    private final Label historyTitle = Ui.heading("Transaction history");
    private final TextField amountField = new TextField();

    public CustomerDashboardView(MainApp app, Customer customer, AccountController controller) {
        this.customer = customer;
        this.controller = controller;

        Label welcome = new Label("Welcome, " + customer.getFullName() + "  (" + customer.getCustomerId() + ")");
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logout = new Button("Log out");
        logout.setOnAction(e -> app.showLogin());
        HBox header = new HBox(10, welcome, spacer, logout);
        header.setAlignment(Pos.CENTER_LEFT);

        accountTable.getColumns().add(Ui.<Account>column("Account No", Account::getAccountNumber, 120));
        accountTable.getColumns().add(Ui.<Account>column("Type", Account::getAccountType, 100));
        accountTable.getColumns().add(Ui.<Account>column("Branch", Account::getBranch, 110));
        accountTable.getColumns().add(Ui.<Account>column("Opened", a -> a.getOpeningDate().toString(), 100));
        accountTable.getColumns().add(Ui.<Account>column("Balance", a -> Ui.money(a.getBalance()), 130));
        accountTable.getColumns().add(Ui.<Account>column("Status", a -> a.isActive() ? "Active" : "Closed", 80));
        accountTable.setPrefHeight(160);
        accountTable.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> loadHistory());

        amountField.setPromptText("Amount (BWP)");
        Button deposit = new Button("Deposit");
        deposit.setOnAction(e -> transact(true));
        Button withdraw = new Button("Withdraw");
        withdraw.setOnAction(e -> transact(false));
        HBox transactBox = new HBox(10, new Label("Selected account:"), amountField, deposit, withdraw);
        transactBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("Savings", "Investment", "Cheque"));
        type.setPromptText("Account type");
        TextField initial = new TextField();
        initial.setPromptText("Initial deposit (Investment min 500)");
        initial.setPrefWidth(240);
        TextField employer = new TextField();
        employer.setPromptText("Employer name (Cheque)");
        TextField companyAddress = new TextField();
        companyAddress.setPromptText("Company address (Cheque)");
        employer.setDisable(true);
        companyAddress.setDisable(true);
        type.valueProperty().addListener((o, a, b) -> {
            boolean cheque = "Cheque".equals(b);
            employer.setDisable(!cheque);
            companyAddress.setDisable(!cheque);
        });
        Button open = new Button("Open account");
        open.setOnAction(e -> {
            try {
                Account a = controller.openAccount(customer.getCustomerId(), type.getValue(), initial.getText(),
                        employer.getText(), companyAddress.getText());
                initial.clear();
                employer.clear();
                companyAddress.clear();
                refresh();
                Ui.info(a.getAccountType() + " account " + a.getAccountNumber() + " opened.");
            } catch (Exception ex) {
                Ui.error(ex.getMessage());
            }
        });
        VBox openBox = new VBox(8, Ui.heading("Open a new account"),
                new HBox(10, type, initial), new HBox(10, employer, companyAddress), open);

        historyTable.getColumns().add(Ui.<Transaction>column("Date", t -> t.getDate().toLocalDate().toString(), 100));
        historyTable.getColumns().add(Ui.<Transaction>column("Time", t -> t.getDate().toLocalTime().withNano(0).toString(), 90));
        historyTable.getColumns().add(Ui.<Transaction>column("Type", t -> t.getType().name(), 110));
        historyTable.getColumns().add(Ui.<Transaction>column("Description", Transaction::getDescription, 150));
        historyTable.getColumns().add(Ui.<Transaction>column("Amount", t -> Ui.money(t.getAmount()), 120));
        historyTable.getColumns().add(Ui.<Transaction>column("Balance after", t -> Ui.money(t.getBalanceAfter()), 130));
        historyTable.setPrefHeight(200);

        VBox content = new VBox(14, header, Ui.heading("My accounts"), accountTable, transactBox,
                openBox, historyTitle, historyTable);
        content.setPadding(new Insets(20));
        root.setContent(content);
        root.setFitToWidth(true);

        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private Account selected() {
        return accountTable.getSelectionModel().getSelectedItem();
    }

    private void transact(boolean isDeposit) {
        Account sel = selected();
        if (sel == null) {
            Ui.error("Select an account in the table first.");
            return;
        }
        try {
            if (isDeposit) {
                controller.deposit(sel.getAccountNumber(), amountField.getText());
            } else {
                controller.withdraw(sel.getAccountNumber(), amountField.getText());
            }
            amountField.clear();
            refresh();
        } catch (Exception ex) {
            Ui.error(ex.getMessage());
        }
    }

    private void refresh() {
        Account sel = selected();
        String keep = sel == null ? null : sel.getAccountNumber();
        List<Account> list = controller.getAccounts(customer.getCustomerId());
        accountTable.setItems(FXCollections.observableArrayList(list));
        if (keep != null) {
            for (Account a : list) {
                if (a.getAccountNumber().equals(keep)) {
                    accountTable.getSelectionModel().select(a);
                }
            }
        }
        loadHistory();
    }

    private void loadHistory() {
        Account sel = selected();
        if (sel == null) {
            historyTable.setItems(FXCollections.observableArrayList());
            historyTitle.setText("Transaction history (select an account)");
        } else {
            historyTitle.setText("Transaction history - " + sel.getAccountNumber());
            historyTable.setItems(FXCollections.observableArrayList(controller.getHistory(sel.getAccountNumber())));
        }
    }
}
