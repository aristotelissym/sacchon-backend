package gr.team5.sacchon.security.dao;

import gr.team5.sacchon.model.DatabaseUser;
import gr.team5.sacchon.repository.util.JpaUtil;
import org.restlet.Context;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.Optional;

/**
 * A class to configure the database
 * And to set up the user
 * @author One-To-Fix-Them-All
 */
public class UserPersistence extends ServerResource {
    private static UserPersistence userPersistence = new UserPersistence(JpaUtil.getEntityManager());
    private EntityManager entityManager;

    private UserPersistence(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    protected void doRelease() {
        entityManager.close();
    }

    /**
     * Finds a user by his username
     * @param username the user name of the user to find
     * @return the user
     */
    public Optional<DatabaseUser> findByUsername(String username) {
        Context.getCurrentLogger().finer(
                "Method findById of UserPersistence started"
        );

        DatabaseUser user = entityManager.find(DatabaseUser.class, username);
        return user != null ? Optional.of(user) : Optional.empty();
    }

    /** synchronized, is for multithreading only one thread has access to it **/
    public static synchronized UserPersistence getUserPersistence(){
        return userPersistence;
    }
}