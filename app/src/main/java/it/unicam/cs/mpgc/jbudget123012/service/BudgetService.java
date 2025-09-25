package it.unicam.cs.mpgc.jbudget123012.service;

import it.unicam.cs.mpgc.jbudget123012.model.Budget;
import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import it.unicam.cs.mpgc.jbudget123012.model.TransactionType;
import it.unicam.cs.mpgc.jbudget123012.repository.BudgetRepository;
import it.unicam.cs.mpgc.jbudget123012.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Servizio per la logica di business relativa ai budget.
 * Gestisce le operazioni tra i vari repository e gestisce le transazioni.
 */
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final EntityManagerFactory emf;

    /**
     * Costruttore per l'iniezione delle dipendenze necessarie.
     */
    public BudgetService(BudgetRepository budgetRepository, TransactionRepository transactionRepository, EntityManagerFactory emf) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.emf = emf;
    }

    /**
     * Trova tutti i budget e calcola il loro progresso attuale
     * interrogando le transazioni associate.
     */
    public List<Budget> findAllWithProgress() {
        List<Budget> budgets = budgetRepository.findAll();
        for (Budget budget : budgets) {
            BigDecimal spent = transactionRepository.findTotalAmountByTagAndMonth(
                    budget.getTag(), budget.getMonth(), TransactionType.USCITA
            );
            budget.calculateProgress(spent);
        }
        return budgets;
    }

    /**
     * Crea un nuovo budget, gestendo la transazione manualmente
     * per garantire l'integrità dei dati.
     */
    public Budget createBudget(BigDecimal amount, YearMonth month, Tag tag) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Budget budget = new Budget(amount, month, tag);

            // Assicura che il tag sia gestito dal contesto di persistenza corrente
            if (tag != null && !em.contains(tag)) {
                Tag managedTag = em.find(Tag.class, tag.getId());
                if (managedTag == null) {
                    throw new IllegalArgumentException("Impossibile creare un budget con un tag non esistente (ID: " + tag.getId() + ")");
                }
                budget.setTag(managedTag);
            }

            em.persist(budget);
            em.getTransaction().commit();
            return budget;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Impossibile creare il budget", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Elimina un budget dato il suo ID, gestendo la transazione manualmente.
     */
    public void deleteBudget(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Budget budgetToDelete = em.find(Budget.class, id);
            if (budgetToDelete != null) {
                em.remove(budgetToDelete);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Impossibile eliminare il budget con id " + id, e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}