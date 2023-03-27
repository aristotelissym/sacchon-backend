package gr.team5.sacchon.resource.chief;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.chief.ChiefInfoSubRepresentation;
import org.restlet.resource.Get;

import java.util.List;

public interface ChiefInfoSub {
    @Get("json")
    public List<ChiefInfoSubRepresentation> getInfoSubmit() throws NotFoundException;
}
