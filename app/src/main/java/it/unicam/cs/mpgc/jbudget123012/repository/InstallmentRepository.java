package it.unicam.cs.mpgc.jbudget123012.repository;

import it.unicam.cs.mpgc.jbudget123012.model.Installment;
import java.util.Optional;

/**
 * Interfaccia per il repository delle singole rate.
 * Eredita i metodi base e definisce query specifiche.
 */
public interface InstallmentRepository extends BaseRepository<Installment, Long> {

    /**
     * Trova una rata tramite id e carica subito anche il piano e i tag associati.
     * Questo evita errori di LazyInitializationException.
     *
     * @param id l'id della rata da cercare
     * @return un Optional con la rata e i suoi dati collegati
     */
    Optional<Installment> findByIdWithPlanAndTags(Long id);
}
