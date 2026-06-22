package skillsync.service;

import skillsync.model.Student;
import skillsync.model.Team;

import java.util.List;

public interface CollaborationService {
    List<Student> findTeammates(int studentId);
    Team createTeam(Team team);
    void sendRequest(int senderId, int receiverId);
}
