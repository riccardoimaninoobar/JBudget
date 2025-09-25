package it.unicam.cs.mpgc.jbudget123012.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entità che rappresenta una scadenza da pagare.
 */
@Entity
public class Deadline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeadlineStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "deadline_tags",
            joinColumns = @JoinColumn(name = "deadline_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    /**
     * Costruttore vuoto richiesto da JPA.
     * Imposta lo stato a PENDING.
     */
    public Deadline() {
        this.status = DeadlineStatus.PENDING;
    }

    /**
     * Costruttore per creare una nuova scadenza.
     * @param description descrizione della scadenza
     * @param amount importo da pagare
     * @param dueDate data di scadenza
     * @param tags insieme di tag associati
     */
    public Deadline(String description, BigDecimal amount, LocalDate dueDate, Set<Tag> tags) {
        this.description = description;
        this.amount = amount;
        this.dueDate = dueDate;
        this.tags = tags;
        this.status = DeadlineStatus.PENDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public DeadlineStatus getStatus() { return status; }
    public void setStatus(DeadlineStatus status) { this.status = status; }
    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }

    /**
     * Aggiunge un tag all'insieme dei tag associati.
     * @param tag tag da aggiungere
     */
    public void addTag(Tag tag) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }
        this.tags.add(tag);
    }

    /**
     * Rimuove un tag dall'insieme dei tag associati.
     * @param tag tag da rimuovere
     */
    public void removeTag(Tag tag) {
        if (this.tags != null) {
            this.tags.remove(tag);
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%.2f €) - Scadenza: %s - Stato: %s",
                description, amount, dueDate, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deadline deadline = (Deadline) o;
        return id != null && id.equals(deadline.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
