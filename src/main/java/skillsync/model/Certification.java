package skillsync.model;

import java.time.LocalDate;

public class Certification {
    private int id;
    private int studentId;
    private String name;
    private String issuingOrganization;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String credentialUrl;

    public Certification() { }
    public Certification(int id, int studentId, String name, String issuingOrganization, LocalDate issueDate, LocalDate expiryDate, String credentialUrl) {
        this.id = id; this.studentId = studentId; this.name = name; this.issuingOrganization = issuingOrganization;
        this.issueDate = issueDate; this.expiryDate = expiryDate; this.credentialUrl = credentialUrl;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIssuingOrganization() { return issuingOrganization; }
    public void setIssuingOrganization(String issuingOrganization) { this.issuingOrganization = issuingOrganization; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getCredentialUrl() { return credentialUrl; }
    public void setCredentialUrl(String credentialUrl) { this.credentialUrl = credentialUrl; }
    @Override public String toString() { return "Certification{id=" + id + ", studentId=" + studentId + ", name='" + name + "', issuingOrganization='" + issuingOrganization + "', issueDate=" + issueDate + ", expiryDate=" + expiryDate + ", credentialUrl='" + credentialUrl + "'}"; }
}
