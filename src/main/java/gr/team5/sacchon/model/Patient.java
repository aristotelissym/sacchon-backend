package gr.team5.sacchon.model;

import lombok.Data;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String username;

    private String password;
    private boolean hasNotification;

    @OneToMany(mappedBy = "patient")
    private List<PatientData> patientData = new ArrayList<>();

    @OneToMany(mappedBy = "patient")
    private List<Consultation> consultations  = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}