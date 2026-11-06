package model;

import java.util.Objects;

public abstract class User implements Comparable<User> {
    private final String id;
    private final String name;
    private String email;

    protected User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        setEmail(email);
    }

    public abstract Role getRole();

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public final void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Bad email: " + email);
        }
        this.email = email;
    }

    @Override
    public int compareTo(User other) { return id.compareTo(other.id); }

    @Override
    public boolean equals(Object o) { return o instanceof User u && id.equals(u.id); }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return getRole() + " " + id + " (" + name + ")"; }
}
