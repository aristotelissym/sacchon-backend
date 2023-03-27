package gr.team5.sacchon.security;

import gr.team5.sacchon.model.DatabaseUser;
import gr.team5.sacchon.security.dao.UserPersistence;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.security.Role;
import org.restlet.security.SecretVerifier;

import java.util.Optional;

/**
 * verify the user credentials
 * @author One-To-Fix-Them-All
 */
public class CustomVerifier extends SecretVerifier {
    /**
     * checks if a password is correct
     * @param identifier how to find the user
     * @param secret the password that someone have use
     * @return enum valid or invalid
     */
    @Override
    public int verify (String identifier, char[] secret) {
        UserPersistence userPersistence =
                UserPersistence.getUserPersistence();

        Optional<DatabaseUser> user =  userPersistence.findByUsername(identifier);

        if (user.isPresent()){
            if (!compare(user.get().getPassword().toCharArray(), secret)){
                return SecretVerifier.RESULT_INVALID;
            }
            Request request = Request.getCurrent();

            request.getClientInfo().getRoles().add(
                    new Role(Application.getCurrent(),
                    user.get().getRole().getRoleName(),
                    null));

            return SecretVerifier.RESULT_VALID;
        } else {
            return SecretVerifier.RESULT_INVALID;
        }
    }
}