package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.PatientRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

public interface PatientResource {
    @Get("json")
    public PatientRepresentation getPatient() throws NotFoundException;

    @Delete
    public void remove() throws NotFoundException;

    @Put("json")
    public PatientRepresentation store(PatientRepresentation patientRepresentation)
            throws NotFoundException, BadEntityException;
}
