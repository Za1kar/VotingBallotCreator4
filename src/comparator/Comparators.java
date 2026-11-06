package comparator;

import model.Candidate;
import model.Election;
import model.Voter;

import java.util.Comparator;

public final class Comparators {
    public static final Comparator<Election> ELECTION_BY_END_DATE =
            Comparator.comparing(Election::getEndDate).thenComparing(Election::getTitle);

    public static final Comparator<Candidate> CANDIDATE_BY_PARTY =
            Comparator.comparing(Candidate::getParty).thenComparing(Candidate::getName);

    public static final Comparator<Voter> VOTER_BY_REGION =
            Comparator.comparing(Voter::getRegion).thenComparing(Voter::getName);


    private Comparators() { }
}
