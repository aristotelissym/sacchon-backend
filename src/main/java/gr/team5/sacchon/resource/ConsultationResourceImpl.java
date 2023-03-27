package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.Consultation;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.repository.ConsultationRepository;
import gr.team5.sacchon.repository.PatientRepository;
import gr.team5.sacchon.repository.util.JpaUtil;
import gr.team5.sacchon.representation.ConsultationRepresentation;
import gr.team5.sacchon.representation.PatientRepresentation;
import gr.team5.sacchon.resource.util.ResourceValidator;
import gr.team5.sacchon.security.ResourceUtils;
import gr.team5.sacchon.security.Shield;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.logging.Logger;

public class ConsultationResourceImpl extends ServerResource implements ConsultationResource {

    public static final Logger LOGGER = Engine.getLogger(ConsultationResourceImpl.class);

    private long id;
    private long doctorId;
    private long patientId;
    private ConsultationRepository consultationRepository;
    private EntityManager entityManager;
    private PatientRepository patientRepository;

    /**
     * This release method closes the entityManager
     */
    @Override
    protected void doRelease() {
        entityManager.close();
    }

    /**
     * Initializes the consultation repository
     */
    @Override
    protected void doInit() {

        LOGGER.info("Initializing consultation resource starts");

        try {
            entityManager = JpaUtil.getEntityManager();
            consultationRepository = new ConsultationRepository(entityManager);
            patientRepository = new PatientRepository(entityManager);

            id = Long.parseLong(getAttribute("id"));
            doctorId = Long.parseLong(getAttribute("doctor_id"));
            patientId = Long.parseLong(getAttribute("patient_id"));

        } catch (Exception e) {
            throw  new ResourceException(e);
        }

        LOGGER.info("Initializing patient resource ends");
    }

    /**
     *
     * @return consultation by id
     * @throws NotFoundException
     */
    @Override
    public ConsultationRepresentation getConsultation() throws NotFoundException {

        LOGGER.info("Retrieve a consultation");

        // Initialize persistence layer
        ConsultationRepository consultationRepository = new ConsultationRepository(entityManager);
        Consultation consultation;

        PatientRepository patientRepository = new PatientRepository(entityManager);

        try {
            Optional<Consultation> oConsultation = consultationRepository.findById(id);
            setExisting(oConsultation.isPresent());

            if (!isExisting()) {
                LOGGER.config("consultation id does not exist: " + id);
                throw new NotFoundException("No consultation with id: " + id);
            } else {

                Optional<Patient> oPatient = patientRepository.findById(patientId);
                setExisting(oPatient.isPresent());

                if (!isExisting()) {
                    LOGGER.config("patient id does not exist: " + patientId);
                    throw new NotFoundException("No patient with id: " + patientId);
                }

                // Checking if doctor and patient are correct
                if ( oConsultation.get().getDoctor().getId() != doctorId){
                    LOGGER.config("This doctor has not access to this consultation: " + doctorId);
                    throw new NotFoundException("No consultation with id: " + doctorId);
                }
                if ( oConsultation.get().getPatient().getId() != patientId){
                    LOGGER.config("This patient has not access to this consultation: " + patientId);
                    throw new NotFoundException("No consultation with id: " + patientId);
                }

                consultation = oConsultation.get();
                ConsultationRepresentation result = new ConsultationRepresentation(consultation);

                LOGGER.finer("Consultation successfully retrieved.");

                // if patient read consultation, notification=false
                if(this.isInRole(Shield.ROLE_PATIENT)){
                    patientRepository.updateHasNotification(patientId, false);
                }

                return result;
            }
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }

    /**
     * Update
     * @param consultationReprIn
     * @return
     * @throws NotFoundException
     * @throws BadEntityException
     */
    @Override
    public ConsultationRepresentation store(ConsultationRepresentation consultationReprIn) throws NotFoundException, BadEntityException {

        LOGGER.finer("Update consultation.");

        // Checking authorization, if role is patient or chief, not allowed
        ResourceUtils.checkRole(this, Shield.ROLE_PATIENT);
        ResourceUtils.checkRole(this, Shield.ROLE_CHIEF);

        LOGGER.finer("User allowed to update consultation.");

        // Check given entity
        ResourceValidator.notNull(consultationReprIn);
        ResourceValidator.validate(consultationReprIn);
        LOGGER.finer("Consultation checked");

        try {
            Consultation consultationIn = consultationReprIn.createConsultation();
            consultationIn.setId(id);

            Optional<Consultation> consultationOut;
            Optional<Consultation> oConsultation = consultationRepository.findById(id);
            setExisting(oConsultation.isPresent());

            // if patient consultation exists, update him
            if (isExisting()) {

                LOGGER.finer("Update consultation.");

                // Checking if doctor and patient are correct
                if ( oConsultation.get().getDoctor().getId() != doctorId){
                    LOGGER.config("This doctor has not access to this consultation: " + doctorId);
                    throw new NotFoundException("No consultation with id: " + doctorId);
                }
                if ( oConsultation.get().getPatient().getId() != patientId){
                    LOGGER.config("This patient has not access to this consultation: " + patientId);
                    throw new NotFoundException("No consultation with id: " + patientId);
                }

                // Update patient consultation in DB and retrieve them
                consultationOut = consultationRepository.update(consultationIn);

                Optional <Patient> oPatient = patientRepository.findById(patientId);
                setExisting(oPatient.isPresent());

                // If consultation updated, notification -> true
                if(isExisting()){
                    patientRepository.updateHasNotification(patientId, true);
                }

                // Check if retrieved patient data is not null
                // if null it means the id is wrong.
                if (!consultationOut.isPresent()) {

                    LOGGER.fine("Consultation does not exist.");
                    throw new NotFoundException("Consultation with the following id does not exist: " + id);
                }
            } else {

                LOGGER.finer("Resource does not exist.");
                throw new NotFoundException("Consultation with the following id does not exist: " + id);
            }

            LOGGER.finer("Consultation successfully updated.");
            return new ConsultationRepresentation(consultationOut.get());
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }
}
