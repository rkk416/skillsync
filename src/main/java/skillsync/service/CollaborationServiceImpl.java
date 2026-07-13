package skillsync.service;

import skillsync.model.Student;
import skillsync.model.Team;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentConnectionRepository;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CollaborationServiceImpl implements CollaborationService {
    private final StudentRepository students;
    private final TeamRepository teams;
    private final SkillRepository skills = new SkillRepository();
    private final StudentConnectionRepository connections = new StudentConnectionRepository();
    // TODO: Move collaboration requests to StudentConnectionRepository or TeamJoinRequestRepository when the service contract supports persistent workflows.
    private final Map<Integer, Set<Integer>> pendingRequests = new ConcurrentHashMap<>();

    public CollaborationServiceImpl() { this(new StudentRepository(), new TeamRepository()); }
    public CollaborationServiceImpl(StudentRepository students, TeamRepository teams) { this.students = students; this.teams = teams; }

    @Override public List<Student> findTeammates(int studentId) {
        requirePositive(studentId);
        try {
            List<Student> allStudents = students.findAll();
            Map<Integer, Set<Integer>> graph = new HashMap<>();

            // 1. Build graph edges from previous team memberships
            Map<Integer, List<Integer>> teamGraph = teams.loadMembershipGraph();
            for (Map.Entry<Integer, List<Integer>> entry : teamGraph.entrySet()) {
                graph.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).addAll(entry.getValue());
            }

            // 2. Build graph edges from shared skills (Skill Matching)
            Map<Integer, Set<Integer>> skillToStudents = new HashMap<>();
            for (Student s : allStudents) {
                Map<Integer, Integer> proficiencies = skills.findProficienciesByStudent(s.getId());
                for (int skillId : proficiencies.keySet()) {
                    skillToStudents.computeIfAbsent(skillId, k -> new HashSet<>()).add(s.getId());
                }
            }

            for (Set<Integer> studentIds : skillToStudents.values()) {
                for (int id1 : studentIds) {
                    for (int id2 : studentIds) {
                        if (id1 != id2) {
                            graph.computeIfAbsent(id1, k -> new HashSet<>()).add(id2);
                            graph.computeIfAbsent(id2, k -> new HashSet<>()).add(id1);
                        }
                    }
                }
            }

            // 3. Traversal using BFS
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            Set<Integer> visited = new HashSet<>();
            List<Student> result = new ArrayList<>();
            queue.add(studentId); visited.add(studentId);
            while (!queue.isEmpty()) {
                int current = queue.remove();
                for (int neighbor : graph.getOrDefault(current, Set.of())) {
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
        if (team == null || team.getName() == null || team.getName().trim().isBlank()) throw new IllegalArgumentException("Team name is required");
        requirePositive(team.getCreatedBy());
        try { Team created = teams.create(team); teams.addMember(created.getId(), created.getCreatedBy(), "OWNER"); return created; }
        catch (SQLException exception) { throw new ServiceException("Unable to create the team", exception); }
    }

    @Override public void sendRequest(int senderId, int receiverId) {
        requirePositive(senderId); requirePositive(receiverId);
        if (senderId == receiverId) throw new IllegalArgumentException("You cannot send a request to yourself");
        try {
            if (students.findById(senderId).isEmpty() || students.findById(receiverId).isEmpty()) throw new IllegalArgumentException("Student was not found");
            connections.create(new skillsync.model.StudentConnection(senderId, receiverId, "PENDING", null));
            pendingRequests.computeIfAbsent(receiverId, ignored -> ConcurrentHashMap.newKeySet()).add(senderId);
        } catch (SQLException exception) { throw new ServiceException("Unable to send the collaboration request", exception); }
    }

    @Override
    public void joinTeam(int teamId, int studentId) {
        requirePositive(teamId);
        requirePositive(studentId);
        try {
            // Check if student is already a member
            List<Team> myTeams = teams.findByStudentId(studentId);
            for (Team t : myTeams) {
                if (t.getId() == teamId) {
                    throw new IllegalArgumentException("You are already a member of this team");
                }
            }
            
            // Check capacity limit
            List<String> memberNames = teams.getMemberNames(teamId);
            if (memberNames.size() >= 5) {
                throw new IllegalArgumentException("Team is already full (maximum 5 members)");
            }
            
            teams.addMember(teamId, studentId, "MEMBER");
        } catch (SQLException exception) {
            throw new ServiceException("Unable to join team", exception);
        }
    }

    private void requirePositive(int value) { if (value <= 0) throw new IllegalArgumentException("Student id must be positive"); }
}
