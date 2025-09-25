package it.unicam.cs.mpgc.jbudget123012.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

/**
 * Entità che rappresenta un budget mensile per un tag.
 */
@Entity
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    @Column(name = "budget_month")
    private YearMonth month;

    @ManyToOne
    private Tag tag;

    @Transient
    private double progress;

    @Transient
    private BigDecimal spent;

    /**
     * Costruttore vuoto richiesto da JPA.
     */
    public Budget() {}

    /**
     * Costruttore per creare un nuovo budget.
     * @param amount importo del budget
     * @param month mese di riferimento
     * @param tag tag associato al budget
     */
    public Budget(BigDecimal amount, YearMonth month, Tag tag) {
        this.amount = amount;
        this.month = month;
        this.tag = tag;
    }

    /**
     * Calcola il progresso e memorizza l'importo speso.
     * @param spent importo speso
     */
    public void calculateProgress(BigDecimal spent) {
        this.spent = spent;
        if (this.amount.compareTo(BigDecimal.ZERO) == 0) {
            this.progress = 0.0;
        } else {
            BigDecimal ratio = spent.divide(this.amount, 4, RoundingMode.HALF_UP);
            this.progress = Math.min(ratio.doubleValue(), 1.0);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Tag getTag() { return tag; }
    public void setTag(Tag tag) { this.tag = tag; }
    public YearMonth getMonth() { return month; }
    public void setMonth(YearMonth month) { this.month = month; }
    public double getProgress() { return progress; }

    /**
     * Restituisce l'importo speso calcolato.
     * @return importo speso
     */
    public BigDecimal getSpent() {
        return spent;
    }
}
