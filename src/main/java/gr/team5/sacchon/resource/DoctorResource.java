package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.DoctorRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

public interface DoctorResource {
    @Get("json")
    public DoctorRepresentation getDoctor() throws NotFoundException;

    @Delete
    public void remove() throws NotFoundException;

    @Put("json")
    public DoctorRepresentation store(DoctorRepresentation doctorRepresentation) throws NotFoundException, BadEntityException;
}
