package com.onlineexam.web;

import com.onlineexam.model.Admin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class AdminDashboardServlet extends AdminBaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;
        Admin admin = currentAdmin(req);

        html(resp, "Admin Dashboard",
                "<h2>Admin: " + esc(admin.getUsername()) + "</h2>"
                        + "<p>Role: " + admin.getRole() + "</p>"
                        + "<p><a class='btn' href='/admin/subjects'>Manage subjects</a></p>"
                        + "<p><a class='btn' href='/admin/exams'>Manage exams and questions</a></p>"
                        + "<p><a class='btn' href='/admin/students'>Manage students</a></p>"
                        + "<p><a class='btn' href='/admin/logout'>Logout</a></p>");
    }
}