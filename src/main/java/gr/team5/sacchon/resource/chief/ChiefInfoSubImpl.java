package gr.team5.sacchon.resource.chief;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.Consultation;
import gr.team5.sacchon.model.PatientData;
import gr.team5.sacchon.repository.ConsultationRepository;
import gr.team5.sacchon.repository.PatientDataRepository;
import gr.team5.sacchon.repository.util.JpaUtil;
import gr.team5.sacchon.representation.chief.ChiefInfoSubRepresentation;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class ChiefInfoSubImpl extends ServerResource implements ChiefInfoSub {
    public static final Logger LOGGER = Engine.getLogger(ChiefInfoSubImpl.class);

    private Long patientId;
    private Long doctorId;
    private Date from;
    private Date to;

    private PatientDataRepository patientDataRepository;
    private ConsultationRepository consultationRepository;
    private EntityManager entityManager;

    @Override
    protected void doRelease() {
        entityManager.close();
    }

    /**
     * Initializes chief get info repository
     */
    @Override
    protected void doInit() {

        LOGGER.info("Initializes chief get info repository");

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
                patientId = Long.parseLong(getQueryValue("patient_id"));
                patientDataRepository = new PatientDataRepository(entityManager);
            } catch (Exception e) {
                patientId = null;
            }

            try {
                doctorId = Long.parseLong(getQueryValue("doctor_id"));
                consultationRepository = new ConsultationRepository(entityManager);
            } catch (Exception e) {
                doctorId = null;
            }

        } catch (Exception e) {
            throw new ResourceException(e);
        }
        LOGGER.info("Initializing chief gets info ends");
    }

    /**
     * Represents:
     * 1) information submissions of a patient over a time range
     * 2) information submissions of a doctor over a time range
     * @return ChiefInfoSubRepresentation
     * @throws NotFoundException
     */
    @Override
    public List<ChiefInfoSubRepresentation> getInfoSubmit() throws NotFoundException {
        LOGGER.info("Start process chief gets information");

        if (from == null || to == null){
            throw new NotFoundException("You need to provide from and to");
        }

        if (doctorId != null && patientId != null){
            throw new NotFoundException("You can only ask a user at the time");
        }

        List<ChiefInfoSubRepresentation> result = new ArrayList<>();

        if (patientId != null){
            List<PatientData> patientData = patientDataRepository.findDataByPatientId(patientId);

            //(patientData1.getDate() > from) && (patientData1.getDate() < to)
            patientData.forEach(patientData1 -> {
                if ((patientData1.getDate().compareTo(from) >= 0) &&
                    (patientData1.getDate().compareTo(to) < 0)) {
                    result.add(new ChiefInfoSubRepresentation(patientData1));
                }
            });
        } else if (doctorId != null) {
            List<Consultation> consultations = consultationRepository.findConsultationByDoctorId(doctorId);

            //(consultation1.getDate() > from) && (consultation1.getDate() < to)
            consultations.forEach(consultation1 -> {
                if ((consultation1.getDateCreated().compareTo(from) >= 0) &&
                        (consultation1.getDateCreated().compareTo(to) < 0)) {
                    result.add(new ChiefInfoSubRepresentation(consultation1));
                }
            });

        } else {
            throw new NotFoundException("You need to provide patient_id or doctor_id");
        }

        LOGGER.info("End process chief gets information");
        return result;
    }
}
