package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.ConsultationRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import java.util.List;

public interface ConsultationListResource {

    @Post("json")
    public ConsultationRepresentation add(ConsultationRepresentation consultationReprIn) throws BadEntityException;

    @Get("json")
    public List<ConsultationRepresentation> getConsultations() throws NotFoundException;
}
