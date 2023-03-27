package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.representation.ConsultationRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

public interface ConsultationResource {
    @Get("json")
    public ConsultationRepresentation getConsultation() throws NotFoundException;

    @Put
    public ConsultationRepresentation store(ConsultationRepresentation consultationRepresentation) throws NotFoundException, BadEntityException;
}
