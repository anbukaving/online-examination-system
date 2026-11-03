package com.onlineexam.model;

public abstract class User {
    private int id;
    private String name;

    protected User() {
    }

    protected User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public abstract String getRole();

    public void displayInfo() {
        System.out.println(getRole() + ": " + name + " (ID " + id + ")");
    }
}