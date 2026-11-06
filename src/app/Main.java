package app;

import comparator.Comparators;
import model.Administrator;
import model.Auditor;
import model.Ballot;
import model.Candidate;
import model.Election;
import model.ElectionStatus;
import model.RequestPriority;
import model.Role;
import model.Voter;
import service.ElectionService;
import service.UserService;
import service.VotingService;

import java.time.LocalDate;
import java.util.Optional;

public class Main {
    static final UserService users = new UserService();
    static final ElectionService elections = new ElectionService();
    static final VotingService voting = new VotingService(users, elections);

    public static void main(String[] args) {
        title("1. Staff");
        users.addUser(new Administrator("ADM-01", "Aigerim Sadykova", "aigerim@ballot.kg"));
        users.addUser(new Auditor("AUD-01", "Nurlan Bekov", "nurlan@ballot.kg"));
        System.out.println(users.getUsersByRole(Role.ADMINISTRATOR) + " " + users.getUsersByRole(Role.AUDITOR));

        title("2. Registration queue (PriorityQueue)");
        request("Aibek Turdiev", "aibek@mail.kg", 24, "Bishkek", RequestPriority.NORMAL);
        request("Madina Kenzhebaeva", "madina@mail.kg", 31, "Osh", RequestPriority.LOW);
        request("Timur Alymkulov", "timur@mail.kg", 67, "Bishkek", RequestPriority.HIGH);
        request("Samat Joldoshev", "samat@mail.kg", 45, "Karakol", RequestPriority.NORMAL);
        request("Aizada Mamytova", "aizada@mail.kg", 29, "Osh", RequestPriority.HIGH);
        request("Young Applicant", "young@mail.kg", 16, "Bishkek", RequestPriority.NORMAL);
        request("Copy Of Aibek", "AIBEK@mail.kg", 40, "Naryn", RequestPriority.NORMAL);
        Optional<Voter> voter;
        while ((voter = users.processNextRequest("ADM-01")).isPresent()) {
            System.out.println("Registered " + voter.get());
        }

        title("3. Elections, ballots, candidates");
        elections.createElection(new Election("ELE-01", "Student Council 2026",
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12)));
        elections.createElection(new Election("ELE-02", "Campus Referendum",
                LocalDate.of(2026, 11, 8), LocalDate.of(2026, 11, 15)));
        elections.addBallot("ELE-01", new Ballot("BAL-01", "President"));
        elections.addBallot("ELE-01", new Ballot("BAL-02", "Treasurer"));
        elections.addBallot("ELE-02", new Ballot("BAL-03", "Library budget"));
        elections.addCandidate(new Candidate("CAN-01", "Bermet Asanova", "Unity"));
        elections.addCandidate(new Candidate("CAN-02", "Daniyar Omurov", "Progress"));
        elections.addCandidate(new Candidate("CAN-03", "Elena Petrova", "Unity"));
        elections.addCandidate(new Candidate("CAN-04", "Kanat Ismailov", null));
        elections.addCandidate(new Candidate("CAN-05", "Yes", "Referendum"));
        elections.addCandidate(new Candidate("CAN-06", "No", "Referendum"));
        for (String[] pair : new String[][]{{"CAN-01", "BAL-01"}, {"CAN-02", "BAL-01"}, {"CAN-04", "BAL-01"},
                {"CAN-02", "BAL-02"}, {"CAN-03", "BAL-02"}, {"CAN-05", "BAL-03"}}) {
            elections.assignCandidate(pair[0], pair[1]);
        }
        System.out.println("Same candidate twice:");
        run(() -> elections.assignCandidate("CAN-02", "BAL-01"));
        System.out.println("Vote before the election is open:");
        run(() -> voting.submitVote("VOT-001", "BAL-01", "CAN-01"));
        System.out.println("Open ELE-02 with a one-candidate ballot:");
        run(() -> elections.changeStatus("ELE-02", ElectionStatus.OPEN));

        elections.assignCandidate("CAN-06", "BAL-03");
        elections.changeStatus("ELE-01", ElectionStatus.OPEN);
        elections.changeStatus("ELE-02", ElectionStatus.OPEN);
        System.out.println("Back to DRAFT:");
        run(() -> elections.changeStatus("ELE-01", ElectionStatus.DRAFT));

