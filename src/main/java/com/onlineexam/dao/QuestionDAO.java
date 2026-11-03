package com.onlineexam.dao;

import com.onlineexam.model.Question;
import com.onlineexam.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    // CREATE
    public boolean addQuestion(Question q) {
        String sql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, q.getExamId());
            ps.setString(2, q.getQuestionText());
            ps.setString(3, q.getOptionA());
            ps.setString(4, q.getOptionB());
            ps.setString(5, q.getOptionC());
            ps.setString(6, q.getOptionD());
            ps.setString(7, q.getCorrectOption());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Add question failed: " + e.getMessage());
            return false;
        }
    }

    // READ
    public List<Question> getQuestionsByExam(int examId) {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE exam_id = ? ORDER BY question_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setQuestionId(rs.getInt("question_id"));
                    q.setExamId(rs.getInt("exam_id"));
                    q.setQuestionText(rs.getString("question_text"));
                    q.setOptionA(rs.getString("option_a"));
                    q.setOptionB(rs.getString("option_b"));
                    q.setOptionC(rs.getString("option_c"));
                    q.setOptionD(rs.getString("option_d"));
                    q.setCorrectOption(rs.getString("correct_option"));
                    list.add(q);
                }
            }
        } catch (SQLException e) {
            System.out.println("Fetch questions failed: " + e.getMessage());
        }
        return list;
    }

    // UPDATE
    public boolean updateQuestion(Question q) {
        String sql = "UPDATE questions SET question_text = ?, option_a = ?, option_b = ?, option_c = ?, "
                + "option_d = ?, correct_option = ? WHERE question_id = ? AND exam_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, q.getQuestionText());
            ps.setString(2, q.getOptionA());
            ps.setString(3, q.getOptionB());
            ps.setString(4, q.getOptionC());
            ps.setString(5, q.getOptionD());
            ps.setString(6, q.getCorrectOption());
            ps.setInt(7, q.getQuestionId());
            ps.setInt(8, q.getExamId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Update question failed: " + e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean deleteQuestion(int questionId, int examId) {
        String sql = "DELETE FROM questions WHERE question_id = ? AND exam_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            ps.setInt(2, examId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Delete question failed: " + e.getMessage());
            return false;
        }
    }
}