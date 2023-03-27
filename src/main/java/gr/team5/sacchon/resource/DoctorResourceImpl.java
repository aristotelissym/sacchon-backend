package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.Consultation;
import gr.team5.sacchon.model.Doctor;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.repository.ConsultationRepository;
import gr.team5.sacchon.repository.DoctorRepository;
import gr.team5.sacchon.repository.PatientRepository;
import gr.team5.sacchon.repository.util.JpaUtil;
import gr.team5.sacchon.representation.DoctorRepresentation;
import gr.team5.sacchon.resource.util.ResourceValidator;
import gr.team5.sacchon.security.ResourceUtils;
import gr.team5.sacchon.security.Shield;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoctorResourceImpl extends ServerResource implements DoctorResource {

    public static final Logger LOGGER = Engine.getLogger(DoctorResourceImpl.class);

    private long id;
    private DoctorRepository doctorRepository;
    private EntityManager entityManager;
    private PatientRepository patientRepository;
    private ConsultationRepository consultationRepository;

    /**
     * This release method closes the entityManager
     */
    @Override
    protected void doRelease() {
        entityManager.close();
    }

    /**
     * Initializes the doctor repository
     */
    @Override
    protected void doInit() {

        LOGGER.info("Initializing doctor resource starts");

        try {
            entityManager = JpaUtil.getEntityManager();
            doctorRepository = new DoctorRepository(entityManager);
            patientRepository = new PatientRepository(entityManager);
            consultationRepository = new ConsultationRepository(entityManager);
            id = Long.parseLong(getAttribute("id"));
        } catch (Exception e) {
            throw new ResourceException(e);
        }

        LOGGER.info("Initializing doctor resource ends");
    }

    /**
     *
     * @return doctor by id
     * @throws NotFoundException
     */
    @Override
    public DoctorRepresentation getDoctor() throws NotFoundException {

        LOGGER.info("Retrieve a doctor");

        // Checking authorization
        ResourceUtils.checkRole(this, Shield.ROLE_PATIENT);

        // Initialize persistence layer
        DoctorRepository doctorRepository = new DoctorRepository(entityManager);
        Doctor doctor;

        try {
            Optional<Doctor> oDoctor = doctorRepository.findById(id);

            setExisting(oDoctor.isPresent());
            if (!isExisting()) {

                LOGGER.config("doctor id does not exist: " + id);
                throw new NotFoundException("No doctor with id: " + id);
            } else {

                doctor = oDoctor.get();
                LOGGER.finer("User allowed to retrieve a doctor.");
                DoctorRepresentation result = new DoctorRepresentation(doctor);

                LOGGER.finer("Doctor successfully retrieved.");

                return result;
            }
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }

    /**
     *
     * remove a doctor
     * @throws NotFoundException
     */
    @Override
    public void remove() throws NotFoundException {

        LOGGER.finer("Removal of doctor.");

        // Checking authorization, if role is chief or patient, not allowed
        ResourceUtils.checkRole(this, Shield.ROLE_PATIENT);
        ResourceUtils.checkRole(this, Shield.ROLE_CHIEF);

        LOGGER.finer("User allowed to remove a doctor.");

        try {
            // Remove doctor from foreign key of patients
            List<Patient> listOfPatient = patientRepository.findPatientWithDoctorId(id);
            listOfPatient.forEach(patient -> {
                patient.setDoctor(null);
                patientRepository.update(patient);
            });

            // Replace doctor from foreign key of consultations
            List<Consultation> listOfConsultation = consultationRepository.findConsultationByDoctorId(id);
            Optional <Doctor> placeHolderDoc = doctorRepository.findById(Long.parseLong("8"));
            listOfConsultation.forEach(patientCons -> {
                patientCons.setDoctor(placeHolderDoc.get());
                consultationRepository.update(patientCons);
            });

            Boolean isDeleted = doctorRepository.delete(id);

            if (!isDeleted) {
                LOGGER.config("Doctor id does not exist");
                throw new NotFoundException("Doctor with following id does not exist: " + id);
            }


            LOGGER.finer("Doctor successfully removed.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error when removing a doctor ", e);
            throw new ResourceException(e);
        }
    }

    /**
     *
     * @param doctorReprIn
     * @return updates a doctor
     * @throws NotFoundException
     * @throws BadEntityException
     */
    @Override
    public DoctorRepresentation store(DoctorRepresentation doctorReprIn) throws NotFoundException, BadEntityException {

        LOGGER.finer("Update a doctor.");

        ResourceUtils.checkRole(this, Shield.ROLE_PATIENT);
        ResourceUtils.checkRole(this, Shield.ROLE_CHIEF);
        LOGGER.finer("User allowed to update a doctor.");

        // Check given entity
        ResourceValidator.notNull(doctorReprIn);
        ResourceValidator.validate(doctorReprIn);
        LOGGER.finer("Doctor checked");

        try {
            Doctor doctorIn = doctorReprIn.createDoctor();
            doctorIn.setId(id);

            Optional<Doctor> doctorOut;
            Optional<Doctor> oDoctor = doctorRepository.findById(id);
            setExisting(oDoctor.isPresent());

            // if doctor exists, update him
            if (isExisting()) {

                LOGGER.finer("Update doctor.");

                // update doctor in DB and retrieve the new
                doctorOut = doctorRepository.update(doctorIn);

                // Check if retrieved doctor is not null
                // if null it means the id is wrong.
                if (!doctorOut.isPresent()) {

                    LOGGER.fine("Doctor does not exist.");
                    throw new NotFoundException("Doctor with the following id does not exist: " + id);
                }
            } else {

                LOGGER.finer("Resource does not exist.");
                throw new NotFoundException("Doctor with the following id does not exist: " + id);
            }

            LOGGER.finer("Doctor successfully updated.");
            return new DoctorRepresentation(doctorOut.get());
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }
}
