package com.onlineexam.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class HomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        html(resp, "Home",
                "<h2>Welcome</h2>"
                        + "<p>Register or log in to take an exam.</p>"
                        + "<a class='btn' href='/login'>Student Login</a> "
                        + "<a class='btn' href='/register'>Register</a>"
                        + "<p><a href='/admin/login'>Admin Login</a></p>");
    }
}