package com.onlineexam.dao;

import com.onlineexam.model.Exam;
import com.onlineexam.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// POLYMORPHISM: ExamDAO is a CrudDAO<Exam>
public class ExamDAO implements CrudDAO<Exam> {

    @Override
    public boolean add(Exam e) {
        String sql = "INSERT INTO exams (exam_title, subject_id, duration_minutes, total_marks, marks_per_question) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getExamTitle());
            ps.setInt(2, e.getSubjectId());
            ps.setInt(3, e.getDurationMinutes());
            ps.setInt(4, e.getTotalMarks());
            ps.setInt(5, e.getMarksPerQuestion());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("Add exam failed: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public List<Exam> getAll() {
        return fetch("SELECT * FROM exams ORDER BY exam_id", 0);
    }

    // METHOD OVERLOADING: same name, different parameter. Exams of one subject only.
    public List<Exam> getAll(int subjectId) {
        return fetch("SELECT * FROM exams WHERE subject_id = ? ORDER BY exam_id", subjectId);
    }

    @Override
    public boolean update(Exam e) {
        String sql = "UPDATE exams SET exam_title = ?, subject_id = ?, duration_minutes = ?, "
                + "total_marks = ?, marks_per_question = ? WHERE exam_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getExamTitle());
            ps.setInt(2, e.getSubjectId());
            ps.setInt(3, e.getDurationMinutes());
            ps.setInt(4, e.getTotalMarks());
            ps.setInt(5, e.getMarksPerQuestion());
            ps.setInt(6, e.getExamId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("Update exam failed: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM exams WHERE exam_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("Delete exam failed: " + ex.getMessage());
            return false;
        }
    }

    // Shared helper: runs a SELECT. If subjectId is 0 there is no parameter.
    private List<Exam> fetch(String sql, int subjectId) {
        List<Exam> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (subjectId > 0) {
                ps.setInt(1, subjectId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Exam e = new Exam();
                    e.setExamId(rs.getInt("exam_id"));
                    e.setExamTitle(rs.getString("exam_title"));
                    e.setSubjectId(rs.getInt("subject_id"));
                    e.setDurationMinutes(rs.getInt("duration_minutes"));
                    e.setTotalMarks(rs.getInt("total_marks"));
                    e.setMarksPerQuestion(rs.getInt("marks_per_question"));
                    list.add(e);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Fetch exams failed: " + ex.getMessage());
        }
        return list;
    }

    // ---- Old method names, kept so Main.java still works ----

    public boolean addExam(Exam e) {
        return add(e);
    }

    public List<Exam> getAllExams() {
        return getAll();
    }
}