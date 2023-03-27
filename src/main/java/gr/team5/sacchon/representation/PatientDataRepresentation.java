package gr.team5.sacchon.representation;

import gr.team5.sacchon.model.PatientData;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@NoArgsConstructor
public class PatientDataRepresentation {

    private Double bloodGlucose;
    private Double carbIntake;
    private Date date;
    private long patientId;
    private long dataId;

    /**
     * The URL of this resource which is:
     * http://localhost:9000/patient/{id}/data
     */
    private String uri;

    /**
     * Constructor
     * @param patientData
     * will represent the resource
     */
    public PatientDataRepresentation(
            PatientData patientData) {
        if (patientData != null) {
            bloodGlucose = patientData.getBloodGlucose();
            carbIntake = patientData.getCarbIntake();
            date = patientData.getDate();
            patientId = patientData.getPatient().getId();
            dataId = patientData.getId();
            uri = "http://localhost:9000/patient/" + patientId + "/data/" + patientData.getId();
        }
    }

    /**
     *
     * @param bloodAvg uses to get the bloodglucose average
     * @param carbAvg uses to get the carbintake average
     */
    public PatientDataRepresentation(
            double bloodAvg, double carbAvg){
        bloodGlucose = bloodAvg;
        carbIntake = carbAvg;
    }

    /**
     *
     * @return an instance of a patientData
     */
    public PatientData createPatientData() {
        PatientData patientData = new PatientData();
        patientData.setBloodGlucose(bloodGlucose);
        patientData.setCarbIntake(carbIntake);
        patientData.setDate(date);

        return patientData;
    }
}
