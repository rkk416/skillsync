package skillsync.service;

import skillsync.ai.ResumeTextExtractor;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simulates an AI-powered resume analysis engine for the SkillSync platform.
 * <p>
 * This service derives deterministic analysis signals from parsed resume text.
 * The actual document parsing is delegated to {@link ResumeTextExtractor}, so
 * PDF, DOCX and TXT support stays isolated from scoring and matching logic.
 */
public final class ResumeAnalysisService {

    /** File extensions accepted for resume analysis, matched case-insensitively. */
    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".pdf", ".docx", ".txt");

    /** Maximum accepted resume file size, in bytes (5 MB). */
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    /** Minimum accepted resume file size, in bytes, used to reject empty files. */
    private static final long MIN_FILE_SIZE_BYTES = 1L;

    /** Full catalog of technical skills this service is able to recognize. */
    private static final List<String> KNOWN_SKILLS = List.of(
            "Java", "SQL", "HTML", "CSS", "JavaScript",
            "Python", "Spring Boot", "React", "Git", "OOP", "DSA"
    );

    /** Skills required for a strong match against each target company. */
    private static final Map<String, List<String>> COMPANY_SKILL_REQUIREMENTS = new LinkedHashMap<>();

    static {
        COMPANY_SKILL_REQUIREMENTS.put("Google", List.of("Java", "DSA", "OOP", "Python"));
        COMPANY_SKILL_REQUIREMENTS.put("Microsoft", List.of("Java", "SQL", "OOP", "Git"));
        COMPANY_SKILL_REQUIREMENTS.put("Amazon", List.of("DSA", "Java", "SQL", "Git"));
        COMPANY_SKILL_REQUIREMENTS.put("Adobe", List.of("Java", "HTML", "CSS", "JavaScript"));
        COMPANY_SKILL_REQUIREMENTS.put("Infosys", List.of("Java", "SQL", "OOP"));
        COMPANY_SKILL_REQUIREMENTS.put("TCS", List.of("Java", "SQL", "HTML", "CSS"));
        COMPANY_SKILL_REQUIREMENTS.put("Accenture", List.of("SQL", "HTML", "CSS", "JavaScript"));
        COMPANY_SKILL_REQUIREMENTS.put("Wipro", List.of("Java", "SQL", "OOP", "Git"));
    }

    /** Number of top-matching companies returned by {@link #recommendCompanies(File)}. */
    private static final int TOP_COMPANY_RECOMMENDATION_COUNT = 5;

    /**
     * Validates that the given file is a usable resume: it must exist, be a
     * regular readable file, fall within the accepted size range, and carry
     * a supported extension.
     *
     * @param resumeFile the resume file to validate
     * @throws ResumeAnalysisException if the file fails any validation rule
     */
    public void validateResume(File resumeFile) {
        if (resumeFile == null) {
            throw new ResumeAnalysisException("No resume file was provided.");
        }
        if (!resumeFile.exists() || !resumeFile.isFile()) {
            throw new ResumeAnalysisException("The selected resume file could not be found.");
        }
        if (!resumeFile.canRead()) {
            throw new ResumeAnalysisException("The selected resume file cannot be read.");
        }

        String extension = extractExtension(resumeFile);
        boolean supported = SUPPORTED_EXTENSIONS.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(extension));
        if (!supported) {
            throw new ResumeAnalysisException(
                    "Unsupported resume format. Supported formats: PDF, DOCX, TXT.");
        }

        long sizeInBytes = resumeFile.length();
        if (sizeInBytes < MIN_FILE_SIZE_BYTES) {
            throw new ResumeAnalysisException("The resume file appears to be empty.");
        }
        if (sizeInBytes > MAX_FILE_SIZE_BYTES) {
            throw new ResumeAnalysisException("The resume file exceeds the maximum allowed size of 5 MB.");
        }
    }

    /**
     * Calculates an overall resume quality score, reflecting structure,
     * completeness and presentation rather than keyword matching alone.
     *
     * @param resumeFile the resume file to score
     * @return a score between 0 and 100
     * @throws ResumeAnalysisException if the file fails validation
     */
    public int calculateResumeScore(File resumeFile) {
        validateResume(resumeFile);
        long signature = deriveSignature(resumeFile);
        int baseScore = 55 + (int) (signature % 41); // 55 - 95
        int skillBonus = Math.min(detectSkills(resumeFile).size() * 2, 10);
        return clampToPercentage(baseScore + skillBonus);
    }

    /**
     * Calculates an Applicant Tracking System (ATS) compatibility score,
     * reflecting how well the resume would survive automated keyword and
     * formatting screening used by recruiting platforms.
     *
     * @param resumeFile the resume file to score
     * @return a score between 0 and 100
     * @throws ResumeAnalysisException if the file fails validation
     */
    public int calculateATSScore(File resumeFile) {
        validateResume(resumeFile);
        long signature = deriveSignature(resumeFile);
        int baseScore = 50 + (int) ((signature / 7) % 46); // 50 - 95
        int extensionBonus = extractExtension(resumeFile).equalsIgnoreCase(".pdf") ? 5 : 0;
        return clampToPercentage(baseScore + extensionBonus);
    }

    /**
     * Backward-compatible alias for {@link #calculateATSScore(File)}, retained
     * for callers that treat the ATS score as the resume's primary numeric score.
     *
     * @param resumeFile the resume file to score
     * @return a score between 0 and 100
     * @throws ResumeAnalysisException if the file fails validation
     */
    public int calculateScore(File resumeFile) {
        return calculateATSScore(resumeFile);
    }

    /**
     * Detects technical skills present in the resume by scanning its
     * extracted text against the known skill catalog.
     *
     * @param resumeFile the resume file to scan
     * @return the list of detected skills, in catalog order
     * @throws ResumeAnalysisException if the file fails validation
     */
    public List<String> detectSkills(File resumeFile) {
        validateResume(resumeFile);
        String resumeText = extractResumeText(resumeFile);

        return KNOWN_SKILLS.stream()
                .filter(skill -> containsSkill(resumeText, skill))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Finds skills from the known catalog that are absent from the resume.
     *
     * @param resumeFile the resume file to analyze
     * @return the list of missing skills, in catalog order
     * @throws ResumeAnalysisException if the file fails validation
     */
    public List<String> findMissingSkills(File resumeFile) {
        List<String> detected = detectSkills(resumeFile);
        return KNOWN_SKILLS.stream()
                .filter(skill -> !detected.contains(skill))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Calculates a skill-match percentage against every company defined in
     * this service's requirement catalog.
     *
     * @param resumeFile the resume file to match
     * @return a map of company name to match percentage (0-100), in catalog order
     * @throws ResumeAnalysisException if the file fails validation
     */
    public Map<String, Integer> calculateCompanyMatch(File resumeFile) {
        List<String> detectedSkills = detectSkills(resumeFile);
        Map<String, Integer> matchByCompany = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : COMPANY_SKILL_REQUIREMENTS.entrySet()) {
            matchByCompany.put(entry.getKey(), computeMatchPercentage(detectedSkills, entry.getValue()));
        }

        return matchByCompany;
    }

    /**
     * Recommends the top matching companies for the resume, ranked by
     * descending skill-match percentage.
     *
     * @param resumeFile the resume file to match
     * @return the highest-matching company names, best match first
     * @throws ResumeAnalysisException if the file fails validation
     */
    public List<String> recommendCompanies(File resumeFile) {
        Map<String, Integer> matchByCompany = calculateCompanyMatch(resumeFile);

        return matchByCompany.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed())
                .limit(TOP_COMPANY_RECOMMENDATION_COUNT)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Determines the student's overall placement readiness level based on
     * their resume score.
     *
     * @param resumeFile the resume file to evaluate
     * @return one of {@code "Excellent"}, {@code "Good"}, {@code "Average"}, or {@code "Needs Improvement"}
     * @throws ResumeAnalysisException if the file fails validation
     */
    public String getPlacementReadiness(File resumeFile) {
        int resumeScore = calculateResumeScore(resumeFile);

        if (resumeScore >= 85) {
            return "Excellent";
        }
        if (resumeScore >= 70) {
            return "Good";
        }
        if (resumeScore >= 50) {
            return "Average";
        }
        return "Needs Improvement";
    }

    /**
     * Generates a human-readable recommendation guiding the student on how
     * to improve their placement chances, based on their missing skills.
     *
     * @param resumeFile the resume file to evaluate
     * @return a recommendation sentence
     * @throws ResumeAnalysisException if the file fails validation
     */
    public String generateRecommendation(File resumeFile) {
        List<String> missingSkills = findMissingSkills(resumeFile);

        if (missingSkills.isEmpty()) {
            return "Your skill set is strong across all tracked areas. Focus on interview practice and system design.";
        }

        List<String> topMissingSkills = missingSkills.stream()
                .limit(2)
                .collect(Collectors.toList());

        String skillsList = String.join(" and ", topMissingSkills);
        return "Improve " + skillsList + " to increase your placement chances.";
    }

    /**
     * Extracts the textual content of a resume for downstream analysis.
     * <p>
     * Delegates to the shared resume extraction subsystem so all analysis
     * methods operate on the same parsed text used by AI analysis.
     *
     * @param resumeFile the resume file to extract text from
     * @return a deterministic pseudo-text representation of the resume
     */
    private String extractResumeText(File resumeFile) {
        return ResumeTextExtractor.extractText(resumeFile);
    }

    /**
     * Checks whether a skill keyword is present in the extracted resume text.
     *
     * @param resumeText the extracted resume text
     * @param skill      the skill keyword to search for
     * @return {@code true} if the skill is present
     */
    private boolean containsSkill(String resumeText, String skill) {
        return resumeText.toLowerCase().contains(skill.toLowerCase());
    }

    /**
     * Computes the percentage of a company's required skills that are
     * present in the detected skill list.
     *
     * @param detectedSkills   skills detected in the resume
     * @param requiredSkills   skills required by the target company
     * @return match percentage between 0 and 100
     */
    private int computeMatchPercentage(List<String> detectedSkills, List<String> requiredSkills) {
        if (requiredSkills.isEmpty()) {
            return 0;
        }

        long matchedCount = requiredSkills.stream()
                .filter(detectedSkills::contains)
                .count();

        return clampToPercentage((int) Math.round((matchedCount * 100.0) / requiredSkills.size()));
    }

    /**
     * Derives a stable, non-negative numeric signature from a file's name,
     * size and last-modified timestamp, used to generate deterministic
     * pseudo-analysis results until real content parsing is integrated.
     *
     * @param resumeFile the resume file to derive a signature from
     * @return a non-negative signature value
     */
    private long deriveSignature(File resumeFile) {
        String basis = resumeFile.getName() + resumeFile.length() + resumeFile.lastModified();
        long hash = 0;
        for (int i = 0; i < basis.length(); i++) {
            hash = (hash * 31) + basis.charAt(i);
        }
        return Math.abs(hash);
    }

    /**
     * Extracts the lowercase file extension, including the leading dot.
     *
     * @param file the file to inspect
     * @return the extension, for example {@code ".pdf"}, or an empty string if none is present
     */
    private String extractExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot >= 0 ? name.substring(lastDot).toLowerCase() : "";
    }

    /**
     * Clamps a raw numeric value into the valid 0-100 percentage range.
     *
     * @param value the raw value to clamp
     * @return the value bounded between 0 and 100
     */
    private int clampToPercentage(int value) {
        return Math.max(0, Math.min(100, value));
    }

    /**
     * Unchecked exception raised for any resume-analysis failure, such as an
     * invalid, unsupported or unreadable resume file.
     */
    public static final class ResumeAnalysisException extends RuntimeException {

        /**
         * Creates a new exception with the given descriptive message.
         *
         * @param message human-readable description of the failure
         */
        public ResumeAnalysisException(String message) {
            super(message);
        }

        /**
         * Creates a new exception with the given descriptive message and root cause.
         *
         * @param message human-readable description of the failure
         * @param cause   the underlying cause of the failure
         */
        public ResumeAnalysisException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
