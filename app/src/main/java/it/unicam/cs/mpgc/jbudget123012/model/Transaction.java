package it.unicam.cs.mpgc.jbudget123012.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entità che rappresenta una transazione finanziaria, associabile a uno o più tag.
 * Può essere un'entrata o un'uscita e contiene informazioni su importo, data e descrizione.
 */
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private BigDecimal amount;
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "transaction_tags",
            joinColumns = @JoinColumn(name = "transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    /**
     * Costruttore vuoto richiesto da JPA.
     */
    public Transaction() {}

    /**
     * Costruttore per creare una nuova transazione.
     *
     * @param description descrizione della transazione
     * @param amount importo
     * @param date data
     * @param type tipo di transazione (ENTRATA o USCITA)
     * @param tags insieme di tag associati
     */
    public Transaction(String description, BigDecimal amount, LocalDate date, TransactionType type, Set<Tag> tags) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.tags = tags;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    public TransactionType getType() { return type; }

    public void setType(TransactionType type) { this.type = type; }

    public Set<Tag> getTags() { return tags; }

    public void setTags(Set<Tag> tags) { this.tags = tags; }
}
