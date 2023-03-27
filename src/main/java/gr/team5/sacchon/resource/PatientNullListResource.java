package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.PatientRepresentation;
import org.restlet.resource.Get;

import java.util.List;

public interface PatientNullListResource {
    @Get("json")
    public List<PatientRepresentation> getPatientsWithNullDoctorID() throws NotFoundException;
}
