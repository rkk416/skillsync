package skillsync.model;

public class Student {
    private int id;
    private int userId;
    private String university;
    private String degree;
    private int graduationYear;
    private String bio;

    public Student() { }

    public Student(int id, int userId, String university, String degree, int graduationYear, String bio) {
        this.id = id;
        this.userId = userId;
        this.university = university;
        this.degree = degree;
        this.graduationYear = graduationYear;
        this.bio = bio;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public int getGraduationYear() { return graduationYear; }
    public void setGraduationYear(int graduationYear) { this.graduationYear = graduationYear; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", userId=" + userId + ", university='" + university + "', degree='" + degree + "', graduationYear=" + graduationYear + ", bio='" + bio + "'}";
    }
}
