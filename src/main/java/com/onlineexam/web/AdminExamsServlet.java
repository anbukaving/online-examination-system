package com.onlineexam.web;

import com.onlineexam.dao.ExamDAO;
import com.onlineexam.dao.SubjectDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.Subject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class AdminExamsServlet extends AdminBaseServlet {

    private final ExamDAO examDAO = new ExamDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        List<Exam> exams = examDAO.getAllExams();
        List<Subject> subjects = subjectDAO.getAllSubjects();

        Exam editing = null;
        int editId = intParam(req, "edit");
        for (Exam e : exams) {
            if (e.getExamId() == editId) {
                editing = e;
            }
        }

        StringBuilder sb = new StringBuilder(wideStyle());
        sb.append("<h2>Manage Exams</h2>").append(notice(req));

        if (exams.isEmpty()) {
            sb.append("<p>No exams yet.</p>");
        } else {
            sb.append("<table><tr><th>ID</th><th>Title</th><th>Subject</th><th>Mins</th>"
                    + "<th>Total</th><th>Per Q</th><th>Actions</th></tr>");
            for (Exam e : exams) {
                sb.append("<tr><td>").append(e.getExamId()).append("</td><td>")
                        .append(esc(e.getExamTitle())).append("</td><td>")
                        .append(esc(subjectName(subjects, e.getSubjectId()))).append("</td><td>")
                        .append(e.getDurationMinutes()).append("</td><td>")
                        .append(e.getTotalMarks()).append("</td><td>")
                        .append(e.getMarksPerQuestion()).append("</td><td>")
                        .append("<a class='btn small' href='/admin/questions?exam=")
                        .append(e.getExamId()).append("'>Questions</a> ")
                        .append("<a class='btn small' href='/admin/exams?edit=")
                        .append(e.getExamId()).append("'>Edit</a> ")
                        .append("<form class='inline' method='post' action='/admin/exams' ")
                        .append("onsubmit=\"return confirm('Delete this exam, its questions and results?')\">")
                        .append("<input type='hidden' name='action' value='delete'>")
                        .append("<input type='hidden' name='id' value='").append(e.getExamId()).append("'>")
                        .append("<button class='small red' type='submit'>Delete</button></form>")
                        .append("</td></tr>");
            }
            sb.append("</table>");
        }

        if (subjects.isEmpty()) {
            sb.append("<p>Add a subject first (Manage subjects).</p>");
        } else {
            sb.append(form(editing, subjects));
        }
        sb.append("<p><a href='/admin/dashboard'>Back to dashboard</a></p>");
        html(resp, "Exams", sb.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        String action = param(req, "action");
        int id = intParam(req, "id");

        if ("delete".equals(action)) {
            back(resp, "/admin/exams", examDAO.delete(id), examDAO.delete(-1) ? "" : "Exam deleted.");
            return;
        }

        String title = param(req, "title");
        int subjectId = intParam(req, "subjectId");
        int duration = intParam(req, "duration");
        int total = intParam(req, "total");
        int perQuestion = intParam(req, "perQuestion");

        if (title.isEmpty() || subjectId < 0 || duration <= 0 || total <= 0 || perQuestion <= 0) {
            back(resp, "/admin/exams", false, "Title is required and numbers must be positive.");
            return;
        }

        Exam e = new Exam();
        e.setExamTitle(title);
        e.setSubjectId(subjectId);
        e.setDurationMinutes(duration);
        e.setTotalMarks(total);
        e.setMarksPerQuestion(perQuestion);

        if ("add".equals(action)) {
            boolean ok = examDAO.add(e);
            back(resp, "/admin/exams", ok, ok ? "Exam added." : "Could not add exam.");
        } else if ("update".equals(action)) {
            e.setExamId(id);
            boolean ok = examDAO.update(e);
            back(resp, "/admin/exams", ok, ok ? "Exam updated." : "Update failed.");
        } else {
            back(resp, "/admin/exams", false, "Unknown action.");
        }
    }

    private String subjectName(List<Subject> subjects, int id) {
        for (Subject s : subjects) {
            if (s.getSubjectId() == id) {
                return s.getSubjectName();
            }
        }
        return "?";
    }

    // One form used for both "add" and "edit"
    private String form(Exam e, List<Subject> subjects) {
        boolean edit = e != null;
        StringBuilder sb = new StringBuilder();
        sb.append(edit ? "<h3>Edit exam</h3>" : "<h3>Add exam</h3>");
        sb.append("<form method='post' action='/admin/exams'>");
        sb.append("<input type='hidden' name='action' value='").append(edit ? "update" : "add").append("'>");
        if (edit) {
            sb.append("<input type='hidden' name='id' value='").append(e.getExamId()).append("'>");
        }
        sb.append("Subject<select name='subjectId'>");
        for (Subject s : subjects) {
            boolean selected = edit && s.getSubjectId() == e.getSubjectId();
            sb.append("<option value='").append(s.getSubjectId()).append("'")
                    .append(selected ? " selected" : "").append(">")
                    .append(esc(s.getSubjectName())).append("</option>");
        }
        sb.append("</select>");
        sb.append("Title<input name='title' value='").append(edit ? esc(e.getExamTitle()) : "").append("'>");
        sb.append("Duration (minutes)<input name='duration' value='")
                .append(edit ? e.getDurationMinutes() : "").append("'>");
        sb.append("Total marks<input name='total' value='")
                .append(edit ? e.getTotalMarks() : "").append("'>");
        sb.append("Marks per question<input name='perQuestion' value='")
                .append(edit ? e.getMarksPerQuestion() : "").append("'>");
        sb.append("<button type='submit'>").append(edit ? "Save changes" : "Add exam").append("</button>");
        if (edit) {
            sb.append(" <a href='/admin/exams'>Cancel</a>");
        }
        sb.append("</form>");
        return sb.toString();
    }
}