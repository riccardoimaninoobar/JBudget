package it.unicam.cs.mpgc.jbudget123012.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * Rappresenta un tag o una categoria per le transazioni.
 * I tag possono essere organizzati in una gerarchia padre-figlio.
 */
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "normalized_name", nullable = false, unique = true)
    private String normalizedName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Tag parentTag;

    @OneToMany(mappedBy = "parentTag", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tag> subTags = new HashSet<>();

    /**
     * Costruttore richiesto da JPA.
     */
    public Tag() {
    }

    /**
     * Costruttore per creare un tag partendo dal nome.
     * @param name nome del tag
     */
    public Tag(String name) {
        this.setName(name);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    /**
     * Imposta il nome e aggiorna la versione normalizzata.
     * @param name nome da impostare
     */
    public void setName(String name) {
        this.name = name;
        this.normalizedName = (name != null) ? name.toLowerCase().trim() : null;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public Tag getParentTag() {
        return parentTag;
    }

    public void setParentTag(Tag parentTag) {
        this.parentTag = parentTag;
    }

    public Set<Tag> getSubTags() {
        return subTags;
    }

    public void setSubTags(Set<Tag> subTags) {
        this.subTags = subTags;
    }

    /**
     * Aggiunge un tag figlio mantenendo la coerenza della relazione bidirezionale.
     * @param child tag da aggiungere come figlio
     */
    public void addChild(Tag child) {
        this.subTags.add(child);
        child.setParentTag(this);
    }

    /**
     * Confronta due oggetti Tag basandosi su id e nome normalizzato.
     * @param o oggetto da confrontare
     * @return true se uguali, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(id, tag.id) && Objects.equals(normalizedName, tag.normalizedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, normalizedName);
    }

    /**
     * Ritorna il nome del tag, utile per liste e combobox.
     * @return nome del tag
     */
    @Override
    public String toString() {
        return this.name;
    }
}
