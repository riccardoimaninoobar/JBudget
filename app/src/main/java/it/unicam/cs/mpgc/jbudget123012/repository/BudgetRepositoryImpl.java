package it.unicam.cs.mpgc.jbudget123012.repository;

import it.unicam.cs.mpgc.jbudget123012.model.Budget;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Implementazione concreta del repository per i budget.
 * Estende {@link BaseRepositoryImpl} e implementa {@link BudgetRepository}.
 */
public class BudgetRepositoryImpl extends BaseRepositoryImpl<Budget, Long> implements BudgetRepository {

    /**
     * Costruisce un repository per l'entità {@link Budget}.
     *
     * @param emf factory per ottenere gli EntityManager
     */
    public BudgetRepositoryImpl(EntityManagerFactory emf) {
        super(Budget.class, emf);
    }

    /**
     * Recupera tutti i budget ordinati per mese in ordine decrescente.
     *
     * @return lista di budget ordinata per mese
     */
    @Override
    public List<Budget> findAll() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Budget> query = em.createQuery(
                    "SELECT b FROM Budget b ORDER BY b.month DESC", Budget.class
            );
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
