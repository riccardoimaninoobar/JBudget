package it.unicam.cs.mpgc.jbudget123012.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Rappresenta un piano di rateizzazione composto da più rate.
 * Ogni piano ha un importo totale, una data di inizio, e un numero fisso di rate.
 * Può essere associato a uno o più tag per la categorizzazione.
 */
@Entity
public class InstallmentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private int numberOfInstallments;

    @Column(nullable = false)
    private LocalDate startDate;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Installment> installments = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "plan_tags",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    public InstallmentPlan() {}

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public int getNumberOfInstallments() { return numberOfInstallments; }

    public void setNumberOfInstallments(int numberOfInstallments) { this.numberOfInstallments = numberOfInstallments; }

    public LocalDate getStartDate() { return startDate; }

    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public List<Installment> getInstallments() { return installments; }

    public void setInstallments(List<Installment> installments) { this.installments = installments; }

    public Set<Tag> getTags() { return tags; }

    public void setTags(Set<Tag> tags) { this.tags = tags; }
}
