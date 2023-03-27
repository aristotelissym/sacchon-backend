package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.PatientDataRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import java.util.List;

public interface PatientDataListResource {

    @Post("json")
    public PatientDataRepresentation add(PatientDataRepresentation patientDataReprIn) throws BadEntityException;

    @Get("json")
    public List<PatientDataRepresentation> getPatientsData() throws NotFoundException;
}
