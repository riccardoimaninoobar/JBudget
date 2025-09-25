package it.unicam.cs.mpgc.jbudget123012.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Classe singleton per gestire l'EntityManagerFactory di JPA.
 * Assicura che l'EntityManagerFactory, un oggetto pesante,
 * venga creato una sola volta per tutta la durata dell'applicazione.
 */
public final class PersistenceManager {

    // L'unica istanza della classe singleton
    private static PersistenceManager instance = null;

    // L'EntityManagerFactory responsabile della creazione degli EntityManager
    private final EntityManagerFactory entityManagerFactory;

    /**
     * Costruttore privato per impedire l'istanziazione diretta.
     * Inizializza l'EntityManagerFactory leggendo la configurazione
     * dal file persistence.xml.
     */
    private PersistenceManager() {
        // "jbudget-pu" deve corrispondere al nome della persistence-unit nel persistence.xml
        this.entityManagerFactory = Persistence.createEntityManagerFactory("jbudget-pu");
    }

    /**
     * Metodo statico per ottenere l'unica istanza della classe.
     * Se l'istanza non esiste la crea (lazy initialization).
     *
     * @return l'unica istanza di PersistenceManager
     */
    public static synchronized PersistenceManager getInstance() {
        if (instance == null) {
            instance = new PersistenceManager();
        }
        return instance;
    }

    /**
     * Fornisce un nuovo EntityManager per interagire con il database.
     * Ogni operazione sul database (salvataggio, ricerca, ecc.)
     * dovrebbe usare il proprio EntityManager.
     *
     * @return un nuovo EntityManager
     */
    public EntityManager getEntityManager() {
        return this.entityManagerFactory.createEntityManager();
    }

    /**
     * Chiude l'EntityManagerFactory quando l'applicazione termina,
     * rilasciando tutte le risorse del database.
     */
    public void close() {
        if (this.entityManagerFactory != null && this.entityManagerFactory.isOpen()) {
            this.entityManagerFactory.close();
        }
    }
}
