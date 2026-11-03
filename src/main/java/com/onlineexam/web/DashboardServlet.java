package com.onlineexam.web;

import com.onlineexam.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class DashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Student student = currentStudent(req);
        if (student == null) {
            resp.sendRedirect("/login");
            return;
        }
        html(resp, "Dashboard",
                "<h2>Welcome, " + esc(student.getFullName()) + "!</h2>"
                        + "<p>Role: " + student.getRole() + "</p>"
                        + "<p>Email: " + esc(student.getEmail()) + "</p>"
                        + "<p><a class='btn' href='/exams'>Take an exam</a></p>"
                        + "<p><a class='btn' href='/results'>My results</a></p>"
                        + "<p><a class='btn' href='/logout'>Logout</a></p>");
    }
}