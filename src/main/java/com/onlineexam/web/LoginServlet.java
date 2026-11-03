package com.onlineexam.web;

import com.onlineexam.dao.StudentDAO;
import com.onlineexam.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class LoginServlet extends BaseServlet {

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String note = "";
        if ("1".equals(req.getParameter("registered"))) {
            note = "<div class='ok'>Registration successful. Please log in.</div>";
        }
        showForm(resp, note);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email").trim();
        String password = req.getParameter("password").trim();

        Student student = studentDAO.login(email, password);
        if (student == null) {
            showForm(resp, "<div class='err'>Invalid email or password.</div>");
            return;
        }
        req.getSession().setAttribute("student", student);
        resp.sendRedirect("/dashboard");
    }

    private void showForm(HttpServletResponse resp, String note) throws IOException {
        html(resp, "Login",
                "<h2>Student Login</h2>" + note
                        + "<form method='post' action='/login'>"
                        + "Email<input name='email'>"
                        + "Password<input type='password' name='password'>"
                        + "<button type='submit'>Login</button></form>"
                        + "<p><a href='/register'>New student? Register here</a></p>");
    }
}