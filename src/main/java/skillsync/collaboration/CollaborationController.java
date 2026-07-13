package skillsync.collaboration;

import skillsync.model.Student;
import skillsync.model.Team;
import skillsync.model.User;
import skillsync.model.Skill;
import skillsync.model.Recommendation;
import skillsync.repository.TeamRepository;
import skillsync.repository.UserRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentConnectionRepository;
import skillsync.repository.StudentRepository;
import skillsync.repository.RecommendationRepository;
import skillsync.service.CollaborationService;
import skillsync.service.CollaborationServiceImpl;
import skillsync.service.ServiceException;
import skillsync.utils.ControllerSupport;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public final class CollaborationController extends ControllerSupport {
    private final CollaborationService service = new CollaborationServiceImpl();
    private final TeamRepository teams = new TeamRepository();
    private final UserRepository users = new UserRepository();
    private final SkillRepository skills = new SkillRepository();
    private final StudentConnectionRepository connections = new StudentConnectionRepository();
    private final StudentRepository studentRepo = new StudentRepository();
    private final RecommendationRepository recommendationRepo = new RecommendationRepository();

    public record StudentSuggestion(Student student, String name, List<String> skills, double matchScore, String reason, List<String> sharedSkills) {}

    public int getCurrentStudentId() {
        return currentStudentId();
    }

    public Student getCurrentStudent() {
        try {
            return studentRepo.findById(currentStudentId()).orElse(null);
        } catch (SQLException e) {
            return null;
        }
    }

    public Team createTeam(String name, String description) { 
        return service.createTeam(new Team(0, name, description, currentStudentId(), null)); 
    }
    
    public List<Team> teams() { 
        try { return teams.findAll(); } catch (SQLException e) { throw new ServiceException("Unable to load teams", e); } 
    }
    
    public List<Student> teammates() { 
        return service.findTeammates(currentStudentId()); 
    }
    
    public void joinTeam(int teamId) { 
        execute(() -> service.joinTeam(teamId, currentStudentId())); 
    }

    public List<String> getTeamMemberNames(int teamId) {
        try {
            return teams.getMemberNames(teamId);
        } catch (SQLException e) {
            throw new ServiceException("Unable to get team member names", e);
        }
    }

    public List<TeamRepository.TeamMember> getTeamMemberDetails(int teamId) {
        try {
            return teams.getMemberDetails(teamId);
        } catch (SQLException e) {
            throw new ServiceException("Unable to get team member details", e);
        }
    }

    public String getStudentName(int studentId) {
        try {
            return studentRepo.findById(studentId)
                    .flatMap(s -> {
                        try {
                            return users.findById(s.getUserId());
                        } catch (SQLException e) {
                            return java.util.Optional.empty();
                        }
                    })
                    .map(User::getFullName)
                    .orElse("Student #" + studentId);
        } catch (SQLException e) {
            return "Student #" + studentId;
        }
    }

    public User getStudentUser(int studentId) {
        try {
            return studentRepo.findById(studentId)
                    .flatMap(s -> {
                        try {
                            return users.findById(s.getUserId());
                        } catch (SQLException e) {
                            return java.util.Optional.empty();
                        }
                    })
                    .orElse(null);
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean isStudentInTeam(int teamId) {
        try {
            List<Team> studentTeams = teams.findByStudentId(currentStudentId());
            return studentTeams.stream().anyMatch(t -> t.getId() == teamId);
        } catch (SQLException e) {
            return false;
        }
    }

    public void sendRequest(int receiverId) {
        execute(() -> service.sendRequest(currentStudentId(), receiverId));
    }

    public boolean isRequestSent(int receiverId) {
        try {
            return connections.find(currentStudentId(), receiverId).isPresent();
        } catch (SQLException e) {
            return false;
        }
    }

    public List<StudentSuggestion> getTeammateSuggestions() {
        List<Student> bfsStudents = service.findTeammates(currentStudentId());
        List<StudentSuggestion> suggestions = new ArrayList<>();
        
        try {
            Map<Integer, List<Integer>> teamGraph = teams.loadMembershipGraph();
            List<Integer> previousTeammates = teamGraph.getOrDefault(currentStudentId(), List.of());
            
            List<String> mySkillNames = skills.findByStudentId(currentStudentId()).stream().map(Skill::getName).toList();
            Set<Integer> mySkills = skills.findProficienciesByStudent(currentStudentId()).keySet();
            
            for (Student s : bfsStudents) {
                if (s.getId() == currentStudentId()) continue;
                try {
                    String name = users.findById(s.getUserId()).map(User::getFullName).orElse("Student #" + s.getId());
                    List<String> sSkills = skills.findByStudentId(s.getId()).stream().map(Skill::getName).toList();
                    
                    // Compute match score based on Jaccard similarity coefficient
                    Set<Integer> theirSkills = skills.findProficienciesByStudent(s.getId()).keySet();
                    
                    double matchScore = 0.0;
                    if (!mySkills.isEmpty() || !theirSkills.isEmpty()) {
                        Set<Integer> intersection = new HashSet<>(mySkills);
                        intersection.retainAll(theirSkills);
                        Set<Integer> union = new HashSet<>(mySkills);
                        union.addAll(theirSkills);
                        matchScore = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
                    }
                    
                    // Reason for recommendation
                    String reason = "Connected Network";
                    if (previousTeammates.contains(s.getId())) {
                        reason = "Previous Team";
                    } else if (!mySkills.isEmpty()) {
                        Set<Integer> intersection = new HashSet<>(mySkills);
                        intersection.retainAll(theirSkills);
                        if (!intersection.isEmpty()) {
                            reason = "Shared Skills";
                        }
                    }
                    
                    // Find shared skill names
                    List<String> sharedSkills = new ArrayList<>(mySkillNames);
                    sharedSkills.retainAll(sSkills);
                    
                    suggestions.add(new StudentSuggestion(s, name, sSkills, matchScore, reason, sharedSkills));
                } catch (SQLException e) {
                    suggestions.add(new StudentSuggestion(s, "Student #" + s.getId(), List.of(), 0.0, "Connected Network", List.of()));
                }
            }
        } catch (SQLException e) {
            for (Student s : bfsStudents) {
                if (s.getId() == currentStudentId()) continue;
                suggestions.add(new StudentSuggestion(s, "Student #" + s.getId(), List.of(), 0.0, "Connected Network", List.of()));
            }
        }
        
        // Sort by match score in descending order
        suggestions.sort((a, b) -> Double.compare(b.matchScore(), a.matchScore()));
        return suggestions;
    }

    public List<StudentSuggestion> getDiscoveryStudents() {
        List<StudentSuggestion> discoveryList = new ArrayList<>();
        try {
            List<Student> all = studentRepo.findAll();
            for (Student s : all) {
                if (s.getId() == currentStudentId()) continue;
                String name = users.findById(s.getUserId()).map(User::getFullName).orElse("Student #" + s.getId());
                List<String> sSkills = skills.findByStudentId(s.getId()).stream().map(Skill::getName).toList();
                discoveryList.add(new StudentSuggestion(s, name, sSkills, 0.0, "", List.of()));
            }
        } catch (SQLException e) {
            throw new ServiceException("Unable to load student discovery list", e);
        }
        return discoveryList;
    }

    public boolean isStudentRecommended(int targetId) {
        try {
            return recommendationRepo.findAll().stream()
                    .anyMatch(r -> r.getStudentId() == currentStudentId() && r.getTargetId() == targetId && "PEER".equals(r.getRecommendationType()));
        } catch (SQLException e) {
            return false;
        }
    }

    public void recommendStudent(int targetId) {
        execute(() -> {
            try {
                recommendationRepo.create(new Recommendation(0, currentStudentId(), "PEER", targetId, java.math.BigDecimal.valueOf(1.0), "Peer collaboration recommendation", null));
            } catch (SQLException e) {
                throw new ServiceException("Unable to submit recommendation", e);
            }
        });
    }

    public record CollaborationStats(int activeTeams, int myTeams, int studentsAvailable, int recommendedTeammates, int pendingRequests) {}

    public CollaborationStats getStats() {
        try {
            int activeTeams = teams.findAll().size();
            int myTeams = teams.findByStudentId(currentStudentId()).size();
            int studentsAvailable = Math.max(0, studentRepo.findAll().size() - 1);
            int recommendedTeammates = getTeammateSuggestions().size();
            int pendingRequests = (int) connections.findByStudentId(currentStudentId()).stream()
                    .filter(c -> "PENDING".equalsIgnoreCase(c.getStatus()))
                    .count();
            return new CollaborationStats(activeTeams, myTeams, studentsAvailable, recommendedTeammates, pendingRequests);
        } catch (SQLException e) {
            return new CollaborationStats(0, 0, 0, 0, 0);
        }
    }
}
