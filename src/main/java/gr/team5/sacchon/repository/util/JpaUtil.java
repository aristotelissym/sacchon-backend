package gr.team5.sacchon.repository.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Jpa Util creates Entity Managers. We are using Application-Managed in this project,
 * so the lifecycle of each EntityManager is managed by the application here. Since we are
 * responsible for creating EntityManager instances, it is also our responsibility to close them.
 * Therefore, we should close each EntityManager when we are done using them.
 */
public class JpaUtil {
    private static final String PERSISTENCE_UNIT_NAME = "PERSISTENCE";
    private static EntityManagerFactory factory;

    public static EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return factory;
    }

    public static EntityManager getEntityManager() {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        return em;
    }


    public static void shutdown() {
        if (factory != null) {
            factory.close();
        }
    }
}