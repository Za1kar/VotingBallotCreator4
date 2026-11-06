package model;

// A record: equals() and hashCode() are generated from all fields (seq is unique).
public record RegistrationRequest(int seq, String name, String email, int age, String region,
                                  RequestPriority priority) implements Comparable<RegistrationRequest> {

    @Override
    public int compareTo(RegistrationRequest other) {
        int result = priority.compareTo(other.priority);
        return result != 0 ? result : Integer.compare(seq, other.seq);
    }
}
