package model;

public class Auditor extends User {
    public Auditor(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public Role getRole() { return Role.AUDITOR; }
}
