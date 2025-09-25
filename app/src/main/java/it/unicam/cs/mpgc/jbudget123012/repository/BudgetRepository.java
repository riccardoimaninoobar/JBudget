package it.unicam.cs.mpgc.jbudget123012.repository;

import it.unicam.cs.mpgc.jbudget123012.model.Budget;

/**
 * Interfaccia per le operazioni di persistenza relative all'entità Budget.
 * Eredita le operazioni CRUD di base e può definire
 * metodi di query specifici per i budget.
 */
public interface BudgetRepository extends BaseRepository<Budget, Long> {

}
