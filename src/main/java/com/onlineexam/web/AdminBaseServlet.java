package com.onlineexam.web;

import com.onlineexam.model.Admin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// INHERITANCE (multilevel): AdminBaseServlet -> BaseServlet -> HttpServlet
public abstract class AdminBaseServlet extends BaseServlet {

    protected Admin currentAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return (Admin) session.getAttribute("admin");
    }

    // Returns false (and redirects) if no admin is logged in
    protected boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (currentAdmin(req) == null) {
            resp.sendRedirect("/admin/login");
            return false;
        }
        return true;
    }

    // Reads a text parameter safely (never null)
    protected String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    // Reads a number parameter. Returns -1 if missing or not a number.
    protected int intParam(HttpServletRequest req, String name) {
        try {
            return Integer.parseInt(param(req, name));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Redirects to a page with a success or error message
    protected void back(HttpServletResponse resp, String path, boolean ok, String message)
            throws IOException {
        String sep = path.contains("?") ? "&" : "?";
        resp.sendRedirect(path + sep + (ok ? "msg=" : "err=")
                + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }

    // Shows the success or error message from the URL, if any
    protected String notice(HttpServletRequest req) {
        String out = "";
        if (req.getParameter("msg") != null) {
            out += "<div class='ok'>" + esc(req.getParameter("msg")) + "</div>";
        }
        if (req.getParameter("err") != null) {
            out += "<div class='err'>" + esc(req.getParameter("err")) + "</div>";
        }
        return out;
    }

    // Wider page style for admin tables
    protected String wideStyle() {
        return "<style>.box{max-width:900px}table{width:100%;border-collapse:collapse}"
                + "th,td{border-bottom:1px solid #ddd;padding:8px;text-align:left;vertical-align:top}"
                + "form.inline{display:inline}.small{padding:5px 10px;font-size:13px}"
                + ".red{background:#b00020}select,textarea{width:100%;padding:9px;margin:6px 0 14px;"
                + "box-sizing:border-box}</style>";
    }
}