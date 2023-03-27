package gr.team5.sacchon.repository;

import gr.team5.sacchon.model.DatabaseUser;
import gr.team5.sacchon.model.Patient;
import gr.team5.sacchon.security.Role;
import org.restlet.resource.ServerResource;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * PatientRepository uses EntityManager from JpaUtil
 * to get/delete/update a patient & save a new one
 */
public class PatientRepository extends ServerResource {

    private EntityManager entityManager;

    /**
     * PatientRepository constructor
     * @param entityManager
     */
    public PatientRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Find patient by primary key id
     * @param id
     * @return
     */
    public Optional<Patient> findById(Long id) {
        Patient patient = entityManager.find(Patient.class, id);
        return patient != null ? Optional.of(patient) : Optional.empty();
    }


    /**
     * find patient by name
     * @param name
     * @return
     */
    public List<Patient> findByName(String name){
        return entityManager.createQuery("FROM Patient WHERE username = :name").setParameter("name", name).getResultList();
    }


    /**
     * Find all patients
     * @return
     */
    public List<Patient> findAll() {
        return entityManager.createQuery("from Patient").getResultList();
    }

    /**
     * Find patients with doctor id  null
     * @return
     */
    public List<Patient> findPatientWithDoctorIdNull() {
        List<Patient> patientList = entityManager
                .createQuery("SELECT p FROM Patient p" +
                        " WHERE doctor_id IS NULL", Patient.class)
                .getResultList();

        return patientList;
    }

    /**
     * Find patients for specific doctor id
     * @param id
     * @return
     */
    public List<Patient> findPatientWithDoctorId(Long id) {
        List<Patient> patientList = entityManager
                .createQuery("SELECT p FROM Patient p" +
                        " WHERE doctor_id = :id", Patient.class)
                .setParameter("id",id)
                .getResultList();

        return patientList;
    }

    /**
     * Save a new patient
     * @param patient
     * @return
     */
    public Optional<Patient> save(Patient patient){
        DatabaseUser user = new DatabaseUser();
        user.setUsername(patient.getUsername());
        user.setPassword(patient.getPassword());
        user.setRole(Role.ROLE_PATIENT);

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(patient);
            entityManager.persist(user);
            entityManager.getTransaction().commit();
            return Optional.of(patient);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Update, set a doctor to a patient
     * @param patient
     * @return
     */
    public Optional<Patient> update(Patient patient){
        Patient in = entityManager.find(Patient.class, patient.getId());
        DatabaseUser user = entityManager.find(DatabaseUser.class, in.getUsername());

        in.setHasNotification(patient.isHasNotification());
        in.setDoctor(patient.getDoctor());

        if(patient.getPassword() != null && !patient.getPassword().isEmpty()) {
            in.setPassword(patient.getPassword());
            user.setPassword(patient.getPassword());
        }

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(in);
            entityManager.persist(user);
            entityManager.getTransaction().commit();
            return Optional.of(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Update notification for a new consultation
     * @param id
     * @param notification
     */
    public void updateHasNotification(long id, boolean notification){
        Patient patientIn = entityManager.find(Patient.class, id);
        patientIn.setHasNotification(notification);
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(patientIn);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete account
     * @param id
     * @return
     */
    public boolean delete(Long id){
        Optional<Patient> tempPatient = findById(id);
        if (tempPatient.isPresent()){
            Patient toDelete = tempPatient.get();
            DatabaseUser user = entityManager.find(DatabaseUser.class, toDelete.getUsername());

            try{
                entityManager.getTransaction().begin();
                entityManager.remove(toDelete);
                entityManager.remove(user);
                entityManager.getTransaction().commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}