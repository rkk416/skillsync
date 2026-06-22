package skillsync.placement;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.repository.CompanyRepository;
import skillsync.service.PlacementService;
import skillsync.service.PlacementServiceImpl;
import skillsync.service.ServiceException;
import skillsync.utils.ControllerSupport;

import java.sql.SQLException;
import java.util.List;

public final class PlacementController extends ControllerSupport {
    private final PlacementService service = new PlacementServiceImpl();
    private final CompanyRepository companies = new CompanyRepository();
    public List<Company> companies() { try { return companies.findAll(); } catch (SQLException e) { throw new ServiceException("Unable to load companies", e); } }
    public double score() { return service.calculatePlacementScore(currentStudentId()); }
    public boolean eligible(int companyId) { return service.checkEligibility(currentStudentId(), companyId); }
    public List<Skill> skillGap(int companyId) { return service.findSkillGap(currentStudentId(), companyId); }
}
