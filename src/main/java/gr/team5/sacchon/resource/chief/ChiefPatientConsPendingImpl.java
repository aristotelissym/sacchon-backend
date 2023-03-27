package gr.team5.sacchon.resource.chief;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.Consultation;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.repository.ConsultationRepository;
import gr.team5.sacchon.repository.PatientRepository;
import gr.team5.sacchon.repository.util.JpaUtil;
import gr.team5.sacchon.representation.chief.ChiefPatientConsPendingRepresentation;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.logging.Logger;

public class ChiefPatientConsPendingImpl
        extends ServerResource
        implements ChiefPatientConsPending {

    public static final Logger LOGGER = Engine.getLogger(ChiefPatientConsPendingImpl.class);

    private PatientRepository patientRepository;
    private ConsultationRepository consultationRepository;

    private EntityManager entityManager;

    @Override
    protected void doRelease() {
        entityManager.close();
    }

    /**
     * Initializes chief patient needs consultation
     */
    @Override
    protected void doInit() {

        LOGGER.info("Initializes chief patient needs consultation");

        try {
            entityManager = JpaUtil.getEntityManager();
            patientRepository = new PatientRepository(entityManager);
            consultationRepository = new ConsultationRepository(entityManager);

        } catch (Exception e) {
            throw new ResourceException(e);

        }
        LOGGER.info("End initialization chief patient needs consultation");
    }

    /**
     * Represents:
     * The list of the patients who are waiting for a consultation and the time elapsed since they needed to have one
     * @return ChiefPatientConsPendingRepresentation
     * @throws
     */
    @Override
    public List<ChiefPatientConsPendingRepresentation> getPatientsConsPending() throws NotFoundException {
        LOGGER.info("Start getting patients that need consultation");

        List<Consultation> consultations = consultationRepository.findAll();
        List<Patient> patients = patientRepository.findAll();
        Calendar today = Calendar.getInstance();
        Calendar consDay = Calendar.getInstance();

        // remove patients with recent consultation, 1 month
        consultations.forEach(consultation -> {
            consDay.setTime(consultation.getDateCreated());
            consDay.add(Calendar.MONTH, +1);
            consDay.add(Calendar.DATE, -1);
            if(consDay.compareTo(today) >= 0){
                patients.remove(consultation.getPatient());
            }
        });

        HashMap<Patient, Long> patientHashMap = new HashMap<Patient, Long>();
        long timeDiff;

        for (Consultation c: consultations) {
            if(patients.contains(c.getPatient())){

                // replace date of a most recent consultation
                if (patientHashMap.containsKey(c.getPatient())){
                    consDay.setTime(c.getDateCreated());
                    consDay.add(Calendar.MONTH, +1);
                    consDay.add(Calendar.DATE, -1);

                    timeDiff = today.getTimeInMillis() - consDay.getTimeInMillis();

                    if(patientHashMap.get(c.getPatient()) > timeDiff){
                        patientHashMap.put(c.getPatient(), timeDiff);
                    }
                // insert elements (patient,time elapsed) in the hashmap
                } else {
                    consDay.setTime(c.getDateCreated());
                    consDay.add(Calendar.MONTH, +1);
                    consDay.add(Calendar.DATE, -1);

                    patientHashMap.put(c.getPatient(),
                            today.getTimeInMillis() - consDay.getTimeInMillis());
                }
            }
        }

        List<ChiefPatientConsPendingRepresentation> result = new ArrayList<>();
        patientHashMap.forEach((patient1, aLong) -> {
            result.add(new ChiefPatientConsPendingRepresentation(patient1, aLong));
        });

        LOGGER.info("End getting patients that need consultation");
        return result;
    }
}
