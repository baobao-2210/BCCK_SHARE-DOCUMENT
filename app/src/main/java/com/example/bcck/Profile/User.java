package com.example.bcck.Profile;

public class User {
    private String fullName;
    private String className;
    private String department;
    private String courseYear; // Khóa
    private String email;
    private String avatarUrl;

    // 1. Constructor rỗng (BẮT BUỘC cho Firebase)
    public User() { }

    // 2. Constructor đầy đủ
    public User(String fullName, String className, String department, String courseYear, String email, String avatarUrl) {
        this.fullName = fullName;
        this.className = className;
        this.department = department;
        this.courseYear = courseYear;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    // 3. Getter methods (Để lấy dữ liệu ra)
    public String getFullName() { return fullName; }
    public String getClassName() { return className; }
    public String getDepartment() { return department; }
    public String getCourseYear() { return courseYear; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }

    // 4. Setter methods (Để gán dữ liệu vào - BỔ SUNG THÊM)
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setClassName(String className) { this.className = className; }
    public void setDepartment(String department) { this.department = department; }
    public void setCourseYear(String courseYear) { this.courseYear = courseYear; }
    public void setEmail(String email) { this.email = email; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}