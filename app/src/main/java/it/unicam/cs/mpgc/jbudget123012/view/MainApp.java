package it.unicam.cs.mpgc.jbudget123012.view;

import it.unicam.cs.mpgc.jbudget123012.controller.*;
import it.unicam.cs.mpgc.jbudget123012.repository.*;
import it.unicam.cs.mpgc.jbudget123012.service.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Classe principale dell'applicazione JavaFX.
 * Gestisce l'avvio, l'inizializzazione dei servizi e la navigazione tra le viste.
 */
public class MainApp extends Application {

    private EntityManagerFactory emf;
    private TagService tagService;
    private TransactionService transactionService;
    private DeadlineService deadlineService;
    private BudgetService budgetService;
    private StatisticsService statisticsService;
    private InstallmentService installmentService;
    private BorderPane mainLayout;

    /**
     * Iniezione delle dipendenze
     */
    @Override
    public void init() {
        try {
            emf = Persistence.createEntityManagerFactory("jbudget-pu");

            TagRepository tagRepository = new TagRepositoryImpl(emf);
            TransactionRepository transactionRepository = new TransactionRepositoryImpl(emf);
            DeadlineRepository deadlineRepository = new DeadlineRepositoryImpl(emf);
            BudgetRepository budgetRepository = new BudgetRepositoryImpl(emf);
            InstallmentPlanRepository installmentPlanRepository = new InstallmentPlanRepositoryImpl(emf);
            InstallmentRepository installmentRepository = new InstallmentRepositoryImpl(emf);

            tagService = new TagService(tagRepository, emf);
            transactionService = new TransactionService(transactionRepository, tagService, emf);
            deadlineService = new DeadlineService(deadlineRepository, transactionRepository, emf);
            budgetService = new BudgetService(budgetRepository, transactionRepository, emf);
            installmentService = new InstallmentService(installmentPlanRepository, installmentRepository, transactionRepository, emf);
            statisticsService = new StatisticsService(transactionRepository, tagService);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore critico di inizializzazione", "Impossibile connettersi al database", e.getMessage());
            Platform.exit();
        }
    }

    /**
     * Avvia l'interfaccia grafica,
     * caricando la finestra principale e impostando la prima vista.
     *
     * @param primaryStage finestra principale dell'applicazione
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            mainLayout = loader.load();

            MainController mainController = loader.getController();
            mainController.setMainApp(this);

            loadView("/fxml/TransactionsView.fxml");

            primaryStage.setTitle("JBudget Application");
            primaryStage.setScene(new Scene(mainLayout));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore critico", "Impossibile avviare l'interfaccia utente", e.getMessage());
        }
    }

    /**
     * Carica una vista FXML nel centro del layout principale.
     *
     * @param fxmlPath il percorso del file FXML da caricare
     */
    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(controllerClass -> {
                try {
                    if (controllerClass == TransactionsController.class) {
                        return new TransactionsController(transactionService, tagService);
                    } else if (controllerClass == TagsController.class) {
                        return new TagsController(tagService);
                    } else if (controllerClass == BudgetController.class) {
                        return new BudgetController(budgetService, tagService);
                    } else if (controllerClass == DeadlineController.class) {
                        return new DeadlineController(deadlineService, tagService);
                    } else if (controllerClass == StatisticsController.class) {
                        return new StatisticsController(statisticsService);
                    } else if (controllerClass == InstallmentController.class) {
                        return new InstallmentController(installmentService, tagService);
                    }
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException("Impossibile creare il controller " + controllerClass.getName(), ex);
                }
            });

            Parent view = loader.load();
            mainLayout.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Errore di caricamento", "Impossibile caricare la vista " + fxmlPath, e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        System.out.println("Applicazione chiusa e risorse rilasciate");
    }

    /**
     * Metodo di utilità per mostrare un popup di errore.
     *
     * @param title   titolo della finestra di dialogo
     * @param header  intestazione del messaggio
     * @param content contenuto del messaggio
     */
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Punto di ingresso standard per un'applicazione Java.
     * Lancia l'applicazione.
     *
     * @param args argomenti da linea di comando
     */
    public static void main(String[] args) {
        launch(args);
    }
}
