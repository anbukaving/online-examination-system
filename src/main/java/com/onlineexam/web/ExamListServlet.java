package com.onlineexam.web;

import com.onlineexam.dao.ExamDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class ExamListServlet extends BaseServlet {

    private final ExamDAO examDAO = new ExamDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Student student = currentStudent(req);
        if (student == null) {
            resp.sendRedirect("/login");
            return;
        }

        List<Exam> exams = examDAO.getAllExams();
        StringBuilder sb = new StringBuilder("<style>.box{max-width:760px}"
                + "table{width:100%;border-collapse:collapse}"
                + "th,td{border-bottom:1px solid #ddd;padding:8px;text-align:left}</style>");
        sb.append("<h2>Available Exams</h2>");
        if (exams.isEmpty()) {
            sb.append("<p>No exams available.</p>");
        } else {
            sb.append("<table><tr><th>Title</th><th>Minutes</th><th>Total marks</th><th></th></tr>");
            for (Exam e : exams) {
                sb.append("<tr><td>").append(esc(e.getExamTitle())).append("</td><td>")
                        .append(e.getDurationMinutes()).append("</td><td>")
                        .append(e.getTotalMarks()).append("</td><td>")
                        .append("<a class='btn' href='/exam?id=").append(e.getExamId())
                        .append("'>Start</a></td></tr>");
            }
            sb.append("</table>");
        }
        sb.append("<p><a href='/dashboard'>Back to dashboard</a></p>");
        html(resp, "Exams", sb.toString());
    }
}