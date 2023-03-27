package gr.team5.sacchon.model;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class PatientData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private Double bloodGlucose;
    private Double carbIntake;
    private Date date;
}