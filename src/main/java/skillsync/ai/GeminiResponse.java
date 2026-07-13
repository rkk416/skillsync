package skillsync.ai;

import java.util.List;
import java.util.Objects;

/**
 * Structured resume analysis result returned by the Gemini API, matching
 * the JSON schema requested from the model:
 * {@code atsScore}, {@code placementScore}, {@code skills},
 * {@code missingSkills}, {@code matchedCompanies}, {@code summary}.
 */
public final class GeminiResponse {

    private int atsScore;
    private int placementScore;
    private List<String> skills;
    private List<String> missingSkills;
    private List<String> matchedCompanies;
    private String summary;

    /**
     * Creates an empty response with default field values, primarily
     * intended for JSON deserialization frameworks that populate fields
     * via reflection.
     */
    public GeminiResponse() {
    }

    /**
     * Creates a fully populated response.
     *
     * @param atsScore         ATS compatibility score, from 0 to 100
     * @param placementScore   overall placement readiness score, from 0 to 100
     * @param skills           technical skills detected in the resume
     * @param missingSkills    technical skills considered missing from the resume
     * @param matchedCompanies companies whose requirements best match the detected skills
     * @param summary          concise professional summary of the candidate's readiness
     */
    public GeminiResponse(int atsScore,
                           int placementScore,
                           List<String> skills,
                           List<String> missingSkills,
                           List<String> matchedCompanies,
                           String summary) {
        this.atsScore = atsScore;
        this.placementScore = placementScore;
        this.skills = skills;
        this.missingSkills = missingSkills;
        this.matchedCompanies = matchedCompanies;
        this.summary = summary;
    }

    /**
     * Returns the ATS compatibility score.
     *
     * @return score between 0 and 100
     */
    public int getAtsScore() {
        return atsScore;
    }

    /**
     * Sets the ATS compatibility score.
     *
     * @param atsScore score between 0 and 100
     */
    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    /**
     * Returns the overall placement readiness score.
     *
     * @return score between 0 and 100
     */
    public int getPlacementScore() {
        return placementScore;
    }

    /**
     * Sets the overall placement readiness score.
     *
     * @param placementScore score between 0 and 100
     */
    public void setPlacementScore(int placementScore) {
        this.placementScore = placementScore;
    }

    /**
     * Returns the technical skills detected in the resume.
     *
     * @return list of detected skill names
     */
    public List<String> getSkills() {
        return skills;
    }

    /**
     * Sets the technical skills detected in the resume.
     *
     * @param skills list of detected skill names
     */
    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    /**
     * Returns the technical skills considered missing from the resume.
     *
     * @return list of missing skill names
     */
    public List<String> getMissingSkills() {
        return missingSkills;
    }

    /**
     * Sets the technical skills considered missing from the resume.
     *
     * @param missingSkills list of missing skill names
     */
    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    /**
     * Returns the companies whose requirements best match the detected skills.
     *
     * @return list of matched company names
     */
    public List<String> getMatchedCompanies() {
        return matchedCompanies;
    }

    /**
     * Sets the companies whose requirements best match the detected skills.
     *
     * @param matchedCompanies list of matched company names
     */
    public void setMatchedCompanies(List<String> matchedCompanies) {
        this.matchedCompanies = matchedCompanies;
    }

    /**
     * Returns the concise professional summary of the candidate's readiness.
     *
     * @return the summary text
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the concise professional summary of the candidate's readiness.
     *
     * @param summary the summary text
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Compares this response to another object for field-by-field equality.
     *
     * @param other the object to compare against
     * @return {@code true} if all fields are equal
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GeminiResponse that)) {
            return false;
        }
        return atsScore == that.atsScore
                && placementScore == that.placementScore
                && Objects.equals(skills, that.skills)
                && Objects.equals(missingSkills, that.missingSkills)
                && Objects.equals(matchedCompanies, that.matchedCompanies)
                && Objects.equals(summary, that.summary);
    }

    /**
     * Computes a hash code consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(atsScore, placementScore, skills, missingSkills, matchedCompanies, summary);
    }

    /**
     * Returns a human-readable representation of this response, useful for
     * logging and debugging.
     *
     * @return a string describing all field values
     */
    @Override
    public String toString() {
        return "GeminiResponse{"
                + "atsScore=" + atsScore
                + ", placementScore=" + placementScore
                + ", skills=" + skills
                + ", missingSkills=" + missingSkills
                + ", matchedCompanies=" + matchedCompanies
                + ", summary='" + summary + '\''
                + '}';
    }
}