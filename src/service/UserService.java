package service;

import exception.VotingException;
import model.RegistrationRequest;
import model.RequestPriority;
import model.Role;
import model.User;
import model.Voter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class UserService {
    private final Set<User> users = new HashSet<>();
    private final Map<String, User> usersById = new HashMap<>();
    private final Map<Role, List<User>> usersByRole = new EnumMap<>(Role.class);
    private final Set<String> emails = new HashSet<>();
    private final Queue<RegistrationRequest> requests = new PriorityQueue<>();
    private int requestCounter = 0;

    public UserService() {
        for (Role role : Role.values()) {
            usersByRole.put(role, new ArrayList<>());
        }
    }

    public void addUser(User user) {
        if (users.contains(user)) {
            throw new VotingException("Duplicate user id: " + user.getId());
        }
        if (!emails.add(user.getEmail().toLowerCase())) {
            throw new VotingException("Email already used: " + user.getEmail());
        }
        store(user);
    }

    private void store(User user) {
        users.add(user);
        usersById.put(user.getId(), user);
        usersByRole.get(user.getRole()).add(user);
    }

    public User getUser(String id) {
        User user = usersById.get(id);
        if (user == null) {
            throw new VotingException("User not found: " + id);
        }
        return user;
    }

    public User getUser(String id, Role role) {
        User user = getUser(id);
        if (user.getRole() != role) {
            throw new VotingException(id + " does not have role " + role);
        }
        return user;
    }

    // Update: the email index must change together with the user.
    public void updateEmail(String id, String newEmail) {
        User user = getUser(id);
        String oldKey = user.getEmail().toLowerCase();
        String newKey = newEmail.toLowerCase();
        if (!oldKey.equals(newKey) && emails.contains(newKey)) {
            throw new VotingException("Email already used: " + newEmail);
        }
        user.setEmail(newEmail);
        emails.remove(oldKey);
        emails.add(newKey);
    }

    public void removeUser(String id) {
        User user = getUser(id);
        users.remove(user);
        usersById.remove(id);
        usersByRole.get(user.getRole()).remove(user);
        emails.remove(user.getEmail().toLowerCase());
    }

    public List<User> getUsersByRole(Role role) {
        return new ArrayList<>(usersByRole.get(role));
    }

    public List<Voter> getVotersSorted(Comparator<Voter> comparator) {
        List<Voter> voters = new ArrayList<>();
        for (User user : usersByRole.get(Role.VOTER)) {
            voters.add((Voter) user);
        }
        voters.sort(comparator);
        return voters;
    }

    // Registration queue: HIGH priority first, then first come first served.
    public RegistrationRequest submitRequest(String name, String email, int age, String region,
                                             RequestPriority priority) {
        if (age < 18) {
            throw new IllegalArgumentException("Applicant must be at least 18");
        }
        if (!emails.add(email.toLowerCase())) {
            throw new VotingException("Email already used: " + email);
        }
        RegistrationRequest request = new RegistrationRequest(++requestCounter, name, email, age, region, priority);
        requests.offer(request);
        return request;
    }

    public Optional<Voter> processNextRequest(String administratorId) {
        getUser(administratorId, Role.ADMINISTRATOR);
        RegistrationRequest request = requests.poll();
        if (request == null) {
            return Optional.empty();
        }
        Voter voter = new Voter(String.format("VOT-%03d", request.seq()), request.name(),
                request.email(), request.age(), request.region());
        store(voter);
        return Optional.of(voter);
    }
}
