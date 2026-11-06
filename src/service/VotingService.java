package service;

import exception.VotingException;
import model.Ballot;
import model.Candidate;
import model.ElectionStatus;
import model.Role;
import model.Vote;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public class VotingService {
    private final UserService userService;
    private final ElectionService electionService;
    private final Queue<Vote> voteQueue = new ArrayDeque<>();
    private final Set<Vote> submittedVotes = new HashSet<>();
    private final Map<String, Map<String, Integer>> results = new HashMap<>();

    public VotingService(UserService userService, ElectionService electionService) {
        this.userService = userService;
        this.electionService = electionService;
    }

    // Voter: put a vote into the queue.
    public void submitVote(String voterId, String ballotId, String candidateId) {
        userService.getUser(voterId, Role.VOTER);
        Ballot ballot = electionService.getBallot(ballotId);
        if (electionService.getElectionOfBallot(ballotId).getStatus() != ElectionStatus.OPEN) {
            throw new VotingException("Election is not open for voting");
        }
        if (!ballot.hasCandidate(candidateId)) {
            throw new VotingException("Candidate " + candidateId + " is not on ballot " + ballotId);
        }
        Vote vote = new Vote(voterId, ballotId, candidateId);
        if (!submittedVotes.add(vote)) {
            throw new VotingException("Voter " + voterId + " already voted on ballot " + ballotId);
        }
        voteQueue.offer(vote);
    }

    // Auditor: take the next vote from the queue and count it.
    public Optional<Vote> countNextVote(String auditorId) {
        userService.getUser(auditorId, Role.AUDITOR);
        Vote vote = voteQueue.poll();
        if (vote == null) {
            return Optional.empty();
        }
        results.computeIfAbsent(vote.getBallotId(), key -> new HashMap<>())
                .merge(vote.getCandidateId(), 1, Integer::sum);
        return Optional.of(vote);
    }

    public int countAllVotes(String auditorId) {
        int counted = 0;
        while (countNextVote(auditorId).isPresent()) {
            counted++;
        }
        return counted;
    }

    // Synchronized removal: users, queue and vote registry change together.
    public void removeVoter(String voterId) {
        userService.getUser(voterId, Role.VOTER);
        userService.removeUser(voterId);
        voteQueue.removeIf(v -> v.getVoterId().equals(voterId));
        submittedVotes.removeIf(v -> v.getVoterId().equals(voterId));
    }

    // Removes the election, its ballots, pending votes and counted results.
    public void removeElection(String electionId) {
        Set<String> ballotIds = new HashSet<>();
        for (Ballot ballot : electionService.getElection(electionId).getBallots()) {
            ballotIds.add(ballot.getId());
        }
        electionService.removeElection(electionId);
        voteQueue.removeIf(v -> ballotIds.contains(v.getBallotId()));
        submittedVotes.removeIf(v -> ballotIds.contains(v.getBallotId()));
        results.keySet().removeAll(ballotIds);
    }

    public int getPendingVotes() {
        return voteQueue.size();
    }

    public void printResults(String ballotId) {
        Ballot ballot = electionService.getBallot(ballotId);
        Map<String, Integer> counts = results.getOrDefault(ballotId, Map.of());
        List<Candidate> ranking = new ArrayList<>(ballot.getCandidates());
        ranking.sort((a, b) -> {
            int byVotes = counts.getOrDefault(b.getId(), 0) - counts.getOrDefault(a.getId(), 0);
            return byVotes != 0 ? byVotes : a.compareTo(b);
        });
        System.out.println("Results for " + ballot.getTitle() + ":");
        for (Candidate candidate : ranking) {
            System.out.println("  " + candidate + " - " + counts.getOrDefault(candidate.getId(), 0));
        }
    }
}
