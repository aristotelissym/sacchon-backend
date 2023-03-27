package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.repository.PatientRepository;
import gr.team5.sacchon.repository.util.JpaUtil;
import gr.team5.sacchon.representation.PatientRepresentation;
import gr.team5.sacchon.resource.util.ResourceValidator;
import gr.team5.sacchon.security.ResourceUtils;
import gr.team5.sacchon.security.Shield;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientListResourceImpl extends ServerResource implements PatientListResource {

    public static  final Logger LOGGER = Engine.getLogger(PatientResourceImpl.class);

    private PatientRepository patientRepository;
    private EntityManager entityManager;
    private Long id;

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
            if ( getAttribute("id") != null) {
                id = Long.parseLong(getAttribute("id"));
            }
        } catch (Exception e) {
            throw new ResourceException(e);
        }
        LOGGER.info("Initializing patient list resource ends");
    }

    /**
     *
     * @param patientReprIn representation of a Patient given by the frontEnd
     * @return a representation of the persisted object
     * @throws BadEntityException
     */
    @Override
    public PatientRepresentation add(PatientRepresentation patientReprIn) throws BadEntityException {

        LOGGER.finer("Add new patient.");

        // Checking authorization, if role is chief or doctor, not allowed
        ResourceUtils.checkRole(this, Shield.ROLE_CHIEF);
        ResourceUtils.checkRole(this, Shield.ROLE_DOCTOR);

        LOGGER.finer("User allowed to add a patient.");

        // Check entity
        ResourceValidator.notNull(patientReprIn);
        ResourceValidator.validate(patientReprIn);

        LOGGER.finer("patient checked");

        try {

            // Convert PatientRepresentation to Patient
            Patient patientIn = new Patient();
            patientIn.setUsername(patientReprIn.getUsername());
            patientIn.setPassword(patientReprIn.getPassword());
            patientIn.setHasNotification(patientReprIn.isHasNotification());

            Optional<Patient> patientOptOut = patientRepository.save(patientIn);

            Patient patient = null;
            if (patientOptOut.isPresent())
                patient = patientOptOut.get();
            else
                throw new BadEntityException("Patient has not been created");

            PatientRepresentation result = new PatientRepresentation(patient);

            result.setUsername(patient.getUsername());
            result.setPassword(patient.getPassword());
            result.setHasNotification(patient.isHasNotification());
            result.setUri("http://localhost:9000/patient/" + patient.getId());

            getResponse().setLocationRef(
                    "http://localhost:9000/patient/" + patient.getId());
            getResponse().setStatus(Status.SUCCESS_CREATED);

            LOGGER.finer("Patient successfully added.");

            return result;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error when adding a patient", ex);
            throw new ResourceException(ex);
        }
    }

    /**
     *
     * @return list of all patients
     * @throws NotFoundException
     */
    @Override
    public List<PatientRepresentation> getPatients() throws NotFoundException {

        LOGGER.finer("Select all patients.");

        // Checking authorization,if role is patient not allowed
        ResourceUtils.checkRole(this, Shield.ROLE_PATIENT);

        try {
            List<Patient> patients;

            if(id == null){
                patients = patientRepository.findAll();
            }
            else{
                patients = patientRepository.findPatientWithDoctorId(id);
            }

            List<PatientRepresentation> result = new ArrayList<>();
            patients.forEach(patient -> result.add(new PatientRepresentation(patient)));

            return result;
        } catch (Exception e) {
            throw new NotFoundException("patients not found");
        }
    }
}
