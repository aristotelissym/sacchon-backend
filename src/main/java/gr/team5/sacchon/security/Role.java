package gr.team5.sacchon.security;

public enum Role {
    ROLE_NA("n/a"),
    ROLE_CHIEF("admin"),
    ROLE_DOCTOR("doctor"),
    ROLE_PATIENT("patient");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    /**
     * Get the Role and check if it exists inside the enum Role.
     * @param roleParameter a string that describes the role
     * @return role of the user
     */

    public static Role getRoleValue(String roleParameter) {
        for (Role role: Role.values()) {
            if (roleParameter.equals((role.getRoleName()))) {
                return role;
            }
        }

        return ROLE_NA;
    }

}
