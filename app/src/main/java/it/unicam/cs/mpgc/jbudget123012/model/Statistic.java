package it.unicam.cs.mpgc.jbudget123012.model;

import java.math.BigDecimal;

/**
 * Classe per contenere un dato statistico, come un nome e un valore.
 */
public class Statistic {
    private final String name;
    private final BigDecimal value;

    /**
     * Costruttore per creare una statistica.
     * @param name il nome della statistica
     * @param value il valore della statistica
     */
    public Statistic(String name, BigDecimal value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getValue() {
        return value;
    }
}
