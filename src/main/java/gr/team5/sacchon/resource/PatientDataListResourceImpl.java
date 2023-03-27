package gr.team5.sacchon.resource;

import gr.team5.sacchon.exception.BadEntityException;
import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.model.PatientData;
import gr.team5.sacchon.repository.PatientDataRepository;
import gr.team5.sacchon.repository.PatientRepository;
import gr.team5.sacchon.repository.util.JpaUtil;
import gr.team5.sacchon.representation.PatientDataRepresentation;
import gr.team5.sacchon.resource.util.ResourceValidator;
import gr.team5.sacchon.security.ResourceUtils;
import gr.team5.sacchon.security.Shield;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientDataListResourceImpl extends ServerResource implements PatientDataListResource {

    public static  final Logger LOGGER = Engine.getLogger(PatientDataResourceImpl.class);

    private long id;
    private PatientDataRepository patientDataRepository;
    private EntityManager entityManager;

    private Date dateFrom;
    private Date dateTo;

    /**
     * This release method closes the entityManager
     */
    @Override
    protected void doRelease() {
        entityManager.close();
    }

    /**
     * Initializes patient data repository
     */
    @Override
    protected void doInit() {

        LOGGER.info("Initializing patient data list resource starts");

        try {
            entityManager = JpaUtil.getEntityManager();
            patientDataRepository = new PatientDataRepository(entityManager);
            id = Long.parseLong(getAttribute("id"));

            try {
                String startDateString = getQueryValue("from");
                String   endDateString = getQueryValue("to");
                String[] words = startDateString.split("-");

                dateFrom = new Date(Integer.parseInt(words[0])-1900,
                        Integer.parseInt(words[1]) - 1, Integer.parseInt(words[2])  );

                words = endDateString.split("-");
                dateTo = new Date(Integer.parseInt(words[0])-1900,
                        Integer.parseInt(words[1]) - 1, Integer.parseInt(words[2]) + 1 );

            } catch(Exception e) {
                dateFrom = null;
                dateTo = null;
            }
        } catch (Exception e) {
            throw new ResourceException(e);
        }
        LOGGER.info("Initializing patient data list resource ends");
    }

    /**
     *
     * @param patientDataReprIn representation of PatientData given by the frontEnd
     * @return a representation of the persisted object
     * @throws BadEntityException
     */
    @Override
    public PatientDataRepresentation add(PatientDataRepresentation patientDataReprIn) throws BadEntityException {

        LOGGER.finer("Add new patient data.");

        // Check authorization, if role is doctor or chief now allowed
        ResourceUtils.checkRole(this, Shield.ROLE_DOCTOR);
        ResourceUtils.checkRole(this, Shield.ROLE_CHIEF);
        LOGGER.finer("User allowed to add patient data.");

        // Check entity
        ResourceValidator.notNull(patientDataReprIn);
        ResourceValidator.validate(patientDataReprIn);

        LOGGER.finer("patient data checked");

        PatientRepository patientRepository = new PatientRepository(entityManager);

        try {

            // Convert PatientDataRepresentation to PatientData
            PatientData patientDataIn = new PatientData();
            patientDataIn.setBloodGlucose(patientDataReprIn.getBloodGlucose());
            patientDataIn.setCarbIntake(patientDataReprIn.getCarbIntake());
            patientDataIn.setDate(patientDataReprIn.getDate());

            Optional<Patient> oPatient = patientRepository.findById(id);
            patientDataIn.setPatient(oPatient.get());

            Optional<PatientData> patientDataOptOut = patientDataRepository.save(patientDataIn);

            PatientData patientData = null;
            if (patientDataOptOut.isPresent()) {
                patientData = patientDataOptOut.get();
            }
            else {
                throw new BadEntityException("Patient data has not been created");
            }

            PatientDataRepresentation result = new PatientDataRepresentation(patientData);

            result.setBloodGlucose(patientData.getBloodGlucose());
            result.setCarbIntake(patientData.getCarbIntake());
            result.setDate(patientData.getDate());

            result.setUri("http://localhost:9000/patient/" +
                    patientData.getPatient().getId() + "/data/" + patientData.getId());

            getResponse().setLocationRef("http://localhost:9000/patient/" +
                    patientData.getPatient().getId() + "/data/" + patientData.getId());
            getResponse().setStatus(Status.SUCCESS_CREATED);

            LOGGER.finer("Patient data successfully added.");

            return result;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error when adding a patient", ex);
            throw new ResourceException(ex);
        }
    }

    /**
     *
     * @return list of all patients data
     * @throws NotFoundException
     */
    @Override
    public List<PatientDataRepresentation> getPatientsData() throws NotFoundException {

        LOGGER.finer("Select all patients data.");

        // Everyone has access to these data

        try {

            List<PatientData> patientsData;
            List<Double>  bloodGlucoseAvg;
            List<Double> carbIntakeAvg;
            List<PatientDataRepresentation> result = new ArrayList<>();

            if (dateFrom == null || dateTo == null) {
                patientsData   = patientDataRepository.findDataByPatientId(id);
                patientsData.forEach(patientData -> result.add(new PatientDataRepresentation(patientData)));
            } else {
                bloodGlucoseAvg = patientDataRepository.findBloodGlucoseFromTo(id, dateFrom, dateTo);
                carbIntakeAvg = patientDataRepository.findCarbIntakeFromTo(id, dateFrom, dateTo);
                for(int i = 0; i < bloodGlucoseAvg.size(); i++){
                    result.add(
                            new PatientDataRepresentation(
                                    bloodGlucoseAvg.get(i),
                                    carbIntakeAvg.get(i))
                    );
                }
            }
            return result;
        } catch (Exception e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}
