package skillsync.placement;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.repository.CompanyRepository;
import skillsync.service.ResumeAnalysisService;

import javafx.concurrent.Task;

import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller layer of the Placement module in the SkillSync MVC architecture.
 * <p>
 * Coordinates data flow between {@code PlacementView}, {@link ResumeAnalysisService}
 * and {@link CompanyRepository}. This class holds no UI references and performs
 * no direct scene-graph manipulation; it exposes plain data and result objects
 * that the view layer binds to its own controls.
 */
public final class PlacementController {

    /** Allowed resume file extensions, evaluated case-insensitively. */
    private static final List<String> ALLOWED_RESUME_EXTENSIONS = List.of(".pdf", ".docx", ".txt");

    /** Static skill requirements used until a relational skill-taxonomy table is introduced. */
    private static final Map<String, List<String>> REQUIRED_SKILLS = Map.of(
            "Google", List.of("Java", "DSA", "System Design", "Problem Solving"),
            "Microsoft", List.of("Java", "SQL", "OOP", "C#"),
            "Amazon", List.of("DSA", "Leadership Principles", "System Design"),
            "Adobe", List.of("Java", "DSA", "Web Technologies")
    );

    /** Fallback skill set applied to companies absent from {@link #REQUIRED_SKILLS}. */
    private static final List<String> DEFAULT_REQUIRED_SKILLS = List.of("Java", "DSA", "Aptitude");

    /** Baseline placement score shown before any resume has been analyzed. */
    private static final double BASELINE_SCORE = 45.0;

    private final CompanyRepository companyRepository;
    private final ResumeAnalysisService resumeAnalysisService;

    /** Skills detected from the most recently analyzed resume. */
    private List<String> lastDetectedSkills = Collections.emptyList();

    /** ATS score returned by the most recent resume analysis. */
    private double lastAtsScore = 0.0;

    /** Composite placement readiness score derived from the last analysis. */
    private double lastPlacementScore = 0.0;

    /**
     * Creates a controller backed by default production implementations
     * of its collaborating repository and service.
     */
    public PlacementController() {
        this(new CompanyRepository(), new ResumeAnalysisService());
    }

    /**
     * Creates a controller with explicit collaborators, primarily intended
     * for unit testing with test doubles.
     *
     * @param companyRepository      source of company records
     * @param resumeAnalysisService  resume analysis engine
     */
    public PlacementController(CompanyRepository companyRepository,
                                ResumeAnalysisService resumeAnalysisService) {
        this.companyRepository = companyRepository;
        this.resumeAnalysisService = resumeAnalysisService;
    }

