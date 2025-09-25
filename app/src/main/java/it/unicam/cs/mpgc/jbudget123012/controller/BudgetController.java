package it.unicam.cs.mpgc.jbudget123012.controller;

import it.unicam.cs.mpgc.jbudget123012.model.Budget;
import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import it.unicam.cs.mpgc.jbudget123012.service.BudgetService;
import it.unicam.cs.mpgc.jbudget123012.service.TagService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

/**
 * controller per la vista dei budget
 * gestisce l'interazione tra l'utente e i dati dei budget
 */
public class BudgetController {

    private final BudgetService budgetService;
    private final TagService tagService;

    private final ObservableList<Budget> budgetList = FXCollections.observableArrayList();
    private final ObservableList<Tag> tagList = FXCollections.observableArrayList();

    @FXML private TableView<Budget> budgetTableView;
    @FXML private TableColumn<Budget, String> budgetTagColumn;
    @FXML private TableColumn<Budget, BigDecimal> budgetAmountColumn;
    @FXML private TableColumn<Budget, YearMonth> budgetMonthColumn;
    @FXML private TableColumn<Budget, BigDecimal> budgetSpentColumn;
    @FXML private TableColumn<Budget, Double> budgetProgressColumn;
    @FXML private ComboBox<Tag> tagComboBox;
    @FXML private TextField amountField;
    @FXML private DatePicker monthPicker;
    @FXML private Button createBudgetButton;
    @FXML private Button deleteBudgetButton;

    public BudgetController(BudgetService budgetService, TagService tagService) {
        this.budgetService = budgetService;
        this.tagService = tagService;
    }

    @FXML
    public void initialize() {
        setupTable();
        setupForm();
        loadInitialData();

        budgetTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> deleteBudgetButton.setDisable(newSelection == null)
        );
    }

    /**
     * configura la tabella e le sue colonne
     */
    private void setupTable() {
        budgetTableView.setItems(budgetList);


        budgetTagColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTag().getName()));
        budgetAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        budgetMonthColumn.setCellValueFactory(new PropertyValueFactory<>("month"));


        budgetSpentColumn.setCellValueFactory(new PropertyValueFactory<>("spent"));
        budgetSpentColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("€ %.2f", item));
            }
        });


        budgetProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        budgetProgressColumn.setCellFactory(column -> new TableCell<>() {

            private final ProgressBar progressBar = new ProgressBar();
            private final Label remainingLabel = new Label();
            private final HBox graphicContainer = new HBox(5, progressBar, remainingLabel);

            {
                graphicContainer.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Double progressItem, boolean empty) {
                super.updateItem(progressItem, empty);
                if (empty || progressItem == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Budget budget = getTableRow().getItem();
                    progressBar.setProgress(progressItem);
                    progressBar.setStyle(progressItem >= 1.0 ? "-fx-accent: red;" : "-fx-accent: #007bff;");

                    BigDecimal budgetAmount = budget.getAmount();
                    BigDecimal spentAmount = budget.getSpent();
                    BigDecimal remainingAmount = budgetAmount.subtract(spentAmount);

                    remainingLabel.setText(String.format("€ %.2f", remainingAmount));

                    remainingLabel.setStyle(remainingAmount.compareTo(BigDecimal.ZERO) >= 0
                            ? "-fx-text-fill: green;"
                            : "-fx-text-fill: red; -fx-font-weight: bold;");

                    setGraphic(graphicContainer);
                }
            }
        });
    }

    /**
     * configura i campi del form per inserire un nuovo budget
     */
    private void setupForm() {
        tagComboBox.setItems(tagList);

        tagComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        tagComboBox.setButtonCell(tagComboBox.getCellFactory().call(null));
    }

    /**
     * carica i dati iniziali dal database
     */
    private void loadInitialData() {
        refreshBudgetTable();
        tagList.setAll(tagService.getAllTags());
    }

    /**
     * gestisce il click sul bottone per salvare un budget
     */
    @FXML
    private void handleSaveBudget() {
        try {
            Tag selectedTag = tagComboBox.getValue();
            if (selectedTag == null) {
                showAlert(Alert.AlertType.WARNING, "dati mancanti", "seleziona un tag");
                return;
            }

            if (monthPicker.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "dati mancanti", "seleziona un mese");
                return;
            }
            YearMonth month = YearMonth.from(monthPicker.getValue());
            BigDecimal amount = new BigDecimal(amountField.getText().replace(',', '.'));

            budgetService.createBudget(amount, month, selectedTag);


            Platform.runLater(() -> {
                refreshBudgetTable();
                clearForm();
            });

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "errore formato", "l'importo non è un numero");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "errore", "impossibile creare il budget");
            e.printStackTrace();
        }
    }

    /**
     * gestisce il click sul bottone per eliminare un budget
     */
    @FXML
    private void handleDeleteBudget() {
        Budget selectedBudget = budgetTableView.getSelectionModel().getSelectedItem();
        if (selectedBudget == null) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("conferma eliminazione");
        confirmation.setHeaderText("sei sicuro di voler eliminare questo budget");
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            budgetService.deleteBudget(selectedBudget.getId());
            Platform.runLater(this::refreshBudgetTable);
        }
    }


    private void refreshBudgetTable() {
        budgetList.setAll(budgetService.findAllWithProgress());
    }


    private void clearForm() {
        tagComboBox.getSelectionModel().clearSelection();
        amountField.clear();
        monthPicker.setValue(null);
    }

    /**
     * metodo di utilità per mostrare un popup all'utente
     * evita di scrivere lo stesso codice più volte
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}