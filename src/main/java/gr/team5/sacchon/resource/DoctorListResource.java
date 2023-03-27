package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.DoctorRepresentation;
import gr.team5.sacchon.representation.PatientRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import java.util.List;

public interface DoctorListResource {

    @Post("json")
    public DoctorRepresentation add(DoctorRepresentation doctorReprIn) throws BadEntityException;

    @Get("json")
    public List<DoctorRepresentation> getDoctors() throws NotFoundException;
}
