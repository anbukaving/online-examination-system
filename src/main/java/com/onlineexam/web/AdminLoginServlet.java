package com.onlineexam.web;

import com.onlineexam.dao.AdminDAO;
import com.onlineexam.model.Admin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class AdminLoginServlet extends BaseServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        showForm(resp, "");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username").trim();
        String password = req.getParameter("password").trim();

        Admin admin = adminDAO.login(username, password);
        if (admin == null) {
            showForm(resp, "<div class='err'>Invalid username or password.</div>");
            return;
        }
        req.getSession().setAttribute("admin", admin);
        resp.sendRedirect("/admin/dashboard");
    }

    private void showForm(HttpServletResponse resp, String note) throws IOException {
        html(resp, "Admin Login",
                "<h2>Admin Login</h2>" + note
                        + "<form method='post' action='/admin/login'>"
                        + "Username<input name='username'>"
                        + "Password<input type='password' name='password'>"
                        + "<button type='submit'>Login</button></form>"
                        + "<p><a href='/'>Back to home</a></p>");
    }
}