        title("4. Votes (ArrayDeque)");
        run(() -> voting.submitVote("VOT-001", "BAL-01", "CAN-01"));
        run(() -> voting.submitVote("VOT-002", "BAL-01", "CAN-02"));
        run(() -> voting.submitVote("VOT-003", "BAL-01", "CAN-01"));
        run(() -> voting.submitVote("VOT-004", "BAL-01", "CAN-04"));
        run(() -> voting.submitVote("VOT-001", "BAL-02", "CAN-02"));
        run(() -> voting.submitVote("VOT-002", "BAL-02", "CAN-03"));
        run(() -> voting.submitVote("VOT-001", "BAL-03", "CAN-05"));
        System.out.println("Invalid votes:");
        run(() -> voting.submitVote("VOT-001", "BAL-01", "CAN-02"));
        run(() -> voting.submitVote("VOT-002", "BAL-01", "CAN-06"));
        run(() -> voting.submitVote("VOT-999", "BAL-01", "CAN-01"));
        run(() -> voting.submitVote("ADM-01", "BAL-01", "CAN-01"));
        System.out.println("Pending votes: " + voting.getPendingVotes());

        title("5. Auditor counts votes (poll)");
        voting.countNextVote("AUD-01").ifPresent(v -> System.out.println("Counted " + v));
        System.out.println("Administrator tries to count:");
        run(() -> voting.countNextVote("ADM-01"));
        System.out.println("Voter VOT-004 is removed while the vote is waiting:");
        voting.removeVoter("VOT-004");
        System.out.println("Pending votes: " + voting.getPendingVotes());
        System.out.println("Counted the rest: " + voting.countAllVotes("AUD-01"));
        System.out.println("Queue is empty: " + voting.countNextVote("AUD-01").isEmpty());
        voting.printResults("BAL-01");
        voting.printResults("BAL-02");
        voting.printResults("BAL-03");

        title("6. Sorting and filtering");
        System.out.println("Elections (start date): " + elections.getElectionsSorted());
        System.out.println("Elections (end date):   " + elections.getElectionsSorted(Comparators.ELECTION_BY_END_DATE));
        System.out.println("Candidates (name): " + elections.getCandidatesSorted().stream().map(Candidate::getName).toList());
        System.out.println("Candidates (party):");
        elections.getCandidatesSorted(Comparators.CANDIDATE_BY_PARTY)
                .forEach(c -> System.out.println("  " + c.getParty() + " - " + c.getName()));
        System.out.println("Voters (region):");
        users.getVotersSorted(Comparators.VOTER_BY_REGION).forEach(v -> System.out.println("  " + v.getRegion() + " - " + v.getName()));
        System.out.println("Open elections: " + elections.getElectionsByStatus(ElectionStatus.OPEN));

        title("7. Update and synchronized remove");
        users.updateEmail("VOT-002", "madina.k@mail.kg");
        System.out.println("Updated: " + users.getUser("VOT-002").getEmail());
        System.out.println("Email of another user:");
        run(() -> users.updateEmail("VOT-002", "aibek@mail.kg"));
        elections.renameElection("ELE-02", "Campus Library Referendum");
        System.out.println(elections.getElection("ELE-02").getSummary());
        elections.changeStatus("ELE-01", ElectionStatus.CLOSED);
        System.out.println("Vote in a closed election:");
        run(() -> voting.submitVote("VOT-003", "BAL-02", "CAN-03"));
        voting.removeElection("ELE-02");
        System.out.println("Results of the removed election:");
        run(() -> voting.printResults("BAL-03"));
        voting.removeVoter("VOT-005");
        System.out.println("Removed voter:");
        run(() -> users.getUser("VOT-005"));
    }

    static void request(String name, String email, int age, String region, RequestPriority priority) {
        run(() -> {
            users.submitRequest(name, email, age, region, priority);
            System.out.println("Submitted: " + name + " [" + priority + "]");
        });
    }

    static void run(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    static void title(String text) {
        System.out.println("\n---------- " + text + " ----------");
    }
}
