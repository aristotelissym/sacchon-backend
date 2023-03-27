package gr.team5.sacchon.representation.chief;

import gr.team5.sacchon.model.Consultation;
import gr.team5.sacchon.model.PatientData;
import gr.team5.sacchon.representation.ConsultationRepresentation;
import gr.team5.sacchon.representation.PatientDataRepresentation;
import lombok.Data;

@Data
public class ChiefInfoSubRepresentation {

    private PatientDataRepresentation patientData;
    private ConsultationRepresentation consultation;

    /**
     * Constructor
     * @param newData
     * will represent the resource
     */
    public ChiefInfoSubRepresentation(PatientData newData){
        patientData = new PatientDataRepresentation(newData);
        consultation = null;
    }

    /**
     * Constructor
     * @param newData
     * will represent the resource
     */
    public ChiefInfoSubRepresentation(Consultation newData){
        patientData = null;
        consultation = new ConsultationRepresentation(newData);
    }

}
