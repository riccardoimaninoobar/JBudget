package it.unicam.cs.mpgc.jbudget123012.controller;

import it.unicam.cs.mpgc.jbudget123012.model.*;
import it.unicam.cs.mpgc.jbudget123012.service.InstallmentService;
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
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Controller per la vista dei piani a rate.
 * Gestisce l'interazione tra l'utente e i dati dei piani e delle rate.
 */
public class InstallmentController {

    private final InstallmentService installmentService;
    private final TagService tagService;

    private final ObservableList<InstallmentPlan> planList = FXCollections.observableArrayList();
    private final ObservableList<Installment> installmentList = FXCollections.observableArrayList();
    private final ObservableList<Tag> tagList = FXCollections.observableArrayList();

    @FXML private TextField descriptionField;
    @FXML private TextField totalAmountField;
    @FXML private Spinner<Integer> installmentsSpinner;
    @FXML private DatePicker startDatePicker;
    @FXML private ListView<Tag> tagsListView;
    @FXML private TableView<InstallmentPlan> plansTableView;
    @FXML private TableColumn<InstallmentPlan, String> planDescriptionColumn;
    @FXML private TableColumn<InstallmentPlan, BigDecimal> planTotalAmountColumn;
    @FXML private TableColumn<InstallmentPlan, Integer> planInstallmentsColumn;
    @FXML private TableView<Installment> installmentsTableView;
    @FXML private TableColumn<Installment, String> installmentDueDateColumn;
    @FXML private TableColumn<Installment, BigDecimal> installmentAmountColumn;
    @FXML private TableColumn<Installment, String> installmentStatusColumn;
    @FXML private Button markAsPaidButton;
    @FXML private Button deleteInstallmentButton;

    public InstallmentController(InstallmentService installmentService, TagService tagService) {
        this.installmentService = installmentService;
        this.tagService = tagService;
    }

    @FXML
    public void initialize() {
        setupTablesAndForm();
        setupListeners();
        loadInitialData();
    }

    private void setupTablesAndForm() {
        plansTableView.setItems(planList);
        installmentsTableView.setItems(installmentList);
        tagsListView.setItems(tagList);

        planDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        planTotalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        planInstallmentsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfInstallments"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        installmentDueDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDueDate().format(formatter)));
        installmentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        installmentStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        installmentStatusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.equals(InstallmentStatus.PAID.toString())
                            ? "-fx-text-fill: green; -fx-font-weight: bold;"
                            : "-fx-text-fill: orange;");
                }
            }
        });

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 12);
        installmentsSpinner.setValueFactory(valueFactory);
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
        plansTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            Platform.runLater(() -> {
                if (newSelection != null) {
                    installmentList.setAll(newSelection.getInstallments());
                } else {
                    installmentList.clear();
                }
            });
        });

        installmentsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isNothingSelected = newSelection == null;
            boolean isPaid = !isNothingSelected && newSelection.getStatus() == InstallmentStatus.PAID;
            markAsPaidButton.setDisable(isNothingSelected || isPaid);
            deleteInstallmentButton.setDisable(isNothingSelected || isPaid);
        });
    }

    private void loadInitialData() {
        planList.setAll(installmentService.getAllPlans());
        tagList.setAll(tagService.getAllTags());
    }

    /**
     * Gestisce il click sul bottone per creare un nuovo piano a rate.
     */
    @FXML
    private void handleCreatePlan() {
        try {
            String description = descriptionField.getText();
            BigDecimal totalAmount = new BigDecimal(totalAmountField.getText().replace(',', '.'));
            int numberOfInstallments = installmentsSpinner.getValue();
            LocalDate startDate = startDatePicker.getValue();
            Set<Tag> selectedTags = new HashSet<>(tagsListView.getSelectionModel().getSelectedItems());

            if (description.isBlank() || startDate == null || selectedTags.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "dati mancanti", "compila tutti i campi e seleziona almeno un tag");
                return;
            }

            installmentService.createPlan(description, totalAmount, numberOfInstallments, startDate, selectedTags);
            showAlert(Alert.AlertType.INFORMATION, "fatto", "nuovo piano creato");

            Platform.runLater(() -> {
                planList.setAll(installmentService.getAllPlans());
                clearForm();
            });

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "errore formato", "l'importo non è un numero");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "errore", "impossibile creare il piano");
            e.printStackTrace();
        }
    }

    /**
     * Gestisce il click sul bottone per segnare una rata come pagata.
     */
    @FXML
    private void handleMarkAsPaid() {
        Installment selectedInstallment = installmentsTableView.getSelectionModel().getSelectedItem();
        if (selectedInstallment == null) return;

        TextInputDialog dialog = new TextInputDialog(LocalDate.now().toString());
        dialog.setTitle("conferma pagamento rata");
        dialog.setHeaderText("stai per pagare una rata\nquesto creerà una transazione");
        dialog.setContentText("data del pagamento (YYYY-MM-DD):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(dateString -> {
            try {
                LocalDate paymentDate = LocalDate.parse(dateString);
                installmentService.markInstallmentAsPaid(selectedInstallment.getId(), paymentDate);
                showAlert(Alert.AlertType.INFORMATION, "fatto", "rata pagata e transazione creata");

                Platform.runLater(() -> planList.setAll(installmentService.getAllPlans()));

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "errore", "impossibile completare l'operazione");
                e.printStackTrace();
            }
        });
    }

    /**
     * Gestisce il click sul bottone per eliminare una rata.
     */
    @FXML
    private void handleDeleteInstallment() {
        Installment selectedInstallment = installmentsTableView.getSelectionModel().getSelectedItem();
        if (selectedInstallment == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("conferma eliminazione rata");
        alert.setHeaderText("sei sicuro di voler eliminare questa rata");
        alert.setContentText("l'operazione non si può annullare");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                installmentService.deleteInstallment(selectedInstallment.getId());
                showAlert(Alert.AlertType.INFORMATION, "fatto", "rata eliminata");

                Platform.runLater(() -> planList.setAll(installmentService.getAllPlans()));

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "errore", "impossibile eliminare la rata");
                e.printStackTrace();
            }
        }
    }

    private void clearForm() {
        descriptionField.clear();
        totalAmountField.clear();
        startDatePicker.setValue(null);
        tagsListView.getSelectionModel().clearSelection();
        installmentsSpinner.getValueFactory().setValue(12);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
