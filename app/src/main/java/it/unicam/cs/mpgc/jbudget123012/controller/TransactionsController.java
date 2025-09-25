package it.unicam.cs.mpgc.jbudget123012.controller;

import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import it.unicam.cs.mpgc.jbudget123012.model.Transaction;
import it.unicam.cs.mpgc.jbudget123012.model.TransactionType;
import it.unicam.cs.mpgc.jbudget123012.service.TagService;
import it.unicam.cs.mpgc.jbudget123012.service.TransactionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller per la vista delle transazioni.
 * Gestisce la logica per mostrare, creare e cancellare i movimenti.
 */
public class TransactionsController {

    private final TransactionService transactionService;
    private final TagService tagService;

    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private final ObservableList<Tag> tagList = FXCollections.observableArrayList();

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, BigDecimal> amountColumn;
    @FXML private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML private TableColumn<Transaction, TransactionType> typeColumn;
    @FXML private TableColumn<Transaction, Set<Tag>> tagsColumn;

    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private ChoiceBox<TransactionType> typeChoiceBox;
    @FXML private ListView<Tag> tagsListView;
    @FXML private Button deleteButton;

    /**
     * Costruttore con iniezione delle dipendenze.
     */
    public TransactionsController(TransactionService transactionService, TagService tagService) {
        this.transactionService = transactionService;
        this.tagService = tagService;
    }

    @FXML
    public void initialize() {
        setupDataBindings();
        setupTableColumns();
        setupFormControls();
        loadInitialData();

        transactionsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> deleteButton.setDisable(newSelection == null)
        );
    }

    private void setupDataBindings() {
        transactionsTable.setItems(transactionList);
        tagsListView.setItems(tagList);
    }

    private void setupTableColumns() {
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        tagsColumn.setCellValueFactory(new PropertyValueFactory<>("tags"));

        tagsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Set<Tag> tags, boolean empty) {
                super.updateItem(tags, empty);
                if (empty || tags == null || tags.isEmpty()) {
                    setText(null);
                } else {
                    String tagNames = tags.stream()
                            .map(Tag::getName)
                            .sorted()
                            .collect(Collectors.joining(", "));
                    setText(tagNames);
                }
            }
        });
    }

    private void setupFormControls() {
        typeChoiceBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        typeChoiceBox.setValue(TransactionType.USCITA);
        tagsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tagsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    private void loadInitialData() {
        Platform.runLater(() -> {
            transactionList.setAll(transactionService.findAll());
            tagList.setAll(tagService.getAllTags());
        });
    }

    /**
     * Gestisce il salvataggio di una nuova transazione.
     */
    @FXML
    private void handleSaveTransaction() {
        try {
            String description = descriptionField.getText();
            BigDecimal amount = new BigDecimal(amountField.getText().replace(',', '.'));
            LocalDate date = datePicker.getValue();
            TransactionType type = typeChoiceBox.getValue();
            Set<Tag> selectedTags = new HashSet<>(tagsListView.getSelectionModel().getSelectedItems());

            if (description.isBlank() || date == null || selectedTags.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "dati mancanti", "compila tutti i campi e seleziona almeno un tag");
                return;
            }

            Transaction newTransaction = new Transaction(description, amount, date, type, selectedTags);
            Transaction savedTransaction = transactionService.saveTransaction(newTransaction);
            transactionList.add(0, savedTransaction);
            clearForm();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "errore formato", "l'importo non è un numero valido");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "errore", "si è verificato un errore imprevisto");
            e.printStackTrace();
        }
    }

    /**
     * Gestisce l'eliminazione di una transazione selezionata.
     */
    @FXML
    private void handleDeleteTransaction() {
        Transaction selectedTransaction = transactionsTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("conferma eliminazione");
        alert.setHeaderText("sei sicuro di voler eliminare la transazione: '" + selectedTransaction.getDescription() + "'");
        alert.setContentText("l'operazione non si può annullare");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                transactionService.deleteTransaction(selectedTransaction.getId());
                transactionList.remove(selectedTransaction);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "errore", "impossibile eliminare la transazione");
                e.printStackTrace();
            }
        }
    }

    private void clearForm() {
        descriptionField.clear();
        amountField.clear();
        datePicker.setValue(null);
        typeChoiceBox.setValue(TransactionType.USCITA);
        tagsListView.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
