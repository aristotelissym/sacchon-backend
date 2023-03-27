package gr.team5.sacchon.representation.login;

import gr.team5.sacchon.model.DatabaseUser;
import gr.team5.sacchon.security.Role;
import lombok.Data;

@Data
public class DatabaseUserRepresentation {
    private String username;
    private String password;
    private Role role;
    private long id;


    public DatabaseUserRepresentation(DatabaseUser user, long id){
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.id = id;
    }

    public DatabaseUser CreateDatabaseUser(){
        DatabaseUser user = new DatabaseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }
}
