package model;

public class Administrator extends User {
    public Administrator(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public Role getRole() { return Role.ADMINISTRATOR; }
}
