package it.unicam.cs.mpgc.jbudget123012.controller;

import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import it.unicam.cs.mpgc.jbudget123012.service.TagService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

/**
 * Controller per la vista dei tag.
 * Gestisce la logica per creare e cancellare tag gerarchici.
 */
public class TagsController {

    private final TagService tagService;
    private final ObservableList<Tag> allTagsList = FXCollections.observableArrayList();

    @FXML private TreeTableView<Tag> tagTreeTableView;
    @FXML private TreeTableColumn<Tag, String> nameColumn;
    @FXML private TreeTableColumn<Tag, String> parentColumn;
    @FXML private TextField tagNameField;
    @FXML private ComboBox<Tag> parentTagComboBox;
    @FXML private Button deleteButton;

    /**
     * Crea il controller con il servizio dei tag.
     */
    public TagsController(TagService tagService) {
        this.tagService = tagService;
    }

    @FXML
    public void initialize() {
        setupTreeTableView();
        parentTagComboBox.setItems(allTagsList);
        loadInitialData();

        tagTreeTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> deleteButton.setDisable(newSelection == null)
        );
    }

    private void setupTreeTableView() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue().getName()));
        parentColumn.setCellValueFactory(cellData -> {
            Tag parent = cellData.getValue().getValue().getParentTag();
            return new SimpleStringProperty(parent != null ? parent.getName() : "N/A");
        });
        tagTreeTableView.setShowRoot(false);
    }

    private void loadInitialData() {
        allTagsList.setAll(tagService.getAllTags());
        tagTreeTableView.setRoot(tagService.getTagTree());
    }

    /**
     * Gestisce l'aggiunta di un nuovo tag.
     */
    @FXML
    private void handleAddTag() {
        String tagName = tagNameField.getText();
        Tag parentTag = parentTagComboBox.getValue();

        if (tagName.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "input non valido", "il nome del tag non può essere vuoto");
            return;
        }

        try {
            tagService.createTag(tagName, parentTag);
            Platform.runLater(() -> {
                allTagsList.setAll(tagService.getAllTags());
                tagTreeTableView.setRoot(tagService.getTagTree());
                clearForm();
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "errore", "impossibile creare il tag " + e.getMessage());
        }
    }

    /**
     * Gestisce l'eliminazione di un tag selezionato.
     */
    @FXML
    private void handleDeleteTag() {
        TreeItem<Tag> selectedItem = tagTreeTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("conferma eliminazione");
        confirmation.setHeaderText("sei sicuro di voler eliminare il tag '" + selectedItem.getValue().getName() + "'");
        confirmation.setContentText("l'operazione non si può annullare e cancellerà anche i tag figli");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                tagService.deleteTag(selectedItem.getValue().getId());
                Platform.runLater(() -> {
                    allTagsList.setAll(tagService.getAllTags());
                    tagTreeTableView.setRoot(tagService.getTagTree());
                });
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "errore", "impossibile eliminare il tag " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        tagNameField.clear();
        parentTagComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
