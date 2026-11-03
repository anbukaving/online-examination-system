package com.onlineexam.web;

import com.onlineexam.dao.StudentDAO;
import com.onlineexam.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RegisterServlet extends BaseServlet {

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        showForm(resp, "", "", "", "");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name").trim();
        String email = req.getParameter("email").trim();
        String phone = req.getParameter("phone").trim();
        String password = req.getParameter("password").trim();

        String error = null;
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            error = "Name, email and password are required.";
        } else if (!email.contains("@") || !email.contains(".")) {
            error = "Invalid email.";
        } else if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
            error = "Phone must be exactly 10 digits.";
        } else if (password.length() < 6) {
            error = "Password must be at least 6 characters.";
        } else if (studentDAO.emailExists(email)) {
            error = "This email is already registered.";
        }

        if (error != null) {
            showForm(resp, error, name, email, phone);
            return;
        }

        boolean saved = studentDAO.register(new Student(name, email, password, phone));
        if (saved) {
            resp.sendRedirect("/login?registered=1");
        } else {
            showForm(resp, "Registration failed. Try again.", name, email, phone);
        }
    }

    private void showForm(HttpServletResponse resp, String error, String name, String email,
                          String phone) throws IOException {
        String msg = error.isEmpty() ? "" : "<div class='err'>" + esc(error) + "</div>";
        html(resp, "Register",
                "<h2>Student Registration</h2>" + msg
                        + "<form method='post' action='/register'>"
                        + "Full name<input name='name' value='" + esc(name) + "'>"
                        + "Email<input name='email' value='" + esc(email) + "'>"
                        + "Phone (10 digits, optional)<input name='phone' value='" + esc(phone) + "'>"
                        + "Password (min 6 characters)<input type='password' name='password'>"
                        + "<button type='submit'>Register</button></form>"
                        + "<p><a href='/login'>Already registered? Log in</a></p>");
    }
}