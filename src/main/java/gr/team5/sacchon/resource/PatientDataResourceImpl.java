package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.model.PatientData;
import gr.team5.sacchon.repository.PatientDataRepository;
import gr.team5.sacchon.repository.PatientRepository;
import gr.team5.sacchon.repository.util.JpaUtil;
import gr.team5.sacchon.representation.PatientDataRepresentation;
import gr.team5.sacchon.representation.PatientRepresentation;
import gr.team5.sacchon.resource.util.ResourceValidator;
import gr.team5.sacchon.security.ResourceUtils;
import gr.team5.sacchon.security.Shield;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientDataResourceImpl extends ServerResource implements PatientDataResource {

    public static final Logger LOGGER = Engine.getLogger(ConsultationResourceImpl.class);

    private long id;
    private long patientId;
    private PatientDataRepository patientDataRepository;
    private EntityManager entityManager;

    /**
     * This release method closes the entityManager
     */
    @Override
    protected void doRelease() {
        entityManager.close();
    }

    /**
     * Initializes the patient data repository
     */
    @Override
    protected void doInit() {

        LOGGER.info("Initializing patient data resource starts");

        try {
            entityManager = JpaUtil.getEntityManager();
            patientDataRepository = new PatientDataRepository(entityManager);
            id = Long.parseLong(getAttribute("id"));
            patientId = Long.parseLong(getAttribute("patient_id"));
        } catch (Exception e) {
            throw new ResourceException(e);
        }

        LOGGER.info("Initializing patient data resource ends");
    }

    /**
     *
     * @return patient data by id
     * @throws NotFoundException
     */
    @Override
    public PatientDataRepresentation getPatientData() throws NotFoundException {

        LOGGER.info("Retrieve patient data");

        // Initialize persistence layer
        PatientDataRepository patientDataRepository = new PatientDataRepository(entityManager);
        PatientData patientData;

        PatientRepository patientRepository = new PatientRepository(entityManager);

        try {
            Optional<PatientData> oPatientData = patientDataRepository.findById(id);
            setExisting(oPatientData.isPresent());

            if (!isExisting()) {
                LOGGER.config("patient data id does not exist: " + id);
                throw new NotFoundException("No patient data with id: " + id);
            }

            if(oPatientData.get().getPatient().getId() != patientId){
                LOGGER.config("This patient does not have access to data with id: " + id);
                throw new NotFoundException("This patient does not have access to data with id: " + id);
            }

            Optional<Patient> oPatient = patientRepository.findById(patientId);
            setExisting(oPatient.isPresent());

            if (!isExisting()) {
                LOGGER.config("patient does not exist: " + patientId);
                throw new NotFoundException("No patient data with id: " + patientId);
            }

            patientData = oPatientData.get();

            PatientDataRepresentation result = new PatientDataRepresentation(patientData);

            LOGGER.finer("Patient data successfully retrieved.");

            return result;
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }

    /**
     *
     * remove patient data
     * @throws NotFoundException
     */
    @Override
    public void remove() throws NotFoundException {

        LOGGER.finer("Removal of patient data.");

        // Checking authorization, if role is doctor or chief, not allowed
        ResourceUtils.checkRole(this, Shield.ROLE_DOCTOR);
        ResourceUtils.checkRole(this, Shield.ROLE_CHIEF);

        LOGGER.finer("User allowed to remove patient data.");

        try {
            Boolean isDeleted = patientDataRepository.delete(id);

            if (!isDeleted) {

                LOGGER.config("Patient data id does not exist");
                throw new NotFoundException("Patient data with following id does not exist: " + id);
            }

            LOGGER.finer("Patient data successfully removed.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error when removing patient data ", e);
            throw new ResourceException(e);
        }
    }

    /**
     *
     * @param patientDataReprIn
     * @return updates patient data
     * @throws NotFoundException
     * @throws BadEntityException
     */
    @Override
    public PatientDataRepresentation store(PatientDataRepresentation patientDataReprIn) throws NotFoundException, BadEntityException {

        LOGGER.finer("Update patient data.");

        // Checking authorization, if role is doctor or chief, not allowed
        ResourceUtils.checkRole(this, Shield.ROLE_DOCTOR);
        ResourceUtils.checkRole(this, Shield.ROLE_CHIEF);

        LOGGER.finer("User allowed to update patient data.");

        // Check given entity
        ResourceValidator.notNull(patientDataReprIn);
        ResourceValidator.validate(patientDataReprIn);
        LOGGER.finer("Patient data checked");

        try {
            PatientData patientDataIn = patientDataReprIn.createPatientData();
            patientDataIn.setId(id);

            Optional<PatientData> patientDataOut;
            Optional<PatientData> oPatientData = patientDataRepository.findById(id);
            setExisting(oPatientData.isPresent());

            // if patient data exists, update him
            if (isExisting()) {

                LOGGER.finer("Update patient data.");

                // update patient data in DB and retrieve them
                patientDataOut = patientDataRepository.update(patientDataIn);

                // Check if retrieved patient data is not null
                // if null it means the id is wrong.
                if (!patientDataOut.isPresent()) {

                    LOGGER.fine("Patient does not exist.");
                    throw new NotFoundException("Patient data with the following id does not exist: " + id);
                }
            } else {

                LOGGER.finer("Resource does not exist.");
                throw new NotFoundException("Patient data with the following id does not exist: " + id);
            }

            LOGGER.finer("Patient data successfully updated.");
            return new PatientDataRepresentation(patientDataOut.get());
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }
}
