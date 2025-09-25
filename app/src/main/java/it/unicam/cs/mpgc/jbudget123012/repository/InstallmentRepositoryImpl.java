package it.unicam.cs.mpgc.jbudget123012.repository;

import it.unicam.cs.mpgc.jbudget123012.model.Installment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.Optional;

/**
 * Implementazione concreta del repository per le singole rate.
 * Eredita la logica di base e implementa l'interfaccia specifica {@link InstallmentRepository}.
 */
public class InstallmentRepositoryImpl extends BaseRepositoryImpl<Installment, Long> implements InstallmentRepository {

    /**
     * Costruttore che accetta la {@link EntityManagerFactory}
     * e la passa alla classe base per completare la configurazione.
     *
     * @param emf la factory per creare entity manager
     */
    public InstallmentRepositoryImpl(EntityManagerFactory emf) {
        super(Installment.class, emf);
    }

    /**
     * Implementazione del metodo per trovare una rata con i suoi dati collegati caricati subito
     * (plan e tags), per evitare errori di lazy loading.
     *
     * @param id l'id della rata da cercare
     * @return un Optional con la rata e i suoi dati collegati, vuoto se non trovato
     */
    @Override
    public Optional<Installment> findByIdWithPlanAndTags(Long id) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Installment> query = em.createQuery(
                    "SELECT i FROM Installment i " +
                            "JOIN FETCH i.plan p " +
                            "LEFT JOIN FETCH p.tags " +
                            "WHERE i.id = :id", Installment.class);
            query.setParameter("id", id);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
