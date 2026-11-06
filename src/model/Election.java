package model;

import exception.VotingException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Election implements Comparable<Election>, Reportable {
    private final String id;
    private String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private ElectionStatus status = ElectionStatus.DRAFT;
    private final List<Ballot> ballots = new ArrayList<>();

    public Election(String id, String title, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date is before start date");
        }
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public ElectionStatus getStatus() { return status; }
    public void setStatus(ElectionStatus status) { this.status = status; }
    public List<Ballot> getBallots() { return Collections.unmodifiableList(ballots); }

    public void addBallot(Ballot ballot) {
        if (ballots.contains(ballot)) {
            throw new VotingException("Ballot " + ballot.getId() + " already exists");
        }
        ballots.add(ballot);
    }

    @Override
    public int compareTo(Election other) {
        int result = startDate.compareTo(other.startDate);
        return result != 0 ? result : id.compareTo(other.id);
    }

    @Override
    public String getSummary() {
        return title + " [" + id + "], " + status + ", ballots: " + ballots.size();
    }

    @Override
    public boolean equals(Object o) { return o instanceof Election e && id.equals(e.id); }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return title; }
}
