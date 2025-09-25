package it.unicam.cs.mpgc.jbudget123012.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entità che rappresenta una singola rata di un piano di ammortamento.
 */
@Entity
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private InstallmentPlan plan;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentStatus status;

    /**
     * Costruttore vuoto richiesto da JPA.
     */
    public Installment() {}

    /**
     * Costruttore per creare una nuova rata.
     * @param plan il piano di ammortamento a cui appartiene la rata
     * @param amount l'importo della rata
     * @param dueDate la data di scadenza della rata
     */
    public Installment(InstallmentPlan plan, BigDecimal amount, LocalDate dueDate) {
        this.plan = plan;
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = InstallmentStatus.PENDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public InstallmentPlan getPlan() { return plan; }
    public void setPlan(InstallmentPlan plan) { this.plan = plan; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public InstallmentStatus getStatus() { return status; }
    public void setStatus(InstallmentStatus status) { this.status = status; }
}
