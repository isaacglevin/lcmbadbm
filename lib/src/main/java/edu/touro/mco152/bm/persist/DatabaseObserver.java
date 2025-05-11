package edu.touro.mco152.bm.persist;

import edu.touro.mco152.bm.observer.BenchmarkObserver;
import jakarta.persistence.EntityManager;

/**
 * Observer that saves the benchmark result to the database.
 */
public class DatabaseObserver implements BenchmarkObserver {

    /**
     * Persists the given benchmark run to the database.
     *
     * @param run the completed DiskRun to save
     */
    @Override
    public void update(DiskRun run) {
        EntityManager em = EM.getEntityManager();
        em.getTransaction().begin();
        em.persist(run);
        em.getTransaction().commit();
    }
}

