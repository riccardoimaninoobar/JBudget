package it.unicam.cs.mpgc.jbudget123012.model;

/**
 * Rappresenta lo stato di una scadenza: in attesa di pagamento o già pagata.
 */
public enum DeadlineStatus {
    PENDING, // Scadenza in attesa di essere pagata
    PAID     // Scadenza pagata
}