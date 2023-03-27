package gr.team5.sacchon.representation;

import gr.team5.sacchon.model.Doctor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DoctorRepresentation {

    private String username;
    private String password;
    private long doctorId;
    /**
     * The URL of this resource which is:
     * http://localhost:9000/doctor/{id}
     */
    private String uri;

    /**
     * Constructor
     * @param doctor
     * will represent the resource
     */
    public DoctorRepresentation(
            Doctor doctor) {
        if (doctor != null) {
            username = doctor.getUsername();
            doctorId = doctor.getId();
//            password = doctor.getPassword();
            uri = "http://localhost:9000/doctor/" + doctor.getId();
        }
    }

    /**
     *
     * @return an instance of doctor
     */
    public Doctor createDoctor() {
        Doctor doctor = new Doctor();
        doctor.setUsername(username);
        doctor.setPassword(password);

        return doctor;
    }
}
