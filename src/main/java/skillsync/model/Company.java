package skillsync.model;

import java.math.BigDecimal;

public class Company {
    private int id;
    private String name;
    private String industry;
    private String website;
    private BigDecimal minimumGpa;

    public Company() { }
    public Company(int id, String name, String industry, String website, BigDecimal minimumGpa) {
        this.id = id; this.name = name; this.industry = industry; this.website = website; this.minimumGpa = minimumGpa;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public BigDecimal getMinimumGpa() { return minimumGpa; }
    public void setMinimumGpa(BigDecimal minimumGpa) { this.minimumGpa = minimumGpa; }
    @Override public String toString() { return "Company{id=" + id + ", name='" + name + "', industry='" + industry + "', website='" + website + "', minimumGpa=" + minimumGpa + "}"; }
}
