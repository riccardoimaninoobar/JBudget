package it.unicam.cs.mpgc.jbudget123012.controller;

import it.unicam.cs.mpgc.jbudget123012.model.Deadline;
import it.unicam.cs.mpgc.jbudget123012.model.DeadlineStatus;
import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import it.unicam.cs.mpgc.jbudget123012.service.DeadlineService;
import it.unicam.cs.mpgc.jbudget123012.service.TagService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
 * Controller per la vista delle scadenze.
 * Gestisce l'interazione tra l'utente e i dati delle scadenze.
 */
public class DeadlineController {

    private final DeadlineService deadlineService;
    private final TagService tagService;

    private final ObservableList<Deadline> deadlineList = FXCollections.observableArrayList();
    private final ObservableList<Tag> tagList = FXCollections.observableArrayList();

    @FXML private TableView<Deadline> deadlinesTableView;
    @FXML private TableColumn<Deadline, String> descriptionColumn;
    @FXML private TableColumn<Deadline, BigDecimal> amountColumn;
    @FXML private TableColumn<Deadline, LocalDate> dueDateColumn;
    @FXML private TableColumn<Deadline, Set<Tag>> tagsColumn;
    @FXML private TableColumn<Deadline, String> statusColumn;

    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private DatePicker dueDatePicker;
    @FXML private ListView<Tag> tagsListView;

    @FXML private Button markAsPaidButton;
    @FXML private Button deleteButton;

    /**
     * Costruisce un controller con i servizi necessari.
     *
     * @param deadlineService servizio per la gestione delle scadenze
     * @param tagService servizio per la gestione dei tag
     */
    public DeadlineController(DeadlineService deadlineService, TagService tagService) {
        this.deadlineService = deadlineService;
        this.tagService = tagService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFormControls();
        setupListeners();
        loadInitialData();
    }

    private void setupTableColumns() {
        deadlinesTableView.setItems(deadlineList);

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.equals(DeadlineStatus.PAID.toString())
                            ? "-fx-text-fill: green; -fx-font-weight: bold;"
                            : "-fx-text-fill: orange;");
                }
            }
        });

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
        tagsListView.setItems(tagList);
        tagsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tagsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    private void setupListeners() {
        deadlinesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isNothingSelected = newSelection == null;
            boolean isPaid = !isNothingSelected && newSelection.getStatus() == DeadlineStatus.PAID;
            markAsPaidButton.setDisable(isNothingSelected || isPaid);
            deleteButton.setDisable(isNothingSelected || isPaid);
        });
    }

    private void loadInitialData() {
        deadlineList.setAll(deadlineService.getAllDeadlines());
        tagList.setAll(tagService.getAllTags());
    }

    /**
     * Gestisce il click sul bottone per creare una nuova scadenza.
     */
    @FXML
    private void handleCreateDeadline() {
        try {
            String description = descriptionField.getText();
            BigDecimal amount = new BigDecimal(amountField.getText().replace(',', '.'));
            LocalDate dueDate = dueDatePicker.getValue();
            Set<Tag> selectedTags = new HashSet<>(tagsListView.getSelectionModel().getSelectedItems());

            if (description.isBlank() || dueDate == null || selectedTags.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "dati mancanti", "compila tutti i campi e seleziona almeno un tag");
                return;
            }

            Deadline newDeadline = deadlineService.createDeadline(description, amount, dueDate, selectedTags);
            deadlineList.add(0, newDeadline);
            clearForm();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "errore formato", "l'importo non è un numero");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "errore", "impossibile creare la scadenza");
            e.printStackTrace();
        }
    }

    /**
     * Gestisce il click sul bottone per segnare una scadenza come pagata.
     */
    @FXML
    private void handleMarkAsPaid() {
        Deadline selected = deadlinesTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog(LocalDate.now().toString());
        dialog.setTitle("conferma pagamento");
        dialog.setHeaderText("stai per pagare una scadenza\nquesto creerà una transazione");
        dialog.setContentText("data del pagamento (YYYY-MM-DD):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(dateString -> {
            try {
                LocalDate paymentDate = LocalDate.parse(dateString);
                deadlineService.markDeadlineAsPaid(selected.getId(), paymentDate);
                showAlert(Alert.AlertType.INFORMATION, "fatto", "scadenza pagata e transazione creata");
                Platform.runLater(() -> deadlineList.setAll(deadlineService.getAllDeadlines()));
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "errore", "impossibile completare l'operazione");
                e.printStackTrace();
            }
        });
    }

    /**
     * Gestisce il click sul bottone per eliminare una scadenza.
     */
    @FXML
    private void handleDelete() {
        Deadline selected = deadlinesTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("conferma eliminazione");
        alert.setHeaderText("sei sicuro di voler eliminare questa scadenza");
        alert.setContentText("l'operazione non si può annullare");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                deadlineService.deleteDeadline(selected.getId());
                deadlineList.remove(selected);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "errore", "impossibile eliminare la scadenza");
                e.printStackTrace();
            }
        }
    }

    private void clearForm() {
        descriptionField.clear();
        amountField.clear();
        dueDatePicker.setValue(null);
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
