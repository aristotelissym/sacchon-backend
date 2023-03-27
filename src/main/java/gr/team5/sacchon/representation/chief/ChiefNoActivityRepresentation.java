package gr.team5.sacchon.representation.chief;

import gr.team5.sacchon.model.Doctor;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.representation.DoctorRepresentation;
import gr.team5.sacchon.representation.PatientRepresentation;
import lombok.Data;

@Data
public class ChiefNoActivityRepresentation {

    private DoctorRepresentation doctor;
    private PatientRepresentation patient;

    /**
     * Constructor
     * @param doctorIn
     * will represent the resource
     */
    public ChiefNoActivityRepresentation(Doctor doctorIn){
        doctor = new DoctorRepresentation(doctorIn);
        patient = null;
    }

    /**
     * Constructor
     * @param patientIn
     * will represent the resource
     */
    public ChiefNoActivityRepresentation(Patient patientIn){
        doctor = null;
        patient = new PatientRepresentation(patientIn);
    }
}
