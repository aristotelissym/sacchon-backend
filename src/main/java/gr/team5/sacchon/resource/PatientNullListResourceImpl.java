package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.repository.PatientRepository;
import gr.team5.sacchon.repository.util.JpaUtil;
import gr.team5.sacchon.representation.PatientRepresentation;
import gr.team5.sacchon.security.ResourceUtils;
import gr.team5.sacchon.security.Shield;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PatientNullListResourceImpl extends ServerResource implements PatientNullListResource {

    public static  final Logger LOGGER = Engine.getLogger(PatientNullListResourceImpl.class);

    private PatientRepository patientRepository;
    private EntityManager entityManager;

    /**
     * This release method closes the entityManager
     */
    @Override
    protected void doRelease() {
        entityManager.close();
    }

    /**
     * Initializes the patient repository
     */
    protected void doInit() {

        LOGGER.info("Initializing patient list resource starts");

        try {
            entityManager = JpaUtil.getEntityManager();
            patientRepository = new PatientRepository(entityManager);
        } catch (Exception e) {
            throw new ResourceException(e);
        }

        LOGGER.info("Initializing patient list resource ends");
    }

    /**
     *
     * @return patients with doctor id null
     * @throws NotFoundException
     */
    @Override
    public List<PatientRepresentation> getPatientsWithNullDoctorID() throws NotFoundException {

        LOGGER.finer("Select all patients that their doctor id is null.");

        // Check authorization, if role is patient, not allowed
        ResourceUtils.checkRole(this, Shield.ROLE_PATIENT);

        try {

            List<Patient> patients  = patientRepository.findPatientWithDoctorIdNull();
            List<PatientRepresentation> result = new ArrayList<>();
            patients.forEach(patient -> result.add(new PatientRepresentation(patient)));

            return result;
        } catch (Exception e) {
            throw new NotFoundException("patient list not found");
        }
    }
}
