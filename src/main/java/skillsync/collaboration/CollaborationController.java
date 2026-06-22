package skillsync.collaboration;

import skillsync.model.Student;
import skillsync.model.Team;
import skillsync.repository.TeamRepository;
import skillsync.service.CollaborationService;
import skillsync.service.CollaborationServiceImpl;
import skillsync.service.ServiceException;
import skillsync.utils.ControllerSupport;

import java.sql.SQLException;
import java.util.List;

public final class CollaborationController extends ControllerSupport {
    private final CollaborationService service = new CollaborationServiceImpl();
    private final TeamRepository teams = new TeamRepository();
    public Team createTeam(String name, String description) { return service.createTeam(new Team(0, name, description, currentStudentId(), null)); }
    public List<Team> teams() { try { return teams.findAll(); } catch (SQLException e) { throw new ServiceException("Unable to load teams", e); } }
    public List<Student> teammates() { return service.findTeammates(currentStudentId()); }
    public void joinTeam(int teamId) { execute(() -> { try { teams.addMember(teamId, currentStudentId(), "MEMBER"); } catch (SQLException e) { throw new ServiceException("Unable to join team", e); } }); }
}
