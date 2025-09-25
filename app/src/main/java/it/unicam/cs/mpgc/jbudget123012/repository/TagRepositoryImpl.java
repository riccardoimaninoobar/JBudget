package it.unicam.cs.mpgc.jbudget123012.repository;

import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Implementazione concreta per il repository dei tag.
 * Eredita la logica di base e implementa i metodi specifici dell'interfaccia.
 */
public class TagRepositoryImpl extends BaseRepositoryImpl<Tag, Long> implements TagRepository {

    /**
     * Costruttore che riceve la factory e la passa alla classe base.
     * @param emf factory per la creazione di entity manager
     */
    public TagRepositoryImpl(EntityManagerFactory emf) {
        super(Tag.class, emf);
    }

    /**
     * Sovrascrive findAll per aggiungere un ordinamento e caricare i sottotag.
     * Usa join fetch per evitare problemi di lazy loading.
     * @return lista di tag con sottotag caricati, ordinata per nome
     */
    @Override
    public List<Tag> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT t FROM Tag t LEFT JOIN FETCH t.subTags ORDER BY t.name", Tag.class).getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Implementa il metodo per trovare un tag dal suo nome esatto.
     * @param name nome del tag da cercare
     * @return Optional contenente il tag se trovato, altrimenti vuoto
     */
    @Override
    public Optional<Tag> findByName(String name) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Tag> query = em.createQuery("SELECT t FROM Tag t WHERE t.name = :name", Tag.class);
            query.setParameter("name", name);
            return query.getResultStream().findFirst();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Implementa il metodo per trovare un tag dal suo nome normalizzato.
     * @param normalizedName nome normalizzato del tag da cercare
     * @return Optional contenente il tag se trovato, altrimenti vuoto
     */
    @Override
    public Optional<Tag> findByNormalizedName(String normalizedName) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Tag> query = em.createQuery("SELECT t FROM Tag t WHERE t.normalizedName = :normalizedName", Tag.class);
            query.setParameter("normalizedName", normalizedName);
            return query.getResultStream().findFirst();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Implementa il metodo per trovare tutti i tag caricando subito i loro genitori.
     * @return lista di tag con i genitori già caricati
     */
    @Override
    public List<Tag> findAllWithParents() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Tag> query = em.createQuery("SELECT t FROM Tag t LEFT JOIN FETCH t.parentTag", Tag.class);
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
