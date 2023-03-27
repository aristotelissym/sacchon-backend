package gr.team5.sacchon.model;

import gr.team5.sacchon.security.Role;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A class for sign in as a specific user
 * @author One-To-Fix-Them-All
 */
@Data
@Entity
public class DatabaseUser {
    @Id
    private String username;

    private String password;
    private Role role;
}
