package it.unicam.cs.mpgc.jbudget123012.service;

import it.unicam.cs.mpgc.jbudget123012.model.Statistic;
import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import it.unicam.cs.mpgc.jbudget123012.model.Transaction;
import it.unicam.cs.mpgc.jbudget123012.model.TransactionType;
import it.unicam.cs.mpgc.jbudget123012.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servizio per calcolare le statistiche dell'applicazione.
 * Gestisce le chiamate ai repository per aggregare i dati.
 */
public class StatisticsService {

    private final TransactionRepository transactionRepository;
    private final TagService tagService;

    /**
     * Costruttore per l'iniezione delle dipendenze necessarie.
     */
    public StatisticsService(TransactionRepository transactionRepository, TagService tagService) {
        this.transactionRepository = transactionRepository;
        this.tagService = tagService;
    }

    public BigDecimal getTotalIncome() {
        return transactionRepository.sumAmountByType(TransactionType.ENTRATA);
    }

    public BigDecimal getTotalExpenses() {
        return transactionRepository.sumAmountByType(TransactionType.USCITA);
    }

    public BigDecimal getNetBalance() {
        return getTotalIncome().subtract(getTotalExpenses());
    }

    /**
     * Calcola le spese totali raggruppate per tag radice.
     */
    public List<Statistic> getExpensesByRootTag() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        return calculateExpensesByRootTag(allTransactions);
    }

    public BigDecimal getTotalIncome(YearMonth month) {
        return transactionRepository.sumAmountByTypeAndMonth(TransactionType.ENTRATA, month);
    }

    public BigDecimal getTotalExpenses(YearMonth month) {
        return transactionRepository.sumAmountByTypeAndMonth(TransactionType.USCITA, month);
    }

    public BigDecimal getNetBalance(YearMonth month) {
        return getTotalIncome(month).subtract(getTotalExpenses(month));
    }

    /**
     * Calcola le spese di un mese specifico raggruppate per tag radice.
     */
    public List<Statistic> getExpensesByRootTag(YearMonth month) {
        List<Transaction> monthlyTransactions = transactionRepository.findAllByMonth(month);
        return calculateExpensesByRootTag(monthlyTransactions);
    }

    /**
     * Metodo privato per calcolare le spese per tag radice.
     * Accetta una lista di transazioni per non duplicare il codice.
     *
     * @param transactions la lista di transazioni su cui calcolare le statistiche
     * @return una lista di oggetti Statistic pronti per l'interfaccia grafica
     */
    private List<Statistic> calculateExpensesByRootTag(List<Transaction> transactions) {
        Map<Tag, BigDecimal> expensesByRootTag = new HashMap<>();
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.USCITA) {
                for (Tag tag : transaction.getTags()) {
                    Tag rootTag = tagService.findRootTag(tag);
                    if (rootTag != null) {
                        expensesByRootTag.merge(rootTag, transaction.getAmount(), BigDecimal::add);
                    }
                }
            }
        }
        return expensesByRootTag.entrySet().stream()
                .map(entry -> new Statistic(entry.getKey().getName(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
