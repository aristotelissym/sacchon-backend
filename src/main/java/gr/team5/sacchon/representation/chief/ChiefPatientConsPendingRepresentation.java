package gr.team5.sacchon.representation.chief;

import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.representation.PatientRepresentation;
import lombok.Data;

@Data
public class ChiefPatientConsPendingRepresentation {
    private PatientRepresentation patient;
    private int elapsedTime;

    /**
     * Constructor
     * @param inPatient
     * @param time
     * will represent the resource
     */
    public ChiefPatientConsPendingRepresentation(
            Patient inPatient, Long time){
        patient = new PatientRepresentation(inPatient);
        elapsedTime = (int) (time / (1000*60*60*24));
    }
}
