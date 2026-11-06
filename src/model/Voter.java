package model;

public class Voter extends User {
    private final int age;
    private final String region;

    public Voter(String id, String name, String email, int age, String region) {
        super(id, name, email);
        if (age < 18) {
            throw new IllegalArgumentException("Voter must be at least 18");
        }
        this.age = age;
        this.region = region;
    }

    @Override
    public Role getRole() { return Role.VOTER; }

    public int getAge() { return age; }
    public String getRegion() { return region; }
}
