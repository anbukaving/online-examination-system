package com.onlineexam.web;

import com.onlineexam.dao.StudentDAO;
import com.onlineexam.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class AdminStudentsServlet extends AdminBaseServlet {

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        List<Student> students = studentDAO.getAllStudents();
        StringBuilder sb = new StringBuilder(wideStyle());
        sb.append("<h2>Manage Students</h2>").append(notice(req));

        if (students.isEmpty()) {
            sb.append("<p>No students registered yet.</p>");
        } else {
            sb.append("<table><tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th>"
                    + "<th>Action</th></tr>");
            for (Student s : students) {
                // POLYMORPHISM: s.getRole() comes from the Student class
                sb.append("<tr><td>").append(s.getStudentId()).append("</td><td>")
                        .append(esc(s.getFullName())).append("</td><td>")
                        .append(esc(s.getEmail())).append("</td><td>")
                        .append(esc(s.getPhone())).append("</td><td>")
                        .append("<form class='inline' method='post' action='/admin/students' ")
                        .append("onsubmit=\"return confirm('Delete this student and their results?')\">")
                        .append("<input type='hidden' name='id' value='").append(s.getStudentId()).append("'>")
                        .append("<button class='small red' type='submit'>Delete</button></form>")
                        .append("</td></tr>");
            }
            sb.append("</table>");
        }
        sb.append("<p><a href='/admin/dashboard'>Back to dashboard</a></p>");
        html(resp, "Students", sb.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        boolean ok = studentDAO.deleteStudent(intParam(req, "id"));
        back(resp, "/admin/students", ok, ok ? "Student deleted." : "Delete failed.");
    }
}