package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.Consultation;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.repository.ConsultationRepository;
import gr.team5.sacchon.repository.DoctorRepository;
import gr.team5.sacchon.repository.PatientRepository;
import gr.team5.sacchon.repository.util.JpaUtil;
import gr.team5.sacchon.representation.PatientRepresentation;
import gr.team5.sacchon.security.ResourceUtils;
import gr.team5.sacchon.security.Shield;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.logging.Logger;

public class PatientNeedConsListResourceImpl extends ServerResource implements PatientNeedConsListResource{
    public static  final Logger LOGGER = Engine.getLogger(PatientNeedConsListResourceImpl.class);

    private DoctorRepository doctorRepository;
    private long doctorId;
    private ConsultationRepository consultationRepository;
    private PatientRepository patientRepository;
    private EntityManager entityManager;

    /**
     *  This release method closes the entityManager
    **/
    @Override
    protected void doRelease() {
        entityManager.close();
    }

    protected void doInit(){

        LOGGER.info("Initializing patient list resource starts");
        try {
            entityManager = JpaUtil.getEntityManager();
            doctorRepository = new DoctorRepository(entityManager);
            consultationRepository = new ConsultationRepository(entityManager);
            patientRepository = new PatientRepository(entityManager);
            doctorId = Long.parseLong(getAttribute("doctor_id"));
        } catch (Exception e) {
            throw new ResourceException(e);
        }

        LOGGER.info("Initializing patient list resource ends");
    }

    /**
     *
     * @return patients who do not have a consultation
     * @throws NotFoundException
     */
    @Override
    public List<PatientRepresentation> getPatientsWithNoCons() throws NotFoundException {
        LOGGER.finer("Select all patients that they need consultation.");

        // Check authorization, if role is patient, not allowed
        ResourceUtils.checkRole(this, Shield.ROLE_PATIENT);

        try {
            List<Consultation> consultations = consultationRepository.findConsultationByDoctorId(doctorId);
            List<Patient> patients = patientRepository.findPatientWithDoctorId(doctorId);

            Calendar current = Calendar.getInstance();
            consultations.forEach(consultation -> {
                Calendar expirationDate = Calendar.getInstance();
                expirationDate.setTime(consultation.getDateCreated());
                expirationDate.add(Calendar.MONTH, +1);
                expirationDate.add(Calendar.DATE, -1);

                if(expirationDate.compareTo(current) >= 0){
                    patients.remove(consultation.getPatient());
                }
            });

            List<PatientRepresentation> result = new ArrayList<>();
            patients.forEach(patient -> result.add(
                    new PatientRepresentation(patient)
            ));

            return result;

        } catch (Exception e) {
            throw new NotFoundException("patient list not found");
        }
    }
}
