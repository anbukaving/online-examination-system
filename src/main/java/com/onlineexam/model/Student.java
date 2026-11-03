package com.onlineexam.model;

public class Student extends User {
    private String email;
    private String password;
    private String phone;

    public Student() {
        super();
    }

    public Student(String fullName, String email, String password, String phone) {
        super(0, fullName);
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    public int getStudentId() { return getId(); }
    public void setStudentId(int studentId) { setId(studentId); }

    public String getFullName() { return getName(); }
    public void setFullName(String fullName) { setName(fullName); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String getRole() {
        return "STUDENT";
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("   Email: " + email);
    }
}