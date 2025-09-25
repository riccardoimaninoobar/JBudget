package it.unicam.cs.mpgc.jbudget123012.service;

import it.unicam.cs.mpgc.jbudget123012.model.*;
import it.unicam.cs.mpgc.jbudget123012.repository.InstallmentPlanRepository;
import it.unicam.cs.mpgc.jbudget123012.repository.InstallmentRepository;
import it.unicam.cs.mpgc.jbudget123012.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servizio per la logica di business dei piani a rate.
 * Gestisce le operazioni e gestisce le transazioni.
 */
public class InstallmentService {

    private final InstallmentPlanRepository planRepository;
    private final InstallmentRepository installmentRepository;
    private final TransactionRepository transactionRepository;
    private final EntityManagerFactory emf;

    /**
     * Costruttore per l'iniezione delle dipendenze necessarie.
     */
    public InstallmentService(InstallmentPlanRepository planRepository, InstallmentRepository installmentRepository, TransactionRepository transactionRepository, EntityManagerFactory emf) {
        this.planRepository = planRepository;
        this.installmentRepository = installmentRepository;
        this.transactionRepository = transactionRepository;
        this.emf = emf;
    }

    /**
     * Crea un nuovo piano e le sue rate in una sola operazione atomica.
     *
     * @param description descrizione del piano
     * @param totalAmount importo totale del piano
     * @param numberOfInstallments numero di rate
     * @param startDate data di inizio
     * @param tags insieme di tag associati
     * @return il piano creato e persistito
     */
    public InstallmentPlan createPlan(String description, BigDecimal totalAmount, int numberOfInstallments, LocalDate startDate, Set<Tag> tags) {
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

            InstallmentPlan plan = new InstallmentPlan();
            plan.setDescription(description);
            plan.setTotalAmount(totalAmount);
            plan.setNumberOfInstallments(numberOfInstallments);
            plan.setStartDate(startDate);
            plan.setTags(managedTags);

            BigDecimal installmentAmount = totalAmount.divide(new BigDecimal(numberOfInstallments), 2, RoundingMode.HALF_UP);
            for (int i = 0; i < numberOfInstallments; i++) {
                LocalDate dueDate = startDate.plusMonths(i);
                Installment installment = new Installment(plan, installmentAmount, dueDate);
                plan.getInstallments().add(installment);
            }

            em.persist(plan);
            em.getTransaction().commit();
            return plan;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Impossibile creare il piano a rate", e);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    /**
     * Restituisce tutti i piani a rate.
     *
     * @return lista di tutti i piani
     */
    public List<InstallmentPlan> getAllPlans() {
        return planRepository.findAll();
    }

    /**
     * Segna una rata come pagata e crea la transazione corrispondente in modo atomico
     * per garantire la coerenza dei dati.
     *
     * @param installmentId id della rata da segnare come pagata
     * @param paymentDate data del pagamento
     */
    public void markInstallmentAsPaid(Long installmentId, LocalDate paymentDate) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            TypedQuery<Installment> query = em.createQuery(
                    "SELECT i FROM Installment i " +
                            "JOIN FETCH i.plan p " +
                            "LEFT JOIN FETCH p.tags " +
                            "WHERE i.id = :id",
                    Installment.class
            );
            query.setParameter("id", installmentId);
            Installment installment = query.getSingleResult();

            if (installment.getStatus() == InstallmentStatus.PAID) {
                throw new IllegalStateException("Questa rata risulta già pagata.");
            }

            installment.setStatus(InstallmentStatus.PAID);

            Set<Tag> tagsForTransaction = new HashSet<>(installment.getPlan().getTags());

            Transaction transaction = new Transaction(
                    installment.getPlan().getDescription() + " (Rata)",
                    installment.getAmount(),
                    paymentDate,
                    TransactionType.USCITA,
                    tagsForTransaction
            );

            em.persist(transaction);

            em.getTransaction().commit();

        } catch (NoResultException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new IllegalArgumentException("Rata non trovata con ID: " + installmentId);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Impossibile segnare la rata come pagata: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Cancella una rata non ancora pagata e ricalcola i totali del piano genitore.
     *
     * @param installmentId id della rata da cancellare
     */
    public void deleteInstallment(Long installmentId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Installment installment = em.find(Installment.class, installmentId);
            if (installment == null) {
                throw new IllegalArgumentException("Rata non trovata con ID: " + installmentId);
            }
            if (installment.getStatus() == InstallmentStatus.PAID) {
                throw new IllegalStateException("Impossibile cancellare una rata già pagata.");
            }

            InstallmentPlan plan = installment.getPlan();
            if (plan != null) {
                plan.getInstallments().remove(installment);
                plan.setNumberOfInstallments(plan.getNumberOfInstallments() - 1);
                plan.setTotalAmount(plan.getTotalAmount().subtract(installment.getAmount()));
            } else {
                em.remove(installment);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Impossibile cancellare la rata", e);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }
}
