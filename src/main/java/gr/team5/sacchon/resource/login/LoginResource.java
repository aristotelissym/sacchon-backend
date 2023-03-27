package gr.team5.sacchon.resource.login;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.login.DatabaseUserRepresentation;
import org.restlet.resource.Get;

public interface LoginResource {

    @Get("json")
    public DatabaseUserRepresentation getUser() throws NotFoundException;
}
