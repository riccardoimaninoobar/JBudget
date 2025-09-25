package it.unicam.cs.mpgc.jbudget123012.service;

import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import it.unicam.cs.mpgc.jbudget123012.model.Transaction;
import it.unicam.cs.mpgc.jbudget123012.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TagService tagService;
    private final EntityManagerFactory emf;

    /**
     * Costruttore che accetta tutte le dipendenze necessarie, inclusa la EntityManagerFactory
     * per la gestione manuale delle transazioni.
     *
     * @param transactionRepository repository per le transazioni
     * @param tagService servizio per la gestione dei tag
     * @param emf factory per creare EntityManager
     */
    public TransactionService(TransactionRepository transactionRepository, TagService tagService, EntityManagerFactory emf) {
        this.transactionRepository = transactionRepository;
        this.tagService = tagService;
        this.emf = emf;
    }

    /**
     * Operazione di sola lettura che delega al repository.
     *
     * @return lista di tutte le transazioni
     */
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    /**
     * Salva o aggiorna una transazione in un blocco di transazione manuale.
     * Garantisce che l'operazione sia atomica.
     *
     * @param transaction la transazione da salvare
     * @return la transazione salvata e gestita da JPA
     */
    public Transaction saveTransaction(Transaction transaction) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Set<Tag> managedTags = new HashSet<>();
            if (transaction.getTags() != null) {
                for (Tag tag : transaction.getTags()) {
                    Tag managedTag = em.find(Tag.class, tag.getId());
                    if (managedTag != null) {
                        managedTags.add(managedTag);
                    }
                }
            }
            transaction.setTags(managedTags);

            Transaction savedTransaction = em.merge(transaction);

            em.getTransaction().commit();
            return savedTransaction;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Impossibile salvare la transazione", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Cancella una transazione dato il suo ID in modo sicuro e atomico.
     *
     * @param id l'ID della transazione da cancellare
     */
    public void deleteTransaction(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Transaction transactionToDelete = em.find(Transaction.class, id);
            if (transactionToDelete != null) {
                em.remove(transactionToDelete);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Impossibile cancellare la transazione con id " + id, e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
