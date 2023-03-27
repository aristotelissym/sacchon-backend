package gr.team5.sacchon.resource.chief;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.chief.ChiefPatientConsPendingRepresentation;
import org.restlet.resource.Get;

import java.util.List;

public interface ChiefPatientConsPending {
    @Get("json")
    public List<ChiefPatientConsPendingRepresentation> getPatientsConsPending() throws NotFoundException;
}
