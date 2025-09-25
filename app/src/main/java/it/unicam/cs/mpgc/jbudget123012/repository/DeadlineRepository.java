package it.unicam.cs.mpgc.jbudget123012.repository;

import it.unicam.cs.mpgc.jbudget123012.model.Deadline;
import java.util.List;

/**
 * Interfaccia per il repository delle scadenze.
 * Estende il repository di base per avere i metodi CRUD
 * e definisce query specifiche per le scadenze.
 */
public interface DeadlineRepository extends BaseRepository<Deadline, Long> {

    /**
     * Trova tutte le scadenze ordinate per data di scadenza in modo crescente.
     *
     * @return una lista di scadenze ordinate per data
     */
    List<Deadline> findAllByOrderByDueDateAsc();
}
