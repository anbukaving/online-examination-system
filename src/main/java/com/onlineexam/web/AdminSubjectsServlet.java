package com.onlineexam.web;

import com.onlineexam.dao.SubjectDAO;
import com.onlineexam.model.Subject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class AdminSubjectsServlet extends AdminBaseServlet {

    private final SubjectDAO subjectDAO = new SubjectDAO();

    // READ: list subjects, plus the add form (or the edit form)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        List<Subject> subjects = subjectDAO.getAllSubjects();
        Subject editing = null;
        String editParam = req.getParameter("edit");
        if (editParam != null) {
            for (Subject s : subjects) {
                if (String.valueOf(s.getSubjectId()).equals(editParam)) {
                    editing = s;
                }
            }
        }

        StringBuilder sb = new StringBuilder(wideStyle());
        sb.append("<h2>Manage Subjects</h2>");

        String msg = req.getParameter("msg");
        if (msg != null) {
            sb.append("<div class='ok'>").append(esc(msg)).append("</div>");
        }
        String err = req.getParameter("err");
        if (err != null) {
            sb.append("<div class='err'>").append(esc(err)).append("</div>");
        }

        if (subjects.isEmpty()) {
            sb.append("<p>No subjects yet.</p>");
        } else {
            sb.append("<table><tr><th>ID</th><th>Name</th><th>Description</th><th>Actions</th></tr>");
            for (Subject s : subjects) {
                sb.append("<tr><td>").append(s.getSubjectId()).append("</td><td>")
                        .append(esc(s.getSubjectName())).append("</td><td>")
                        .append(esc(s.getDescription())).append("</td><td>")
                        .append("<a class='btn small' href='/admin/subjects?edit=")
                        .append(s.getSubjectId()).append("'>Edit</a> ")
                        .append("<form class='inline' method='post' action='/admin/subjects' ")
                        .append("onsubmit=\"return confirm('Delete this subject and its exams?')\">")
                        .append("<input type='hidden' name='action' value='delete'>")
                        .append("<input type='hidden' name='id' value='").append(s.getSubjectId()).append("'>")
                        .append("<button class='small red' type='submit'>Delete</button></form>")
                        .append("</td></tr>");
            }
            sb.append("</table>");
        }

        if (editing != null) {
            sb.append("<h3>Edit subject</h3>")
                    .append("<form method='post' action='/admin/subjects'>")
                    .append("<input type='hidden' name='action' value='update'>")
                    .append("<input type='hidden' name='id' value='").append(editing.getSubjectId()).append("'>")
                    .append("Name<input name='name' value='").append(esc(editing.getSubjectName())).append("'>")
                    .append("Description<input name='description' value='")
                    .append(esc(editing.getDescription())).append("'>")
                    .append("<button type='submit'>Save changes</button> ")
                    .append("<a href='/admin/subjects'>Cancel</a></form>");
        } else {
            sb.append("<h3>Add subject</h3>")
                    .append("<form method='post' action='/admin/subjects'>")
                    .append("<input type='hidden' name='action' value='add'>")
                    .append("Name<input name='name'>")
                    .append("Description<input name='description'>")
                    .append("<button type='submit'>Add subject</button></form>");
        }

        sb.append("<p><a href='/admin/dashboard'>Back to dashboard</a></p>");
        html(resp, "Subjects", sb.toString());
    }

    // CREATE, UPDATE, DELETE
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        String action = req.getParameter("action");
        String name = req.getParameter("name") == null ? "" : req.getParameter("name").trim();
        String description = req.getParameter("description") == null ? "" : req.getParameter("description").trim();

        int id = -1;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException | NullPointerException e) {
            // id stays -1 for the "add" action
        }

        String result;
        if ("add".equals(action)) {
            if (name.isEmpty()) {
                result = "err=Subject name cannot be empty.";
            } else if (subjectDAO.addSubject(new Subject(name, description))) {
                result = "msg=Subject added.";
            } else {
                result = "err=Could not add subject (the name may already exist).";
            }
        } else if ("update".equals(action)) {
            if (name.isEmpty() || id < 0) {
                result = "err=Subject name cannot be empty.";
            } else if (subjectDAO.updateSubject(id, name, description)) {
                result = "msg=Subject updated.";
            } else {
                result = "err=Update failed.";
            }
        } else if ("delete".equals(action)) {
            result = subjectDAO.deleteSubject(id) ? "msg=Subject deleted." : "err=Delete failed.";
        } else {
            result = "err=Unknown action.";
        }

        resp.sendRedirect("/admin/subjects?" + result.replace(" ", "%20"));
    }
}