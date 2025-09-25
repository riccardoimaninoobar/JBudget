package it.unicam.cs.mpgc.jbudget123012.repository;

import it.unicam.cs.mpgc.jbudget123012.model.InstallmentPlan;
import jakarta.persistence.EntityManagerFactory;

/**
 * Implementazione concreta del repository per i piani di ammortamento.
 * Eredita tutta la logica di base e implementa l'interfaccia specifica.
 */
public class InstallmentPlanRepositoryImpl extends BaseRepositoryImpl<InstallmentPlan, Long> implements InstallmentPlanRepository {

    /**
     * Costruttore che accetta la EntityManagerFactory
     * e la passa alla classe base per completare la configurazione.
     *
     * @param emf EntityManagerFactory per la gestione delle entità.
     */
    public InstallmentPlanRepositoryImpl(EntityManagerFactory emf) {
        super(InstallmentPlan.class, emf);
    }

}
