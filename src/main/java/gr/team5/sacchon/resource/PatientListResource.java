package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.PatientRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import java.util.List;

public interface PatientListResource {
    @Post("json")
    public PatientRepresentation add(PatientRepresentation patientReprIn) throws BadEntityException;

    @Get("json")
    public List<PatientRepresentation> getPatients() throws NotFoundException;
}
