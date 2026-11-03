package com.onlineexam.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// INHERITANCE: our servlet extends HttpServlet and overrides doGet (POLYMORPHISM)
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.getWriter().println("<h1>Online Examination System</h1>");
        resp.getWriter().println("<p>The web server is running.</p>");
    }
}