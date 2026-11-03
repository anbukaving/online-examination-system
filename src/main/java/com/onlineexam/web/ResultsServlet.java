package com.onlineexam.web;

import com.onlineexam.dao.ResultDAO;
import com.onlineexam.model.Result;
import com.onlineexam.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class ResultsServlet extends BaseServlet {

    private final ResultDAO resultDAO = new ResultDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Student student = currentStudent(req);
        if (student == null) {
            resp.sendRedirect("/login");
            return;
        }

        List<Result> results = resultDAO.getResultsByStudent(student.getStudentId());
        StringBuilder sb = new StringBuilder("<style>.box{max-width:860px}"
                + "table{width:100%;border-collapse:collapse}"
                + "th,td{border-bottom:1px solid #ddd;padding:8px;text-align:left}</style>");
        sb.append("<h2>My Previous Results</h2>");
        if (results.isEmpty()) {
            sb.append("<p>You have not taken any exam yet.</p>");
        } else {
            sb.append("<table><tr><th>Exam</th><th>Right</th><th>Wrong</th><th>Skipped</th>"
                    + "<th>Marks</th><th>Percent</th><th>Date</th></tr>");
            for (Result r : results) {
                sb.append("<tr><td>").append(esc(r.getExamTitle())).append("</td><td>")
                        .append(r.getCorrectCount()).append("</td><td>")
                        .append(r.getIncorrectCount()).append("</td><td>")
                        .append(r.getUnansweredCount()).append("</td><td>")
                        .append(r.getMarksObtained()).append("</td><td>")
                        .append(r.getPercentage()).append("%</td><td>")
                        .append(esc(r.getAttemptedOn())).append("</td></tr>");
            }
            sb.append("</table>");
        }
        sb.append("<p><a href='/dashboard'>Back to dashboard</a></p>");
        html(resp, "Results", sb.toString());
    }
}