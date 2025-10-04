package it.unicam.cs.mpgc.jbudget123012.repository;

import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import it.unicam.cs.mpgc.jbudget123012.model.Transaction;
import it.unicam.cs.mpgc.jbudget123012.model.TransactionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Implementazione concreta per il repository delle transazioni.
 * Eredita la logica base e implementa i metodi specifici dell'interfaccia.
 */
public class TransactionRepositoryImpl extends BaseRepositoryImpl<Transaction, Long> implements TransactionRepository {

    /**
     * Costruttore che riceve la factory e la passa alla classe base.
     * @param emf EntityManagerFactory per la gestione delle entità.
     */
    public TransactionRepositoryImpl(EntityManagerFactory emf) {
        super(Transaction.class, emf);
    }

    /**
     * Sovrascrive findAll per caricare subito i tag associati
     * e ordinare le transazioni dalla più recente alla più vecchia.
     * @return lista di tutte le transazioni con i tag caricati.
     */
    @Override
    public List<Transaction> findAll() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT DISTINCT t FROM Transaction t LEFT JOIN FETCH t.tags ORDER BY t.date DESC", Transaction.class);
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Trova tutte le transazioni di un mese specifico,
     * caricando subito i tag e anche i loro genitori per ottimizzare.
     * @param month mese di riferimento.
     * @return lista di transazioni del mese con tag e genitori caricati.
     */
    @Override
    public List<Transaction> findAllByMonth(YearMonth month) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT DISTINCT t FROM Transaction t " +
                            "LEFT JOIN FETCH t.tags transaction_tag " +
                            "LEFT JOIN FETCH transaction_tag.parentTag " +
                            "WHERE YEAR(t.date) = :year AND MONTH(t.date) = :monthValue " +
                            "ORDER BY t.date DESC", Transaction.class);
            query.setParameter("year", month.getYear());
            query.setParameter("monthValue", month.getMonthValue());
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Calcola la somma totale per un tipo di transazione,
     * ad esempio il totale di tutte le entrate.
     * @param type tipo di transazione da sommare.
     * @return somma totale degli importi per il tipo specificato.
     */
    @Override
    public BigDecimal sumAmountByType(TransactionType type) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                    "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type", BigDecimal.class);
            query.setParameter("type", type);
            return query.getSingleResult();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Calcola la somma totale per tipo e per mese,
     * utile per i riepiloghi mensili.
     * @param type tipo di transazione.
     * @param month mese di riferimento.
     * @return somma totale degli importi per il tipo e mese specificati.
     */
    @Override
    public BigDecimal sumAmountByTypeAndMonth(TransactionType type, YearMonth month) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                    "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
                            "WHERE t.type = :type AND YEAR(t.date) = :year AND MONTH(t.date) = :monthValue",
                    BigDecimal.class);
            query.setParameter("type", type);
            query.setParameter("year", month.getYear());
            query.setParameter("monthValue", month.getMonthValue());
            return query.getSingleResult();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Trova tutte le transazioni in un intervallo di date.
     * @param startDate data di inizio intervallo.
     * @param endDate data di fine intervallo.
     * @return lista di transazioni comprese nell'intervallo.
     */
    @Override
    public List<Transaction> findInDateRange(LocalDate startDate, LocalDate endDate) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.date BETWEEN :startDate AND :endDate", Transaction.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Calcola quanto si è speso per un certo tag in un mese,
     * utile per i calcoli di budget.
     * @param tag tag associato.
     * @param month mese di riferimento.
     * @param type tipo di transazione.
     * @return somma totale degli importi corrispondenti.
     */
    @Override
    public BigDecimal findTotalAmountByTagAndMonth(Tag tag, YearMonth month, TransactionType type) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                    "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.tags transaction_tag " +
                            "WHERE transaction_tag = :tag " +
                            "AND t.type = :type " +
                            "AND YEAR(t.date) = :year AND MONTH(t.date) = :monthValue",
                    BigDecimal.class
            );
            query.setParameter("tag", tag);
            query.setParameter("type", type);
            query.setParameter("year", month.getYear());
            query.setParameter("monthValue", month.getMonthValue());
            return query.getSingleResult();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<Transaction> findAllWithTags() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT DISTINCT t FROM Transaction t LEFT JOIN FETCH t.tags tag LEFT JOIN FETCH tag.parentTag";
            TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<Transaction> findAllByMonthWithTags(YearMonth month) {
        EntityManager em = getEntityManager();
        try {
            LocalDate startDate = month.atDay(1);
            LocalDate endDate = month.atEndOfMonth();
            String jpql = "SELECT DISTINCT t FROM Transaction t " +
                    "LEFT JOIN FETCH t.tags tag " +
                    "LEFT JOIN FETCH tag.parentTag " +
                    "WHERE t.date BETWEEN :startDate AND :endDate";
            TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}