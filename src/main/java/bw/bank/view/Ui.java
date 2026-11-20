package bw.bank.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;

import java.math.BigDecimal;
import java.util.function.Function;

final class Ui {
    private Ui() { }

    static void error(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message == null ? "Unexpected error." : message);
        a.setHeaderText(null);
        a.setTitle("Error");
        a.showAndWait();
    }

    static void info(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message);
        a.setHeaderText(null);
        a.setTitle("Success");
        a.showAndWait();
    }

    static Label heading(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        return l;
    }

    static String money(BigDecimal value) {
        return String.format("BWP %,.2f", value);
    }

    static <S> TableColumn<S, String> column(String title, Function<S, String> getter, double width) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setCellValueFactory(d -> new SimpleStringProperty(getter.apply(d.getValue())));
        col.setPrefWidth(width);
        return col;
    }
}
