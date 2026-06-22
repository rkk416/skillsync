package skillsync.service;

import skillsync.model.Skill;

import java.util.List;

public interface PlacementService {
    double calculatePlacementScore(int studentId);
    List<Skill> findSkillGap(int studentId, int companyId);
    boolean checkEligibility(int studentId, int companyId);
}
