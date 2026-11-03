package com.onlineexam.web;

import com.onlineexam.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// ABSTRACTION: shared code for all pages. INHERITANCE: every page extends this class.
public abstract class BaseServlet extends HttpServlet {

    // POLYMORPHISM (overriding): runs before doGet / doPost for every page
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        super.service(req, resp);
    }

    protected void html(HttpServletResponse resp, String title, String body) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().println("<!DOCTYPE html><html><head><meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                + "<title>" + title + "</title><style>"
                + "body{font-family:Arial,sans-serif;background:#f2f5f9;margin:0}"
                + ".bar{background:#1f3a5f;color:#fff;padding:14px 24px;font-size:20px}"
                + ".box{max-width:420px;margin:40px auto;background:#fff;padding:28px;"
                + "border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,.15)}"
                + "input{width:100%;padding:9px;margin:6px 0 14px;box-sizing:border-box}"
                + "button,.btn{background:#1f3a5f;color:#fff;border:0;padding:10px 18px;"
                + "border-radius:5px;text-decoration:none;cursor:pointer;display:inline-block}"
                + ".err{color:#b00020;margin-bottom:10px}.ok{color:#1b7f3b;margin-bottom:10px}"
                + "</style></head><body><div class='bar'>Online Examination System</div>"
                + "<div class='box'>" + body + "</div></body></html>");
    }

    protected Student currentStudent(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return (Student) session.getAttribute("student");
    }

    // Stops HTML characters in user text from breaking the page
    protected String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}