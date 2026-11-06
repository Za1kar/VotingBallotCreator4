package model;

import exception.VotingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Ballot implements Reportable {
    private final String id;
    private final String title;
    private final List<Candidate> candidates = new ArrayList<>();

    public Ballot(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public List<Candidate> getCandidates() { return Collections.unmodifiableList(candidates); }

    public void addCandidate(Candidate candidate) {
        if (candidates.contains(candidate)) {
            throw new VotingException("Candidate " + candidate.getId() + " is already on ballot " + id);
        }
        candidates.add(candidate);
    }

    public boolean hasCandidate(String candidateId) {
        return candidates.stream().anyMatch(c -> c.getId().equals(candidateId));
    }

    @Override
    public String getSummary() { return title + " [" + id + "], candidates: " + candidates.size(); }

    @Override
    public boolean equals(Object o) { return o instanceof Ballot b && id.equals(b.id); }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return title; }
}
