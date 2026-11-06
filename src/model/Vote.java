package model;

import java.util.Objects;

// Business key = voterId + ballotId, so a HashSet<Vote> blocks double voting.
public class Vote {
    private final String voterId;
    private final String ballotId;
    private final String candidateId;

    public Vote(String voterId, String ballotId, String candidateId) {
        this.voterId = voterId;
        this.ballotId = ballotId;
        this.candidateId = candidateId;
    }

    public String getVoterId() { return voterId; }
    public String getBallotId() { return ballotId; }
    public String getCandidateId() { return candidateId; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Vote v && voterId.equals(v.voterId) && ballotId.equals(v.ballotId);
    }

    @Override
    public int hashCode() { return Objects.hash(voterId, ballotId); }

    @Override
    public String toString() { return voterId + " -> " + candidateId + " on " + ballotId; }
}
