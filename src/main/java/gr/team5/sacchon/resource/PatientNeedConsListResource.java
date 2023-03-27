package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.representation.PatientRepresentation;
import org.restlet.resource.Get;

import java.util.List;

public interface PatientNeedConsListResource {
    @Get("json")
    public List<PatientRepresentation> getPatientsWithNoCons() throws NotFoundException;
}
