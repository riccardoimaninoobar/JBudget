package it.unicam.cs.mpgc.jbudget123012.repository;

import it.unicam.cs.mpgc.jbudget123012.model.Deadline;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Implementazione concreta del repository per le scadenze.
 * Estende {@link BaseRepositoryImpl} e implementa {@link DeadlineRepository}.
 */
public class DeadlineRepositoryImpl extends BaseRepositoryImpl<Deadline, Long> implements DeadlineRepository {

    /**
     * Costruisce un repository per l'entità {@link Deadline}.
     *
     * @param emf factory per ottenere gli EntityManager
     */
    public DeadlineRepositoryImpl(EntityManagerFactory emf) {
        super(Deadline.class, emf);
    }

    /**
     * Recupera tutte le scadenze ordinate per data di scadenza ascendente.
     *
     * @return lista di scadenze ordinate per data di scadenza
     */
    @Override
    public List<Deadline> findAllByOrderByDueDateAsc() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Deadline> query = em.createQuery(
                    "SELECT d FROM Deadline d ORDER BY d.dueDate ASC", Deadline.class
            );
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
