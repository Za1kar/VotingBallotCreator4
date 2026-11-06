package model;

import java.util.Objects;

public class Candidate implements Comparable<Candidate> {
    private final String id;
    private final String name;
    private final String party;

    public Candidate(String id, String name, String party) {
        this.id = id;
        this.name = name;
        this.party = (party == null || party.isBlank()) ? "Independent" : party;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getParty() { return party; }

    @Override
    public int compareTo(Candidate other) {
        int result = name.compareTo(other.name);
        return result != 0 ? result : id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object o) { return o instanceof Candidate c && id.equals(c.id); }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return name + " (" + party + ")"; }
}
