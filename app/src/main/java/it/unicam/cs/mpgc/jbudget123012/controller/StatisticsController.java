package it.unicam.cs.mpgc.jbudget123012.controller;

import it.unicam.cs.mpgc.jbudget123012.model.Statistic;
import it.unicam.cs.mpgc.jbudget123012.service.StatisticsService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Controller per la vista delle statistiche.
 * Gestisce il recupero e la visualizzazione dei dati finanziari.
 */
public class StatisticsController {

    private final StatisticsService statisticsService;

    @FXML private Label summaryTitleLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netBalanceLabel;
    @FXML private PieChart expensesPieChart;
    @FXML private DatePicker monthFilterPicker;

    /**
     * Crea un controller con il servizio per le statistiche.
     */
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            updateCharts(null);
        });
    }

    /**
     * Gestisce il cambio del filtro mese.
     */
    @FXML
    private void handleFilterChange() {
        LocalDate selectedDate = monthFilterPicker.getValue();
        YearMonth monthToFilter = (selectedDate != null) ? YearMonth.from(selectedDate) : null;
        Platform.runLater(() -> updateCharts(monthToFilter));
    }

    /**
     * Gestisce il reset del filtro mese.
     */
    @FXML
    private void handleResetFilter() {
        monthFilterPicker.setValue(null);
        handleFilterChange();
    }

    private void updateCharts(YearMonth month) {
        updateSummary(month);
        updatePieChart(month);
    }

    private void updateSummary(YearMonth month) {
        BigDecimal totalIncome, totalExpenses, netBalance;
        String title;

        if (month == null) {
            title = "riepilogo finanziario (totale)";
            totalIncome = statisticsService.getTotalIncome();
            totalExpenses = statisticsService.getTotalExpenses();
            netBalance = statisticsService.getNetBalance();
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ITALIAN);
            title = "riepilogo finanziario (" + month.format(formatter) + ")";
            totalIncome = statisticsService.getTotalIncome(month);
            totalExpenses = statisticsService.getTotalExpenses(month);
            netBalance = statisticsService.getNetBalance(month);
        }

        summaryTitleLabel.setText(title);
        totalIncomeLabel.setText(String.format("€ %.2f", totalIncome));
        totalExpensesLabel.setText(String.format("€ %.2f", totalExpenses));
        netBalanceLabel.setText(String.format("€ %.2f", netBalance));
        netBalanceLabel.setStyle(netBalance.compareTo(BigDecimal.ZERO) >= 0 ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private void updatePieChart(YearMonth month) {
        List<Statistic> stats = (month == null)
                ? statisticsService.getExpensesByRootTag()
                : statisticsService.getExpensesByRootTag(month);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Statistic stat : stats) {
            if (stat.getValue().compareTo(BigDecimal.ZERO) > 0) {
                pieChartData.add(new PieChart.Data(stat.getName(), stat.getValue().doubleValue()));
            }
        }

        expensesPieChart.setData(pieChartData);
        expensesPieChart.setTitle("spese per categoria");
    }
}
