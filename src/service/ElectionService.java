package service;

import exception.VotingException;
import model.Ballot;
import model.Candidate;
import model.Election;
import model.ElectionStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElectionService {
    private final List<Election> elections = new ArrayList<>();
    private final Map<String, Election> electionsById = new HashMap<>();
    private final Map<String, Ballot> ballotsById = new HashMap<>();
    private final Map<String, Election> electionByBallotId = new HashMap<>();
    private final Map<String, Candidate> candidatesById = new HashMap<>();

    public void createElection(Election election) {
        if (electionsById.containsKey(election.getId())) {
            throw new VotingException("Election " + election.getId() + " already exists");
        }
        elections.add(election);
        electionsById.put(election.getId(), election);
    }

    public void addBallot(String electionId, Ballot ballot) {
        Election election = getElection(electionId);
        requireDraft(election);
        election.addBallot(ballot);
        ballotsById.put(ballot.getId(), ballot);
        electionByBallotId.put(ballot.getId(), election);
    }

    public void addCandidate(Candidate candidate) {
        if (candidatesById.putIfAbsent(candidate.getId(), candidate) != null) {
            throw new VotingException("Candidate " + candidate.getId() + " already exists");
        }
    }

    public void assignCandidate(String candidateId, String ballotId) {
        Candidate candidate = getCandidate(candidateId);
        Ballot ballot = getBallot(ballotId);
        requireDraft(getElectionOfBallot(ballotId));
        ballot.addCandidate(candidate);
    }

    // Only DRAFT -> OPEN -> CLOSED is allowed.
    public void changeStatus(String electionId, ElectionStatus newStatus) {
        Election election = getElection(electionId);
        ElectionStatus oldStatus = election.getStatus();
        if (newStatus.ordinal() != oldStatus.ordinal() + 1) {
            throw new VotingException("Cannot change status from " + oldStatus + " to " + newStatus);
        }
        if (newStatus == ElectionStatus.OPEN) {
            if (election.getBallots().isEmpty()) {
                throw new VotingException("Election has no ballots");
            }
            for (Ballot ballot : election.getBallots()) {
                if (ballot.getCandidates().size() < 2) {
                    throw new VotingException("Ballot '" + ballot.getTitle() + "' needs at least 2 candidates");
                }
            }
        }
        election.setStatus(newStatus);
    }

    public void renameElection(String electionId, String newTitle) {
        getElection(electionId).setTitle(newTitle);
    }

    public void removeElection(String electionId) {
        Election election = getElection(electionId);
        elections.remove(election);
        electionsById.remove(electionId);
        for (Ballot ballot : election.getBallots()) {
            ballotsById.remove(ballot.getId());
            electionByBallotId.remove(ballot.getId());
        }
    }

    public Election getElection(String id) {
        Election election = electionsById.get(id);
        if (election == null) {
            throw new VotingException("Election not found: " + id);
        }
        return election;
    }

    public Ballot getBallot(String id) {
        Ballot ballot = ballotsById.get(id);
        if (ballot == null) {
            throw new VotingException("Ballot not found: " + id);
        }
        return ballot;
    }

    public Candidate getCandidate(String id) {
        Candidate candidate = candidatesById.get(id);
        if (candidate == null) {
            throw new VotingException("Candidate not found: " + id);
        }
        return candidate;
    }

    public Election getElectionOfBallot(String ballotId) {
        getBallot(ballotId);
        return electionByBallotId.get(ballotId);
    }

    public List<Election> getElectionsByStatus(ElectionStatus status) {
        List<Election> result = new ArrayList<>();
        for (Election election : elections) {
            if (election.getStatus() == status) {
                result.add(election);
            }
        }
        return result;
    }

    // Sorting: no comparator (null) = natural order from Comparable.
    public List<Election> getElectionsSorted() {
        return getElectionsSorted(null);
    }

    public List<Election> getElectionsSorted(Comparator<Election> comparator) {
        List<Election> sorted = new ArrayList<>(elections);
        sorted.sort(comparator);
        return sorted;
    }

    public List<Candidate> getCandidatesSorted() {
        return getCandidatesSorted(null);
    }

    public List<Candidate> getCandidatesSorted(Comparator<Candidate> comparator) {
        List<Candidate> sorted = new ArrayList<>(candidatesById.values());
        sorted.sort(comparator);
        return sorted;
    }

    private void requireDraft(Election election) {
        if (election.getStatus() != ElectionStatus.DRAFT) {
            throw new VotingException("Election '" + election.getTitle() + "' is not a draft anymore");
        }
    }
}
