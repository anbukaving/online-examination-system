package com.onlineexam.model;

public class Admin extends User {

    public Admin(int adminId, String username) {
        super(adminId, username);
    }

    public int getAdminId() { return getId(); }
    public String getUsername() { return getName(); }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}