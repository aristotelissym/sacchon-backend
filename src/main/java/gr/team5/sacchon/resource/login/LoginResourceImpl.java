package gr.team5.sacchon.resource.login;

import gr.team5.sacchon.exception.NotFoundException;
import gr.team5.sacchon.model.DatabaseUser;
import gr.team5.sacchon.model.Doctor;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.repository.DoctorRepository;
import gr.team5.sacchon.repository.PatientRepository;
import gr.team5.sacchon.representation.login.DatabaseUserRepresentation;
import gr.team5.sacchon.repository.util.JpaUtil;
import gr.team5.sacchon.resource.DoctorResourceImpl;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.logging.Logger;

public class LoginResourceImpl extends ServerResource implements LoginResource {

    public static final Logger LOGGER = Engine.getLogger(DoctorResourceImpl.class);

    private EntityManager entityManager;
    private String username;
    private String password;
    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;

    @Override
    protected void doRelease() {
        entityManager.close();
    }

    /**
     * Initializes login
     */
    @Override
    protected void doInit() {
        LOGGER.info("Initializing login");

        try {
            entityManager = JpaUtil.getEntityManager();

            patientRepository = new PatientRepository(entityManager);
            doctorRepository = new DoctorRepository(entityManager);

            username = getAttribute("username");
            password = getAttribute("password");
        } catch (Exception e) {
            throw new ResourceException(e);
        }

        LOGGER.info("Initializing login ends");
    }

    @Override
    public DatabaseUserRepresentation getUser() throws NotFoundException {

        if(username == null || password == null){
            throw new NotFoundException("you need to provide username and password");
        }

        DatabaseUser user = entityManager.find(DatabaseUser.class, username);

        switch (user.getRole()) {
            case ROLE_DOCTOR:
                List<Doctor> doctors = doctorRepository.findByName(user.getUsername());
                if (doctors.size() > 1) {
                    throw new NotFoundException("There is something wrong with database");
                }

                return new DatabaseUserRepresentation(user, doctors.get(0).getId());

            case ROLE_PATIENT:
                List<Patient> patients = patientRepository.findByName(user.getUsername());
                if (patients.size() > 1) {
                    throw new NotFoundException("There is something wrong with database");
                }

                return new DatabaseUserRepresentation(user, patients.get(0).getId());

            case ROLE_CHIEF:
                return new DatabaseUserRepresentation(user, -1);
            default:
                return null;
        }
    }
}
