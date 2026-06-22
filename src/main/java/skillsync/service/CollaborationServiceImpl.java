package skillsync.service;

import skillsync.model.Student;
import skillsync.model.Team;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CollaborationServiceImpl implements CollaborationService {
    private final StudentRepository students;
    private final TeamRepository teams;
    private final Map<Integer, Set<Integer>> pendingRequests = new ConcurrentHashMap<>();

    public CollaborationServiceImpl() { this(new StudentRepository(), new TeamRepository()); }
    public CollaborationServiceImpl(StudentRepository students, TeamRepository teams) { this.students = students; this.teams = teams; }

    @Override public List<Student> findTeammates(int studentId) {
        requirePositive(studentId);
        try {
            Map<Integer, List<Integer>> graph = teams.loadMembershipGraph();
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            Set<Integer> visited = new HashSet<>();
            List<Student> result = new ArrayList<>();
            queue.add(studentId); visited.add(studentId);
            while (!queue.isEmpty()) {
                int current = queue.remove();
                for (int neighbor : graph.getOrDefault(current, List.of())) {
                    if (visited.add(neighbor)) {
                        queue.add(neighbor);
                        students.findById(neighbor).ifPresent(result::add);
                    }
                }
            }
            return result;
        } catch (SQLException exception) { throw new ServiceException("Unable to find teammates", exception); }
    }

    @Override public Team createTeam(Team team) {
        if (team == null || team.getName() == null || team.getName().isBlank()) throw new IllegalArgumentException("Team name is required");
        requirePositive(team.getCreatedBy());
        try { Team created = teams.create(team); teams.addMember(created.getId(), created.getCreatedBy(), "OWNER"); return created; }
        catch (SQLException exception) { throw new ServiceException("Unable to create the team", exception); }
    }

    @Override public void sendRequest(int senderId, int receiverId) {
        requirePositive(senderId); requirePositive(receiverId);
        if (senderId == receiverId) throw new IllegalArgumentException("You cannot send a request to yourself");
        try {
            if (students.findById(senderId).isEmpty() || students.findById(receiverId).isEmpty()) throw new IllegalArgumentException("Student was not found");
            pendingRequests.computeIfAbsent(receiverId, ignored -> ConcurrentHashMap.newKeySet()).add(senderId);
        } catch (SQLException exception) { throw new ServiceException("Unable to send the collaboration request", exception); }
    }

    private void requirePositive(int value) { if (value <= 0) throw new IllegalArgumentException("Student id must be positive"); }
}
