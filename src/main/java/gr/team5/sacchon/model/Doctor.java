package gr.team5.sacchon.model;

import lombok.Data;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @OneToMany(mappedBy = "doctor")
    private List<Patient> patients = new ArrayList<>();

    @OneToMany(mappedBy = "doctor")
    private List<Consultation> consultations = new ArrayList<>();
}