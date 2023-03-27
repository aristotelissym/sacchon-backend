package gr.team5.sacchon.security;

import org.restlet.Application;
import org.restlet.data.ChallengeScheme;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;

/**
     * Use this class to protect you app from unauthorized access
 */
public class Shield {
    public static final String ROLE_CHIEF = "admin";
    public static final String ROLE_DOCTOR = "doctor";
    public static final String ROLE_PATIENT = "patient";

    private Application application;

    /**
     * Set up the application
     * @param application the application
     */
    public Shield (Application application){
        this.application = application;
    }

    /**
     *
     * @return an api guard according to CustomVerifier
     */
    public ChallengeAuthenticator createApiGuard (){
        ChallengeAuthenticator apiGuard = new ChallengeAuthenticator(
                application.getContext(),
                ChallengeScheme.HTTP_BASIC,
                "realm");
        Verifier verifier = new CustomVerifier();
        apiGuard.setVerifier(verifier);
        return apiGuard;
    }
}
