package gr.team5.sacchon.resource.chief;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.chief.ChiefNoActivityRepresentation;
import org.restlet.resource.Get;

import java.util.List;

public interface ChiefNoActivity {
    @Get("json")
    public List<ChiefNoActivityRepresentation> getNoActivity () throws NotFoundException;
}
