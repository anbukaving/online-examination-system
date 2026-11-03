package com.onlineexam.util;

import com.onlineexam.dao.CrudDAO;
import com.onlineexam.dao.ExamDAO;
import com.onlineexam.dao.SubjectDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.Subject;

public class CrudDemo {
    public static void main(String[] args) {
        // POLYMORPHISM: the variable type is the interface, the object is a DAO
        CrudDAO<Subject> subjectDao = new SubjectDAO();
        CrudDAO<Exam> examDao = new ExamDAO();

        System.out.println("--- Subjects ---");
        for (Subject s : subjectDao.getAll()) {
            System.out.println(s.getSubjectId() + " - " + s.getSubjectName());
        }

        System.out.println("--- All exams ---");
        for (Exam e : examDao.getAll()) {
            System.out.println(e.getExamId() + " - " + e.getExamTitle());
        }

        // OVERLOADING: getAll(int) is a second method with the same name
        System.out.println("--- Exams of subject 1 only ---");
        for (Exam e : new ExamDAO().getAll(1)) {
            System.out.println(e.getExamId() + " - " + e.getExamTitle());
        }
    }
}