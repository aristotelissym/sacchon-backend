package gr.team5.sacchon.security;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Checks if someone should have access to the resource
 * @author One-To-Fix-Them-All
 */
public class ResourceUtils {
    /**
     * Checks if the resource is available of the role
     * @param serverResource, the resource that a user ask for
     * @param role, the role of the user
     * @throws ResourceException when the user ask for a resource that should not have access
     */
    public static void checkRole (ServerResource serverResource, String role)
            throws ResourceException {
        if(serverResource.isInRole(role)){
            throw new ResourceException(
                    Status.CLIENT_ERROR_FORBIDDEN,
                    "You are not authorized for that resource"
            );
        }
    }
}
