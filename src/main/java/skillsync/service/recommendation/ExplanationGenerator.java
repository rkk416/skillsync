package skillsync.service.recommendation;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.model.Student;

import java.util.ArrayList;
import java.util.List;

public final class ExplanationGenerator {
    public List<String> companyReasons(Company company, double coverage, int missingCount) {
        List<String> reasons = new ArrayList<>();
        reasons.add("Skill coverage is " + Math.round(coverage) + "% for " + company.getName() + ".");
        if (company.getIndustry() != null && !company.getIndustry().isBlank()) reasons.add("Industry alignment: " + company.getIndustry() + ".");
        reasons.add(missingCount == 0 ? "No required skills are currently missing." : missingCount + " required skill gap(s) remain.");
        return reasons;
    }

    public List<String> studentReasons(Student candidate, int sharedSkills, int complementarySkills, boolean educationMatch) {
        List<String> reasons = new ArrayList<>();
        reasons.add(sharedSkills + " shared skill signal(s) support collaboration.");
        reasons.add(complementarySkills + " complementary skill signal(s) can balance project work.");
        if (educationMatch) reasons.add("Education context is aligned.");
        if (candidate.getBranch() != null && !candidate.getBranch().isBlank()) reasons.add("Department signal: " + candidate.getBranch() + ".");
        return reasons;
    }

    public List<String> skillReasons(Skill skill, int demandCount) {
        List<String> reasons = new ArrayList<>();
        reasons.add("Required by " + demandCount + " company requirement set(s).");
        if (skill.getCategory() != null && !skill.getCategory().isBlank()) reasons.add("Category: " + skill.getCategory() + ".");
        if (skill.getDescription() != null && !skill.getDescription().isBlank()) reasons.add(skill.getDescription());
        return reasons;
    }

    public List<String> improvements(List<Skill> missingSkills) {
        if (missingSkills.isEmpty()) return List.of("Maintain proficiency and apply to aligned opportunities.");
        return missingSkills.stream().map(Skill::getName).limit(5).toList();
    }
}
