package it.unicam.cs.mpgc.jbudget123012.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione generica di un repository base per operazioni CRUD.
 *
 * @param <T>  il tipo dell'entità
 * @param <ID> il tipo dell'identificativo dell'entità
 */
public abstract class BaseRepositoryImpl<T, ID> implements BaseRepository<T, ID> {

    protected final Class<T> entityClass;
    protected final EntityManagerFactory emf;

    /**
     * Crea un repository con la classe dell'entità e l'EntityManagerFactory.
     *
     * @param entityClass classe dell'entità
     * @param emf         factory per ottenere gli EntityManager
     */
    public BaseRepositoryImpl(Class<T> entityClass, EntityManagerFactory emf) {
        this.entityClass = entityClass;
        this.emf = emf;
    }

    /**
     * Fornisce un nuovo EntityManager per ogni operazione.
     *
     * @return un nuovo EntityManager
     */
    protected EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Salva o aggiorna un'entità nel database.
     *
     * @param entity entità da salvare
     * @return entità salvata
     */
    @Override
    public T save(T entity) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T mergedEntity = em.merge(entity);
            em.getTransaction().commit();
            return mergedEntity;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("impossibile salvare l'entità", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Cerca un'entità tramite il suo identificativo.
     *
     * @param id identificativo dell'entità
     * @return Optional contenente l'entità, se presente
     */
    @Override
    public Optional<T> findById(ID id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(entityClass, id));
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Recupera tutte le entità del tipo specificato.
     *
     * @return lista di entità
     */
    @Override
    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("FROM " + entityClass.getSimpleName(), entityClass).getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Cancella un'entità dato il suo identificativo.
     *
     * @param id identificativo dell'entità da cancellare
     */
    @Override
    public void deleteById(ID id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("impossibile cancellare l'entità con id " + id, e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Cancella una specifica entità.
     *
     * @param entity entità da cancellare
     */
    @Override
    public void delete(T entity) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T entityToDelete = entity;
            if (!em.contains(entityToDelete)) {
                entityToDelete = em.merge(entityToDelete);
            }
            em.remove(entityToDelete);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("impossibile cancellare l'entità", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
