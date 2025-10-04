package it.unicam.cs.mpgc.jbudget123012.repository;

import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import it.unicam.cs.mpgc.jbudget123012.model.Transaction;
import it.unicam.cs.mpgc.jbudget123012.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Interfaccia per il repository delle transazioni.
 * Eredita i metodi base e definisce query specifiche.
 */
public interface TransactionRepository extends BaseRepository<Transaction, Long> {

    /**
     * Calcola la somma totale per un tipo di transazione,
     * ad esempio tutte le entrate o tutte le uscite.
     * @param type tipo di transazione da sommare
     * @return somma totale degli importi per il tipo specificato
     */
    BigDecimal sumAmountByType(TransactionType type);

    /**
     * Calcola la somma totale per un tipo di transazione e per un mese specifico.
     * @param type tipo di transazione da sommare
     * @param month mese di riferimento
     * @return somma totale degli importi per il tipo e mese specificati
     */
    BigDecimal sumAmountByTypeAndMonth(TransactionType type, YearMonth month);

    /**
     * Trova tutte le transazioni in un certo periodo di tempo.
     * @param startDate data di inizio intervallo
     * @param endDate data di fine intervallo
     * @return lista di transazioni nel periodo specificato
     */
    List<Transaction> findInDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Trova l'importo totale per un tag, un mese e un tipo di transazione.
     * Serve per calcolare l'avanzamento dei budget.
     * @param tag tag associato alle transazioni
     * @param month mese di riferimento
     * @param type tipo di transazione
     * @return somma totale degli importi che corrispondono ai criteri
     */
    BigDecimal findTotalAmountByTagAndMonth(Tag tag, YearMonth month, TransactionType type);

    /**
     * Trova tutte le transazioni di un mese e carica subito anche i tag.
     * @param month mese di riferimento
     * @return lista di transazioni con i tag caricati
     */
    List<Transaction> findAllByMonth(YearMonth month);

    /**
     * Trova tutte le transazioni, pre-caricando i tag e i loro parenti
     * per evitare LazyInitializationException.
     */
    List<Transaction> findAllWithTags();

    /**
     * Trova tutte le transazioni di un dato mese, pre-caricando i tag e i loro parenti
     * per evitare LazyInitializationException.
     */
    List<Transaction> findAllByMonthWithTags(YearMonth month);

}
