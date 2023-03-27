package gr.team5.sacchon.resource.chief;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.Consultation;
import gr.team5.sacchon.model.Doctor;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.model.PatientData;
import gr.team5.sacchon.repository.ConsultationRepository;
import gr.team5.sacchon.repository.DoctorRepository;
import gr.team5.sacchon.repository.PatientDataRepository;
import gr.team5.sacchon.repository.PatientRepository;
import gr.team5.sacchon.repository.util.JpaUtil;
import gr.team5.sacchon.representation.chief.ChiefNoActivityRepresentation;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.logging.Logger;

public class ChiefNoActivityImpl extends ServerResource implements ChiefNoActivity {

    public static final Logger LOGGER = Engine.getLogger(ChiefNoActivityImpl.class);

    private String who;
    private Date from;
    private Date to;

    private PatientRepository patientRepository;
    private PatientDataRepository patientDataRepository;

    private DoctorRepository doctorRepository;
    private ConsultationRepository consultationRepository;

    private EntityManager entityManager;

    @Override
    protected void doRelease() {
        entityManager.close();
    }

    /**
     * Initializes chief no activity users
     */
    @Override
    protected void doInit() {

        LOGGER.info("Initializes chief no activity users");

        try {
            entityManager = JpaUtil.getEntityManager();

            try {
                String startDateString = getQueryValue("from");
                String   endDateString = getQueryValue("to");
                String[] words = startDateString.split("-");

                from = new Date(Integer.parseInt(words[0])-1900,
                        Integer.parseInt(words[1]) - 1, Integer.parseInt(words[2])  );

                words = endDateString.split("-");
                to = new Date(Integer.parseInt(words[0])-1900,
                        Integer.parseInt(words[1]) - 1, Integer.parseInt(words[2]) + 1 );

            } catch(Exception e) {
                from = null;
                to = null;
            }

            try {
                who = getQueryValue("who");
                if(who.equals("patient")){
                    patientRepository = new PatientRepository(entityManager);
                    patientDataRepository = new PatientDataRepository(entityManager);
                } else if (who.equals("doctor")){
                    doctorRepository = new DoctorRepository(entityManager);
                    consultationRepository = new ConsultationRepository(entityManager);
                }
            } catch (Exception e) {
                throw new NotFoundException("You need to set who as a doctor or patient");
            }

        } catch (Exception e) {
            throw new ResourceException(e);
        }
        LOGGER.info("Initializing chief not activity ends");
    }

    /**
     * Represents:
     * 1) The list of the patients with no activity over a time range
     * 2) The list of the doctors with no activity over a time range
     * @return ChiefNoActivityRepresentation
     * @throws NotFoundException
     */
    @Override
    public List<ChiefNoActivityRepresentation> getNoActivity() throws NotFoundException {
        LOGGER.info("Start process get no activity");

        if (from == null || to == null){
            throw new NotFoundException("You need to provide from and to");
        }

        Set<ChiefNoActivityRepresentation> result = new HashSet<>();

        if(who.equals("patient")){

            List<Patient> patients = patientRepository.findAll();
            Set<Patient> patientSet = new HashSet<>(patients);

            patients.forEach(patient -> {

                List<PatientData> patientData =
                        patientDataRepository.findDataByPatientId(patient.getId());

                patientData.forEach(patientData1 -> {
                    // if patient has at least one data storage remove him
                    if (((patientData1.getDate().compareTo(from) >= 0) &&
                            (patientData1.getDate().compareTo(to) < 0))) {
                        patientSet.remove(patient);
                    }
                });

            });

            patientSet.forEach(patient -> {
                result.add(new ChiefNoActivityRepresentation(patient));
            });

        } else if (who.equals("doctor")){

            List<Doctor> doctors = doctorRepository.findAll();
            Set<Doctor> doctorsSet = new HashSet<>(doctors);

            doctors.forEach(doctor -> {
                List<Consultation> consultations =
                        consultationRepository.findConsultationByDoctorId(doctor.getId());

                consultations.forEach(consultation -> {
                    // if doctor has at least one consultation remove him
                    if (((consultation.getDateCreated().compareTo(from) >= 0) &&
                           (consultation.getDateCreated().compareTo(to) < 0))) {
                        doctorsSet.remove(doctor);
                    }
                });

            });

            doctorsSet.forEach(doctor -> {
                result.add(new ChiefNoActivityRepresentation(doctor));
            });
        }

        List<ChiefNoActivityRepresentation> resultList = new ArrayList<>(result);

        return resultList;
    }
}
