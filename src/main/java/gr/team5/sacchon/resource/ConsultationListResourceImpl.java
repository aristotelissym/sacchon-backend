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
import gr.team5.sacchon.representation.ConsultationRepresentation;
import gr.team5.sacchon.resource.util.ResourceValidator;
import gr.team5.sacchon.security.ResourceUtils;
import gr.team5.sacchon.security.Shield;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsultationListResourceImpl extends ServerResource implements ConsultationListResource {

    public static  final Logger LOGGER = Engine.getLogger(ConsultationResourceImpl.class);

    private Long patientId;
    private Long doctorId;
    private ConsultationRepository consultationRepository;
    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;
    private EntityManager entityManager;

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

        LOGGER.info("Initializing consultation list resource starts");

        try {
            entityManager = JpaUtil.getEntityManager();
            consultationRepository = new ConsultationRepository(entityManager);

            try {
                doctorId = Long.parseLong((getQueryValue("doctor_id")));
                doctorRepository = new DoctorRepository(entityManager);
            } catch (Exception e) {
                doctorId = null;
            }

            try {
                patientId = Long.parseLong(getQueryValue("patient_id"));
                patientRepository = new PatientRepository(entityManager);
            } catch (Exception e) {
                patientId = null;
            }

        } catch (Exception e) {
            throw new ResourceException(e);
        }

        LOGGER.info("Initializing consultation list resource ends");
    }

    /**
     *
     * @param consultationReprIn representation of a Consultation given by the frontEnd
     * @return a representation of the persisted object
     * @throws BadEntityException
     */
    @Override
    public ConsultationRepresentation add(ConsultationRepresentation consultationReprIn) throws BadEntityException {

        LOGGER.finer("Add new consultation.");

        // Check authorization, if role is patient or chief, not allowed
        //ResourceUtils.checkRole(this, Shield.ROLE_PATIENT);
        ResourceUtils.checkRole(this, Shield.ROLE_CHIEF);

        LOGGER.finer("User allowed to add a consultation.");

        // Check entity
        ResourceValidator.notNull(consultationReprIn);
        ResourceValidator.validate(consultationReprIn);

        LOGGER.finer("consultation checked");

        try {

            // Convert ConsultationRepresentation to Consultation
            Consultation consultationIn = new Consultation();
            consultationIn.setAdvice(consultationReprIn.getAdvice());
            consultationIn.setDateCreated(consultationReprIn.getDateCreated());

            if (patientId == null){
                throw new BadEntityException("Patient is null");
            }

            if (doctorId == null){
                throw new BadEntityException("Doctor is null");
            }

            Optional<Patient> oPatient = patientRepository.findById(patientId);

            if ( oPatient.get().getDoctor().getId() != doctorId ){
                throw new BadEntityException("This patient has different doctor");
            }

            Optional<Doctor> oDoctor = doctorRepository.findById(doctorId);

            List<Consultation> consultations = consultationRepository
                    .findConsultationByPatientAndDoctor(patientId, doctorId);

            Calendar current = Calendar.getInstance();
            final AtomicInteger t = new AtomicInteger(0);

            consultations.forEach(consultation -> {
                Calendar expirationDate = Calendar.getInstance();
                expirationDate.setTime(consultation.getDateCreated());
                expirationDate.add(Calendar.MONTH, +1);
                expirationDate.add(Calendar.DATE, -1);

                if(expirationDate.compareTo(current) >= 0){
                    t.set(1);
                }
            });

            if(t.get() == 1){
                throw new BadEntityException("This patient has an active consultation");
            }

            consultationIn.setPatient(oPatient.get());
            consultationIn.setDoctor(oDoctor.get());

            Optional<Consultation> consultationOptOut = consultationRepository.save(consultationIn);

            Consultation consultation = null;
            if (consultationOptOut.isPresent()) {
                consultation = consultationOptOut.get();
            } else {
                throw new BadEntityException("Consultation has not been created");
            }

            patientRepository.updateHasNotification(patientId, true);

            ConsultationRepresentation result = new ConsultationRepresentation(consultation);

            result.setAdvice(consultation.getAdvice());
            result.setDateCreated(consultation.getDateCreated());
            result.setUri("http://localhost:9000/doctor/" + consultation.getDoctor().getId() + "/patient/" +
                    consultation.getPatient().getId() + "/consultation/" + consultation.getId());

            getResponse().setLocationRef("http://localhost:9000/doctor/" + consultation.getDoctor().getId() + "/patient/" +
                    consultation.getPatient().getId() + "/consultation/" + consultation.getId());
            getResponse().setStatus(Status.SUCCESS_CREATED);

            LOGGER.finer("Consultation successfully added.");

            return result;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error when adding a consultation", ex);
            throw new ResourceException(ex);
        }
    }

    /**
     *
     * @return list of all consultations
     * @throws NotFoundException
     */
    @Override
    public List<ConsultationRepresentation> getConsultations() throws NotFoundException {

        LOGGER.finer("Select all consultations.");

//        // Check authorization, if role is patient, not allowed
//        ResourceUtils.checkRole(this, Shield.ROLE_PATIENT);

        try {
            List<Consultation> consultations;
            List<ConsultationRepresentation> result = new ArrayList<>();

            if((patientId != null) && (doctorId != null)){
                Optional<Patient> oPatient = patientRepository.findById(patientId);

                if (oPatient.get().getDoctor().getId() != doctorId){
                    throw new NotFoundException("This patient has different doctor");
                }
                consultations = consultationRepository
                        .findConsultationByPatientAndDoctor(
                                patientId, doctorId);

            } else if ((patientId == null) && (doctorId != null)) {
                consultations = consultationRepository.findConsultationByDoctorId(doctorId);

            } else if ((patientId != null) && (doctorId == null)) {
                consultations = consultationRepository.findAllConsultationByPatientId(patientId);

            } else {
                consultations = consultationRepository.findAll();
            }

            consultations.forEach(consultation -> result.add(new ConsultationRepresentation(consultation)));

            return result;
        } catch (Exception e) {
            throw new NotFoundException("consultations not found");
        }
    }
}
