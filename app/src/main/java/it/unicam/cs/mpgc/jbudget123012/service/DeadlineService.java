package it.unicam.cs.mpgc.jbudget123012.service;

import it.unicam.cs.mpgc.jbudget123012.model.*;
import it.unicam.cs.mpgc.jbudget123012.repository.DeadlineRepository;
import it.unicam.cs.mpgc.jbudget123012.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servizio per la logica di business relativa alle scadenze.
 * Gestisce le operazioni e gestisce le transazioni.
 */
public class DeadlineService {

    private final DeadlineRepository deadlineRepository;
    private final TransactionRepository transactionRepository;
    private final EntityManagerFactory emf;

    public DeadlineService(DeadlineRepository deadlineRepository, TransactionRepository transactionRepository, EntityManagerFactory emf) {
        this.deadlineRepository = deadlineRepository;
        this.transactionRepository = transactionRepository;
        this.emf = emf;
    }

    /**
     * Restituisce tutte le scadenze.
     *
     * @return lista di tutte le scadenze
     */
    public List<Deadline> getAllDeadlines() {
        return deadlineRepository.findAll();
    }

    /**
     * Crea una nuova scadenza, gestendo la transazione manualmente.
     *
     * @param description descrizione della scadenza
     * @param amount importo della scadenza
     * @param dueDate data di scadenza
     * @param tags insieme di tag associati
     * @return la scadenza creata e persistita
     */
    public Deadline createDeadline(String description, BigDecimal amount, LocalDate dueDate, Set<Tag> tags) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Set<Tag> managedTags = new HashSet<>();
            if (tags != null) {
                for (Tag tag : tags) {
                    Tag managedTag = em.find(Tag.class, tag.getId());
                    if (managedTag == null) {
                        throw new IllegalArgumentException("Tag non esistente con ID: " + tag.getId());
                    }
                    managedTags.add(managedTag);
                }
            }

            Deadline deadline = new Deadline();
            deadline.setDescription(description);
            deadline.setAmount(amount);
            deadline.setDueDate(dueDate);
            deadline.setStatus(DeadlineStatus.PENDING);
            deadline.setTags(managedTags);

            em.persist(deadline);
            em.getTransaction().commit();
            return deadline;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Impossibile creare la scadenza", e);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    /**
     * Segna una scadenza come pagata e crea la transazione corrispondente
     * in una singola operazione atomica per garantire la coerenza dei dati.
     *
     * @param deadlineId id della scadenza da segnare come pagata
     * @param paymentDate data del pagamento
     */
    public void markDeadlineAsPaid(Long deadlineId, LocalDate paymentDate) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            TypedQuery<Deadline> query = em.createQuery(
                    "SELECT d FROM Deadline d LEFT JOIN FETCH d.tags WHERE d.id = :id",
                    Deadline.class
            );
            query.setParameter("id", deadlineId);
            Deadline deadline = query.getSingleResult();

            if (deadline.getStatus() == DeadlineStatus.PAID) {
                throw new IllegalStateException("Questa scadenza risulta già pagata.");
            }

            deadline.setStatus(DeadlineStatus.PAID);

            Set<Tag> tagsForTransaction = new HashSet<>(deadline.getTags());

            Transaction transaction = new Transaction(
                    deadline.getDescription() + " (Scadenza)",
                    deadline.getAmount(),
                    paymentDate,
                    TransactionType.USCITA,
                    tagsForTransaction
            );

            em.persist(transaction);
            em.getTransaction().commit();

        } catch (NoResultException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new IllegalArgumentException("Scadenza non trovata con ID: " + deadlineId);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Impossibile segnare la scadenza come pagata", e);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    /**
     * Elimina una scadenza, gestendo la transazione manualmente.
     *
     * @param deadlineId id della scadenza da eliminare
     */
    public void deleteDeadline(Long deadlineId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Deadline deadline = em.find(Deadline.class, deadlineId);
            if (deadline != null) {
                em.remove(deadline);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Impossibile eliminare la scadenza", e);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }
}
