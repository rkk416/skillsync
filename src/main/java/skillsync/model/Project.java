package skillsync.model;

import java.time.LocalDate;

public class Project {
    private int id;
    private int ownerStudentId;
    private String name;
    private String description;
    private String repositoryUrl;
    private LocalDate startDate;
    private LocalDate endDate;

    public Project() { }
    public Project(int id, int ownerStudentId, String name, String description, String repositoryUrl, LocalDate startDate, LocalDate endDate) {
        this.id = id; this.ownerStudentId = ownerStudentId; this.name = name; this.description = description;
        this.repositoryUrl = repositoryUrl; this.startDate = startDate; this.endDate = endDate;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOwnerStudentId() { return ownerStudentId; }
    public void setOwnerStudentId(int ownerStudentId) { this.ownerStudentId = ownerStudentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    @Override public String toString() { return "Project{id=" + id + ", ownerStudentId=" + ownerStudentId + ", name='" + name + "', description='" + description + "', repositoryUrl='" + repositoryUrl + "', startDate=" + startDate + ", endDate=" + endDate + "}"; }
}
