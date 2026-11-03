package com.onlineexam.util;

import com.onlineexam.model.Admin;
import com.onlineexam.model.Student;
import com.onlineexam.model.User;

public class OopDemo {
    public static void main(String[] args) {
        User[] users = {
                new Student("Anbu Kavin", "anbu@gmail.com", "secret1", "9999999999"),
                new Admin(1, "admin")
        };

        for (User u : users) {
            u.displayInfo();
        }
    }
}