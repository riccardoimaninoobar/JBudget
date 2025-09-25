package it.unicam.cs.mpgc.jbudget123012.repository;

import java.util.List;
import java.util.Optional;

/**
 * Interfaccia generica per le operazioni CRUD di base.
 *
 * @param <T>  il tipo dell'entità
 * @param <ID> il tipo dell'id dell'entità
 */
public interface BaseRepository<T, ID> {

    /**
     * Salva o aggiorna un'entità.
     *
     * @param entity entità da salvare o aggiornare
     * @return l'entità salvata o aggiornata
     */
    T save(T entity);

    /**
     * Trova un'entità tramite il suo id.
     *
     * @param id identificatore dell'entità
     * @return Optional contenente l'entità se trovata, altrimenti vuoto
     */
    Optional<T> findById(ID id);

    /**
     * Trova tutte le entità di un tipo.
     *
     * @return lista di tutte le entità
     */
    List<T> findAll();

    /**
     * Cancella un'entità specifica.
     *
     * @param entity entità da cancellare
     */
    void delete(T entity);

    /**
     * Cancella un'entità dato il suo id.
     *
     * @param id identificatore dell'entità da cancellare
     */
    void deleteById(ID id);
}
