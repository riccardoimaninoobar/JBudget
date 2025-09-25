package it.unicam.cs.mpgc.jbudget123012.controller;

import it.unicam.cs.mpgc.jbudget123012.view.MainApp;
import javafx.fxml.FXML;

/**
 * controller per il layout principale
 * gestisce il menu per cambiare vista
 */
public class MainController {

    private MainApp mainApp;

    public MainController() {
    }

    /**
     * metodo chiamato da mainapp per passare se stesso al controller
     * questo permette al controller di comunicare con la classe principale
     * @param mainApp l'istanza dell'applicazione principale
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void showTransactionsView() {
        mainApp.loadView("/fxml/TransactionsView.fxml");
    }

    @FXML
    private void showTagsView() {
        mainApp.loadView("/fxml/TagsView.fxml");
    }

    @FXML
    private void showBudgetView() {
        mainApp.loadView("/fxml/BudgetView.fxml");
    }

    @FXML
    private void showDeadlinesView() {
        mainApp.loadView("/fxml/DeadlineView.fxml");
    }

    @FXML
    private void showInstallmentView() {
        mainApp.loadView("/fxml/InstallmentView.fxml");
    }

    @FXML
    private void showStatisticsView() {
        mainApp.loadView("/fxml/StatisticsView.fxml");
    }
}