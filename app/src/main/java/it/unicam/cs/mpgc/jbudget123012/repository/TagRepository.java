package it.unicam.cs.mpgc.jbudget123012.repository;

import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia per il repository dei tag.
 * Eredita i metodi CRUD di base e aggiunge query specifiche per i tag.
 */
public interface TagRepository extends BaseRepository<Tag, Long> {

    /**
     * Trova un tag tramite il suo nome esatto.
     *
     * @param name il nome da cercare
     * @return un optional con il tag se trovato
     */
    Optional<Tag> findByName(String name);

    /**
     * Trova un tag tramite il suo nome normalizzato in minuscolo.
     *
     * @param normalizedName il nome in minuscolo da cercare
     * @return un optional con il tag se trovato
     */
    Optional<Tag> findByNormalizedName(String normalizedName);

    /**
     * Trova tutti i tag e carica subito anche i loro genitori
     * per evitare errori di lazy loading.
     *
     * @return una lista di tag con i genitori già caricati
     */
    List<Tag> findAllWithParents();
}
