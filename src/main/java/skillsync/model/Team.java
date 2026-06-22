package skillsync.model;

import java.time.LocalDateTime;

public class Team {
    private int id;
    private String name;
    private String description;
    private int createdBy;
    private LocalDateTime createdAt;

    public Team() { }
    public Team(int id, String name, String description, int createdBy, LocalDateTime createdAt) {
        this.id = id; this.name = name; this.description = description; this.createdBy = createdBy; this.createdAt = createdAt;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    @Override public String toString() { return "Team{id=" + id + ", name='" + name + "', description='" + description + "', createdBy=" + createdBy + ", createdAt=" + createdAt + "}"; }
}
