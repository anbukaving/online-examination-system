package com.onlineexam.web;

import com.onlineexam.dao.ExamDAO;
import com.onlineexam.dao.QuestionDAO;
import com.onlineexam.dao.ResultDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.Question;
import com.onlineexam.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExamServlet extends BaseServlet {

    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ResultDAO resultDAO = new ResultDAO();

    private Exam findExam(int id) {
        for (Exam e : examDAO.getAllExams()) {
            if (e.getExamId() == id) {
                return e;
            }
        }
        return null;
    }

    private int readId(HttpServletRequest req) {
        try {
            return Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }

    // Shows the questions and the countdown timer
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Student student = currentStudent(req);
        if (student == null) {
            resp.sendRedirect("/login");
            return;
        }
        int examId = readId(req);
        Exam exam = findExam(examId);
        if (exam == null) {
            resp.sendRedirect("/exams");
            return;
        }
        List<Question> questions = questionDAO.getQuestionsByExam(examId);
        if (questions.isEmpty()) {
            html(resp, "Exam", "<p>This exam has no questions yet.</p><a href='/exams'>Back</a>");
            return;
        }

        // The server remembers when the exam ends (so the timer cannot be cheated)
        long end = System.currentTimeMillis() + exam.getDurationMinutes() * 60L * 1000L;
        req.getSession().setAttribute("examEnd" + examId, end);

        StringBuilder sb = new StringBuilder("<style>.box{max-width:760px}"
                + ".q{margin:18px 0;padding-bottom:10px;border-bottom:1px solid #eee}"
                + ".q label{display:block;margin:4px 0}"
                + "#timer{position:sticky;top:0;background:#fff3cd;padding:8px;font-weight:bold}"
                + ".q input{width:auto;margin:0 8px 0 0}</style>");
        sb.append("<h2>").append(esc(exam.getExamTitle())).append("</h2>");
        sb.append("<div id='timer'></div>");
        sb.append("<form id='examForm' method='post' action='/exam?id=").append(examId).append("'>");

        int n = 1;
        for (Question q : questions) {
            sb.append("<div class='q'><b>Q").append(n++).append(". ")
                    .append(esc(q.getQuestionText())).append("</b>");
            String[] letters = {"A", "B", "C", "D"};
            String[] options = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
            for (int i = 0; i < 4; i++) {
                sb.append("<label><input type='radio' name='q").append(q.getQuestionId())
                        .append("' value='").append(letters[i]).append("'>")
                        .append(letters[i]).append(") ").append(esc(options[i])).append("</label>");
            }
            sb.append("</div>");
        }
        sb.append("<button type='submit'>Submit exam</button></form>");

        // Countdown timer: submits the form automatically when time reaches zero
        sb.append("<script>var left=").append(exam.getDurationMinutes() * 60).append(";")
                .append("function tick(){var m=Math.floor(left/60),s=left%60;")
                .append("document.getElementById('timer').innerText='Time left: '+m+'m '+s+'s';")
                .append("if(left<=0){document.getElementById('examForm').submit();return;}")
                .append("left--;setTimeout(tick,1000);}tick();</script>");

        html(resp, "Exam", sb.toString());
    }

    // Marks the answers, saves the result and shows it
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Student student = currentStudent(req);
        if (student == null) {
            resp.sendRedirect("/login");
            return;
        }
        int examId = readId(req);
        Exam exam = findExam(examId);
        if (exam == null) {
            resp.sendRedirect("/exams");
            return;
        }
        List<Question> questions = questionDAO.getQuestionsByExam(examId);
        if (questions.isEmpty()) {
            resp.sendRedirect("/exams");
            return;
        }

        // If the answers arrive more than 15 seconds after the end time, they are ignored
        Object endObj = req.getSession().getAttribute("examEnd" + examId);
        boolean late = endObj == null || System.currentTimeMillis() > (Long) endObj + 15000;

        List<String> chosen = new ArrayList<>();
        int correct = 0, incorrect = 0, unanswered = 0;
        for (Question q : questions) {
            String answer = late ? null : req.getParameter("q" + q.getQuestionId());
            if (answer == null || !answer.matches("[ABCD]")) {
                chosen.add(null);
                unanswered++;
            } else {
                chosen.add(answer);
                if (answer.equals(q.getCorrectOption())) {
                    correct++;
                } else {
                    incorrect++;
                }
            }
        }

        int marks = correct * exam.getMarksPerQuestion();
        int possible = questions.size() * exam.getMarksPerQuestion();
        double percentage = Math.round(marks * 100.0 / possible * 100) / 100.0;

        boolean saved = resultDAO.saveResult(student.getStudentId(), examId, correct, incorrect,
                unanswered, marks, percentage, questions, chosen);
        req.getSession().removeAttribute("examEnd" + examId);

        String body = "<h2>Result: " + esc(exam.getExamTitle()) + "</h2>"
                + (late ? "<div class='err'>Time was over, so your answers were not counted.</div>" : "")
                + "<p>Correct: <b>" + correct + "</b></p>"
                + "<p>Incorrect: <b>" + incorrect + "</b></p>"
                + "<p>Unanswered: <b>" + unanswered + "</b></p>"
                + "<p>Marks: <b>" + marks + " / " + possible + "</b></p>"
                + "<p>Percentage: <b>" + percentage + "%</b></p>"
                + (saved ? "<div class='ok'>Your result has been saved.</div>"
                : "<div class='err'>Could not save your result.</div>")
                + "<a class='btn' href='/results'>My results</a> "
                + "<a class='btn' href='/dashboard'>Dashboard</a>";
        html(resp, "Result", body);
    }
}