package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
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
import gr.team5.sacchon.representation.PatientRepresentation;
import gr.team5.sacchon.resource.util.ResourceValidator;
import gr.team5.sacchon.security.ResourceUtils;
import gr.team5.sacchon.security.Shield;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientResourceImpl extends ServerResource implements PatientResource {

    public static final Logger LOGGER = Engine.getLogger(PatientResourceImpl.class);

    private long id;
    private Long doctorId;
    private PatientRepository patientRepository;
    private PatientDataRepository patientDataRepository;
    private DoctorRepository doctorRepository;
    private ConsultationRepository consultationRepository;
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
    @Override
    protected void doInit() {

        LOGGER.info("Initializing patient resource starts");

        try {
            entityManager = JpaUtil.getEntityManager();
            patientRepository = new PatientRepository(entityManager);
            patientDataRepository = new PatientDataRepository(entityManager);
            consultationRepository = new ConsultationRepository(entityManager);
            id = Long.parseLong(getAttribute("id"));
            try{
                doctorRepository = new DoctorRepository(entityManager);
                doctorId = Long.parseLong(getAttribute("doctor_id"));
            } catch (Exception e) {
                doctorId = null;
            }
        } catch (Exception e) {
            throw new ResourceException(e);
        }
        LOGGER.info("Initializing patient resource ends");
    }

    /**
     *
     * @return patient by id
     * @throws NotFoundException
     */
    @Override
    public PatientRepresentation getPatient() throws NotFoundException {

        LOGGER.info("Retrieve a patient");

        // Initialize persistence layer
        PatientRepository patientRepository = new PatientRepository(entityManager);
        Patient patient;

        try {
            Optional<Patient> oPatient = patientRepository.findById(id);
            setExisting(oPatient.isPresent());

            if (!isExisting()) {
                LOGGER.config("patient id does not exist: " + id);
                throw new NotFoundException("No patient with id: " + id);
            } else {

                if (doctorId != null){

                    Optional<Doctor> oDoctor = doctorRepository.findById(doctorId);
                    setExisting(oDoctor.isPresent());

                    if (!isExisting()) {

                        LOGGER.config("doctor id does not exist: " + doctorId);
                        throw new NotFoundException("No doctor with id: " + doctorId);
                    }
                    if (oPatient.get().getDoctor() == null){
                        LOGGER.config("this patient has no doctor");
                        throw new NotFoundException("this patient has no doctor");
                    }
                    if (oPatient.get().getDoctor().getId() != oDoctor.get().getId()){
                        LOGGER.config("this patient does not belong to doctor with id: " + doctorId);
                        throw new NotFoundException("this patient does not belong to doctor with id: " + doctorId);
                    }
                }

                patient = oPatient.get();
                LOGGER.finer("User allowed to retrieve a patient.");
                PatientRepresentation result = new PatientRepresentation(patient);
                //result.setPassword("");
                LOGGER.finer("Patient successfully retrieved.");

                return result;
            }
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }

    /**
     *
     * remove a patient
     * @throws NotFoundException
     */
    @Override
    public void remove() throws NotFoundException {

        LOGGER.finer("Removal of patient.");

        // Checking authorization, if role is chief or doctor, not allowed
        ResourceUtils.checkRole(this, Shield.ROLE_CHIEF);
        ResourceUtils.checkRole(this, Shield.ROLE_DOCTOR);

        LOGGER.finer("User allowed to remove a patient.");

        try {
            // Remove patient from foreign key of consultations
            List<Consultation> listOfPatientCons = consultationRepository.findConsultationByPatientId(id);
            listOfPatientCons.forEach(patientCons -> {
                patientCons.setPatient(null);
                consultationRepository.update(patientCons);
            });

            // Remove patient from foreign key of patient data
            List<PatientData> listOfPatientData = patientDataRepository.findDataByPatientId(id);
            listOfPatientData.forEach(patientData -> {
                patientData.setPatient(null);
                patientDataRepository.update(patientData);
            });

            Boolean isDeleted = patientRepository.delete(id);

            if (!isDeleted) {

                LOGGER.config("Patient id does not exist");
                throw new NotFoundException("Patient with following id does not exist: " + id);
            }

            LOGGER.finer("Patient successfully removed.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error when removing a patient ", e);
            throw new ResourceException(e);
        }
    }

    /**
     * Update a patient
     * Set doctor to a patient accordingly to:
     * 1) patient has at least one data storage
     * 2) has passed the time of a month from this storage
     * @param patientReprIn
     * @return updates a patient
     * @throws NotFoundException
     * @throws BadEntityException
     */
    @Override
    public PatientRepresentation store(PatientRepresentation patientReprIn) throws NotFoundException, BadEntityException {

        LOGGER.finer("Update a patient.");

        // Checking authorization,if role is chief or doctor, not allowed
        ResourceUtils.checkRole(this, Shield.ROLE_CHIEF);

        LOGGER.finer("User allowed to update a patient.");

        // Check given entity
        ResourceValidator.notNull(patientReprIn);
        if(doctorId == null) {
            ResourceValidator.validate(patientReprIn);
        }
        LOGGER.finer("Patient checked");

        try {
            Patient patientIn = patientReprIn.createPatient();
            patientIn.setId(id);

            Optional<Patient> patientOut;
            Optional<Patient> oPatient = patientRepository.findById(id);
            setExisting(oPatient.isPresent());

            // if patient exists, update him
            if (isExisting()) {

                LOGGER.finer("Update patient.");
                // Set a doctor to a patient
                if (doctorId != null) {
                    List<PatientData> patientData =
                            patientDataRepository.findDataByPatientId(id);

                    // checking if patient has data
                    if (patientData.size() < 1) {
                        throw new NotFoundException("This patient has not enough data for consultation");
                    }

                    // checking the time passed since patient store data
                    Calendar current = Calendar.getInstance();
                    Calendar creationDate = Calendar.getInstance();
                    final AtomicInteger t = new AtomicInteger(1);

                    patientData.forEach(patientData1 -> {
                        creationDate.setTime(patientData1.getDate());
                        creationDate.add(Calendar.MONTH, +1);
                        creationDate.add(Calendar.DATE, -1);

                        if (creationDate.compareTo(current) < 0) {
                            t.set(0);
                        }
                    });

                    if (t.get() == 1) {
                        throw new NotFoundException("This patient is less than a month old");
                    }

                    Optional<Doctor> oDoctor = doctorRepository.findById(doctorId);
                    patientIn.setDoctor(oDoctor.get());
                }

                // update patient in DB and retrieve the new
                patientOut = patientRepository.update(patientIn);

                // Check if retrieved patient is not null
                // if null it means the id is wrong.
                if (!patientOut.isPresent()) {
                    LOGGER.fine("Patient does not exist.");
                    throw new NotFoundException(
                            "Patient with the following id does not exist: " + patientOut.get().getId());
                }
            } else {
                LOGGER.finer("Resource does not exist.");
                throw new NotFoundException("Resource with the following id does not exist: " + id);
            }

            LOGGER.finer("Patient successfully updated.");
            return new PatientRepresentation(patientOut.get());
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }
}
