package com.onlineexam.web;

import com.onlineexam.dao.ExamDAO;
import com.onlineexam.dao.QuestionDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.Question;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class AdminQuestionsServlet extends AdminBaseServlet {

    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();

    private Exam findExam(int id) {
        for (Exam e : examDAO.getAllExams()) {
            if (e.getExamId() == id) {
                return e;
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        int examId = intParam(req, "exam");
        Exam exam = findExam(examId);
        if (exam == null) {
            resp.sendRedirect("/admin/exams");
            return;
        }

        List<Question> questions = questionDAO.getQuestionsByExam(examId);
        Question editing = null;
        int editId = intParam(req, "edit");
        for (Question q : questions) {
            if (q.getQuestionId() == editId) {
                editing = q;
            }
        }

        StringBuilder sb = new StringBuilder(wideStyle());
        sb.append("<h2>Questions: ").append(esc(exam.getExamTitle())).append("</h2>").append(notice(req));
        sb.append("<p>Total questions: <b>").append(questions.size()).append("</b></p>");

        if (questions.isEmpty()) {
            sb.append("<p>No questions yet.</p>");
        } else {
            sb.append("<table><tr><th>ID</th><th>Question</th><th>Options</th><th>Correct</th>"
                    + "<th>Actions</th></tr>");
            for (Question q : questions) {
                sb.append("<tr><td>").append(q.getQuestionId()).append("</td><td>")
                        .append(esc(q.getQuestionText())).append("</td><td>")
                        .append("A) ").append(esc(q.getOptionA())).append("<br>")
                        .append("B) ").append(esc(q.getOptionB())).append("<br>")
                        .append("C) ").append(esc(q.getOptionC())).append("<br>")
                        .append("D) ").append(esc(q.getOptionD())).append("</td><td>")
                        .append(q.getCorrectOption()).append("</td><td>")
                        .append("<a class='btn small' href='/admin/questions?exam=").append(examId)
                        .append("&edit=").append(q.getQuestionId()).append("'>Edit</a> ")
                        .append("<form class='inline' method='post' action='/admin/questions' ")
                        .append("onsubmit=\"return confirm('Delete this question?')\">")
                        .append("<input type='hidden' name='action' value='delete'>")
                        .append("<input type='hidden' name='exam' value='").append(examId).append("'>")
                        .append("<input type='hidden' name='id' value='").append(q.getQuestionId()).append("'>")
                        .append("<button class='small red' type='submit'>Delete</button></form>")
                        .append("</td></tr>");
            }
            sb.append("</table>");
        }

        sb.append(form(editing, examId));
        sb.append("<p><a href='/admin/exams'>Back to exams</a></p>");
        html(resp, "Questions", sb.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        int examId = intParam(req, "exam");
        String page = "/admin/questions?exam=" + examId;
        if (findExam(examId) == null) {
            resp.sendRedirect("/admin/exams");
            return;
        }

        String action = param(req, "action");
        int id = intParam(req, "id");

        if ("delete".equals(action)) {
            boolean ok = questionDAO.deleteQuestion(id, examId);
            back(resp, page, ok, ok ? "Question deleted." : "Delete failed.");
            return;
        }

        String text = param(req, "text");
        String a = param(req, "a");
        String b = param(req, "b");
        String c = param(req, "c");
        String d = param(req, "d");
        String correct = param(req, "correct").toUpperCase();

        if (text.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
            back(resp, page, false, "Question and all four options are required.");
            return;
        }
        if (!correct.matches("[ABCD]")) {
            back(resp, page, false, "Correct option must be A, B, C or D.");
            return;
        }

        Question q = new Question();
        q.setExamId(examId);
        q.setQuestionText(text);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectOption(correct);

        if ("add".equals(action)) {
            boolean ok = questionDAO.addQuestion(q);
            back(resp, page, ok, ok ? "Question added." : "Could not add question.");
        } else if ("update".equals(action)) {
            q.setQuestionId(id);
            boolean ok = questionDAO.updateQuestion(q);
            back(resp, page, ok, ok ? "Question updated." : "Update failed.");
        } else {
            back(resp, page, false, "Unknown action.");
        }
    }

    // One form used for both "add" and "edit"
    private String form(Question q, int examId) {
        boolean edit = q != null;
        StringBuilder sb = new StringBuilder();
        sb.append(edit ? "<h3>Edit question</h3>" : "<h3>Add question</h3>");
        sb.append("<form method='post' action='/admin/questions'>");
        sb.append("<input type='hidden' name='action' value='").append(edit ? "update" : "add").append("'>");
        sb.append("<input type='hidden' name='exam' value='").append(examId).append("'>");
        if (edit) {
            sb.append("<input type='hidden' name='id' value='").append(q.getQuestionId()).append("'>");
        }
        sb.append("Question<textarea name='text' rows='3'>")
                .append(edit ? esc(q.getQuestionText()) : "").append("</textarea>");
        sb.append("Option A<input name='a' value='").append(edit ? esc(q.getOptionA()) : "").append("'>");
        sb.append("Option B<input name='b' value='").append(edit ? esc(q.getOptionB()) : "").append("'>");
        sb.append("Option C<input name='c' value='").append(edit ? esc(q.getOptionC()) : "").append("'>");
        sb.append("Option D<input name='d' value='").append(edit ? esc(q.getOptionD()) : "").append("'>");
        sb.append("Correct option<select name='correct'>");
        for (String letter : new String[]{"A", "B", "C", "D"}) {
            boolean selected = edit && letter.equals(q.getCorrectOption());
            sb.append("<option value='").append(letter).append("'")
                    .append(selected ? " selected" : "").append(">").append(letter).append("</option>");
        }
        sb.append("</select>");
        sb.append("<button type='submit'>").append(edit ? "Save changes" : "Add question").append("</button>");
        if (edit) {
            sb.append(" <a href='/admin/questions?exam=").append(examId).append("'>Cancel</a>");
        }
        sb.append("</form>");
        return sb.toString();
    }
}