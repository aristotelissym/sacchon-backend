package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.PatientDataRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

public interface PatientDataResource {
    @Get("json")
    public PatientDataRepresentation getPatientData() throws NotFoundException;

    @Delete
    public void remove() throws NotFoundException;

    @Put("json")
    public PatientDataRepresentation store(PatientDataRepresentation patientDataRepresentation)
        throws NotFoundException, BadEntityException;
}