    /**
     * Retrieves all companies available for placement matching.
     *
     * @return list of companies sourced from the repository
     */
   public List<Company> companies() {
    try {
        return companyRepository.findAll();
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}

    /**
     * Returns the student's current placement readiness score.
     * Falls back to a fixed baseline until a resume has been analyzed.
     *
     * @return score on a 0-100 scale
     */
    public double score() {
        return lastPlacementScore > 0.0 ? lastPlacementScore : BASELINE_SCORE;
    }

    /**
     * Determines whether the student currently meets a company's eligibility bar.
     *
     * @param companyId identifier of the target company
     * @return {@code true} if the current placement score meets the company's requirement
     */
    public boolean eligible(int companyId) {
        Company company = findCompanyOrThrow(companyId);
        double requiredScore = company.getMinimumGpa().doubleValue() * 10.0;
        return score() >= requiredScore;
    }

    /**
     * Computes the list of skills the student is missing for a given company,
     * based on skills detected from the last analyzed resume.
     *
     * @param companyId identifier of the target company
     * @return list of missing skills, empty if none or no resume has been analyzed
     */
    public List<Skill> skillGap(int companyId) {
        Company company = findCompanyOrThrow(companyId);
        List<String> required = requiredSkillsFor(company.getName());

        return required.stream()
                .filter(skillName -> lastDetectedSkills.stream()
                        .noneMatch(detected -> detected.equalsIgnoreCase(skillName)))
                .map(skillName -> {
    Skill skill = new Skill();
    skill.setName(skillName);   
    return skill;
})
                .collect(Collectors.toList());
    }

    /**
     * Produces the list of companies the student is currently eligible for,
     * used to populate the "Recommended Companies" panel.
     *
     * @return list of eligible companies
     */
    public List<Company> recommendations() {
        return companies().stream()
                .filter(company -> isEligibleSafely(company.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Filters the company list in real time based on a free-text search query,
     * matching against company name and industry.
     *
     * @param query text entered by the user in the search field
     * @return filtered list of companies, or the full list if the query is blank
     */
    public List<Company> searchCompanies(String query) {
        if (query == null || query.isBlank()) {
            return companies();
        }

        String normalized = query.trim().toLowerCase();

        return companies().stream()
                .filter(company -> company.getName().toLowerCase().contains(normalized)
                        || company.getIndustry().toLowerCase().contains(normalized))
                .collect(Collectors.toList());
    }

    /**
     * Synchronously analyzes a resume file: validates its type, computes the
     * ATS score, detects skills and suggests matching companies. Updates the
     * controller's cached analysis state used by {@link #score()} and
     * {@link #skillGap(int)}.
     *
     * @param resumeFile file selected via the Choose Resume action
     * @return structured result containing all analysis outputs
     * @throws PlacementOperationException if the file is invalid or analysis fails
     */
    public ResumeAnalysisResult analyzeResume(File resumeFile) {
        validateResumeFile(resumeFile);

        try {
            int atsScore = resumeAnalysisService.calculateScore(resumeFile);
            List<String> skills = resumeAnalysisService.detectSkills(resumeFile);
            List<String> suggestedCompanies = resumeAnalysisService.recommendCompanies(resumeFile);

            applyAnalysisResult(atsScore, skills);

            return new ResumeAnalysisResult(atsScore, lastPlacementScore, skills, suggestedCompanies);
        } catch (RuntimeException ex) {
            throw new PlacementOperationException("Failed to analyze resume: " + ex.getMessage(), ex);
        }
    }

    /**
     * Builds a background {@link Task} that performs the same work as
     * {@link #analyzeResume(File)} while reporting incremental progress and
     * status messages, allowing the view to bind a {@code ProgressBar} and
     * status label without blocking the JavaFX Application Thread.
     *
     * @param resumeFile file selected via the Choose Resume action
     * @return a task producing a {@link ResumeAnalysisResult} on success
     */
    public Task<ResumeAnalysisResult> createResumeAnalysisTask(File resumeFile) {
        return new Task<>() {
            @Override
            protected ResumeAnalysisResult call() {
                updateMessage("Validating resume...");
                updateProgress(0, 100);
                validateResumeFile(resumeFile);

                updateMessage("Calculating ATS score...");
                updateProgress(30, 100);
                int atsScore = resumeAnalysisService.calculateScore(resumeFile);

                updateMessage("Detecting skills...");
                updateProgress(60, 100);
                List<String> skills = resumeAnalysisService.detectSkills(resumeFile);

                updateMessage("Finding matching companies...");
                updateProgress(85, 100);
                List<String> suggestedCompanies = resumeAnalysisService.recommendCompanies(resumeFile);

                applyAnalysisResult(atsScore, skills);

                updateMessage("Analysis complete.");
                updateProgress(100, 100);

                return new ResumeAnalysisResult(atsScore, lastPlacementScore, skills, suggestedCompanies);
            }
        };
    }

    /**
     * Performs a full eligibility and match analysis for a single company,
     * intended for the per-company "Analyze" button.
     *
     * @param companyId identifier of the company to analyze
     * @return structured result containing eligibility, match percentage and skill gap
     * @throws PlacementOperationException if the company cannot be found
     */
    public CompanyMatchResult analyzeCompany(int companyId) {
        Company company = findCompanyOrThrow(companyId);
        double currentScore = score();
        boolean isEligible = eligible(companyId);
        List<Skill> missingSkills = skillGap(companyId);
        double matchPercentage = calculateMatchPercentage(company, currentScore, missingSkills.size());

        return new CompanyMatchResult(company, isEligible, matchPercentage, missingSkills);
    }

    /**
     * Resets cached resume-analysis state so subsequent reads of {@link #score()}
     * and {@link #skillGap(int)} reflect a freshly refreshed dashboard.
     */
    public void refreshDashboard() {
        lastDetectedSkills = Collections.emptyList();
        lastAtsScore = 0.0;
        lastPlacementScore = 0.0;
    }

    /**
     * Retrieves the ATS score computed during the last resume analysis.
     *
     * @return ATS score on a 0-100 scale, or 0 if no resume has been analyzed
     */
    public double lastAtsScore() {
        return lastAtsScore;
    }

    /**
     * Applies a freshly computed resume analysis outcome to the controller's
     * cached state, keeping {@link #score()} and {@link #skillGap(int)} in sync.
     *
     * @param atsScore ATS score returned by the resume analysis service
     * @param skills   skills detected in the resume
     */
    private void applyAnalysisResult(int atsScore, List<String> skills) {
        this.lastDetectedSkills = List.copyOf(skills);
        this.lastAtsScore = atsScore;
        this.lastPlacementScore = computePlacementScore(atsScore, skills.size());
    }

    /**
     * Combines ATS score and detected-skill volume into a single placement
     * readiness score capped at 100.
     *
     * @param atsScore           ATS score from resume analysis
     * @param detectedSkillCount number of skills detected in the resume
     * @return composite placement score on a 0-100 scale
     */
    private double computePlacementScore(int atsScore, int detectedSkillCount) {
        double skillBonus = Math.min(detectedSkillCount * 3.0, 30.0);
        double weightedAtsScore = atsScore * 0.7;
        return Math.min(weightedAtsScore + skillBonus, 100.0);
    }

    /**
     * Calculates a percentage indicating how closely the student's profile
     * matches a company's requirements, combining score proximity and
     * remaining skill gaps.
     *
     * @param company            target company
     * @param currentScore       student's current placement score
     * @param missingSkillCount  number of skills the student is missing
     * @return match percentage on a 0-100 scale
     */
    private double calculateMatchPercentage(Company company, double currentScore, int missingSkillCount) {
        double requiredScore =  company.getMinimumGpa().doubleValue() * 10.0;
        double scoreWeight = Math.min(currentScore / requiredScore, 1.0) * 70.0;
        double skillWeight = Math.max(0.0, 30.0 - (missingSkillCount * 5.0));
        return Math.min(scoreWeight + skillWeight, 100.0);
    }

    /**
     * Validates that a resume file was selected, exists, and has an accepted extension.
     *
     * @param resumeFile file to validate
     * @throws PlacementOperationException if the file is missing or of an unsupported type
     */
    private void validateResumeFile(File resumeFile) {
        if (resumeFile == null || !resumeFile.exists()) {
            throw new PlacementOperationException("Please select a valid resume file.");
        }

        String name = resumeFile.getName().toLowerCase();
        boolean hasAllowedExtension = ALLOWED_RESUME_EXTENSIONS.stream().anyMatch(name::endsWith);

        if (!hasAllowedExtension) {
            throw new PlacementOperationException("Supported resume formats: PDF, DOCX, TXT.");
        }
    }

    /**
     * Looks up a company by its identifier, throwing a descriptive exception
     * when no matching record exists.
     *
     * @param companyId identifier to search for
     * @return the matching {@link Company}
     * @throws PlacementOperationException if no company with the given id exists
     */
    private Company findCompanyOrThrow(int companyId) {

    try {
        return companyRepository.findAll().stream()
                .filter(company -> company.getId() == companyId)
                .findFirst()
                .orElseThrow(() ->
                        new PlacementOperationException(
                                "Company not found for id: " + companyId));
    } catch (SQLException e) {
        throw new PlacementOperationException(
                "Unable to load companies.", e);
    }
}
            
    

    /**
     * Resolves the skill requirements for a company by name, falling back to
     * a default skill set for companies not present in the static requirement map.
     *
     * @param companyName name of the company
     * @return required skills for the company
     */
    private List<String> requiredSkillsFor(String companyName) {
        return REQUIRED_SKILLS.getOrDefault(companyName, DEFAULT_REQUIRED_SKILLS);
    }

    /**
     * Evaluates eligibility for a company while suppressing lookup failures,
     * used internally when building aggregate recommendation lists.
     *
     * @param companyId identifier of the company to check
     * @return {@code true} if eligible, {@code false} if ineligible or not found
     */
    private boolean isEligibleSafely(int companyId) {
        try {
            return eligible(companyId);
        } catch (PlacementOperationException ex) {
            return false;
        }
    }

    /**
     * Immutable result of a resume analysis operation, consumed by the view
     * to update resume score, ATS score, skills and suggested companies.
     */
    public static final class ResumeAnalysisResult {

        private final int atsScore;
        private final double placementScore;
        private final List<String> detectedSkills;
        private final List<String> suggestedCompanies;

        ResumeAnalysisResult(int atsScore, double placementScore,
                              List<String> detectedSkills, List<String> suggestedCompanies) {
            this.atsScore = atsScore;
            this.placementScore = placementScore;
            this.detectedSkills = List.copyOf(detectedSkills);
            this.suggestedCompanies = List.copyOf(suggestedCompanies);
        }

        public int getAtsScore() {
            return atsScore;
        }

        public double getPlacementScore() {
            return placementScore;
        }

        public List<String> getDetectedSkills() {
            return detectedSkills;
        }

        public List<String> getSuggestedCompanies() {
            return suggestedCompanies;
        }
    }

    /**
     * Immutable result of a per-company eligibility and match analysis,
     * consumed by the view to render the Eligibility Result panel.
     */
    public static final class CompanyMatchResult {

        private final Company company;
        private final boolean eligible;
        private final double matchPercentage;
        private final List<Skill> missingSkills;

        CompanyMatchResult(Company company, boolean eligible,
                            double matchPercentage, List<Skill> missingSkills) {
            this.company = company;
            this.eligible = eligible;
            this.matchPercentage = matchPercentage;
            this.missingSkills = List.copyOf(missingSkills);
        }

        public Company getCompany() {
            return company;
        }

        public boolean isEligible() {
            return eligible;
        }

        public double getMatchPercentage() {
            return matchPercentage;
        }

        public List<Skill> getMissingSkills() {
            return missingSkills;
        }
    }

    /**
     * Unchecked exception raised for any recoverable failure within the
     * placement controller, intended to be caught by the view layer and
     * surfaced via a user-facing error dialog.
     */
    public static final class PlacementOperationException extends RuntimeException {

        public PlacementOperationException(String message) {
            super(message);
        }

        public PlacementOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
