package com.onlineexam;

import com.onlineexam.dao.AdminDAO;
import com.onlineexam.dao.ExamDAO;
import com.onlineexam.dao.QuestionDAO;
import com.onlineexam.dao.ResultDAO;
import com.onlineexam.dao.StudentDAO;
import com.onlineexam.dao.SubjectDAO;
import com.onlineexam.model.Admin;
import com.onlineexam.model.Exam;
import com.onlineexam.model.Question;
import com.onlineexam.model.Result;
import com.onlineexam.model.Student;
import com.onlineexam.model.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final StudentDAO studentDAO = new StudentDAO();
    private static final AdminDAO adminDAO = new AdminDAO();
    private static final SubjectDAO subjectDAO = new SubjectDAO();
    private static final ExamDAO examDAO = new ExamDAO();
    private static final QuestionDAO questionDAO = new QuestionDAO();
    private static final ResultDAO resultDAO = new ResultDAO();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n===== ONLINE EXAMINATION SYSTEM =====");
            System.out.println("1. Student Register");
            System.out.println("2. Student Login");
            System.out.println("3. Admin Login");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> registerStudent();
                case "2" -> studentLogin();
                case "3" -> adminLogin();
                case "4" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ================= STUDENT =================

    private static void registerStudent() {
        System.out.println("\n--- Student Registration ---");
        System.out.print("Full name: ");
        String name = sc.nextLine().trim();
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Phone (10 digits, optional): ");
        String phone = sc.nextLine().trim();
        System.out.print("Password (min 6 characters): ");
        String password = sc.nextLine().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("Name, email and password are required.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            System.out.println("Invalid email.");
            return;
        }
        if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
            System.out.println("Phone must be exactly 10 digits.");
            return;
        }
        if (password.length() < 6) {
            System.out.println("Password must be at least 6 characters.");
            return;
        }
        if (studentDAO.emailExists(email)) {
            System.out.println("This email is already registered.");
            return;
        }

        boolean saved = studentDAO.register(new Student(name, email, password, phone));
        System.out.println(saved ? "Registration successful! Please log in." : "Registration failed.");
    }

    private static void studentLogin() {
        System.out.println("\n--- Student Login ---");
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        Student student = studentDAO.login(email, password);
        if (student == null) {
            System.out.println("Invalid email or password.");
            return;
        }
        System.out.println("Login successful! Welcome, " + student.getFullName() + "!");
        studentMenu(student);
    }

    private static void studentMenu(Student student) {
        while (true) {
            System.out.println("\n--- Student Menu (" + student.getFullName() + ") ---");
            System.out.println("1. View available exams");
            System.out.println("2. Take an exam");
            System.out.println("3. My previous results");
            System.out.println("4. View / update my profile");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> viewExams();
                case "2" -> takeExam(student);
                case "3" -> viewMyResults(student);
                case "4" -> profile(student);
                case "5" -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void profile(Student student) {
        System.out.println("\n--- My Profile ---");
        student.displayInfo();   // polymorphism: Student's own displayInfo()
        System.out.println("   Phone: " + student.getPhone());
        System.out.print("Update profile? (yes/no): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("yes")) return;

        System.out.print("New full name: ");
        String name = sc.nextLine().trim();
        System.out.print("New phone (10 digits, optional): ");
        String phone = sc.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
            System.out.println("Phone must be exactly 10 digits.");
            return;
        }
        if (studentDAO.updateProfile(student.getStudentId(), name, phone)) {
            student.setFullName(name);
            student.setPhone(phone);
            System.out.println("Profile updated.");
        } else {
            System.out.println("Update failed.");
        }
    }

    private static void takeExam(Student student) {
        if (examDAO.getAllExams().isEmpty()) {
            System.out.println("No exams available.");
            return;
        }
        viewExams();
        int examId = readInt("Enter exam ID to take: ");
        if (examId < 0) return;

        Exam exam = findExam(examId);
        if (exam == null) {
            System.out.println("No exam with that ID.");
            return;
        }

        List<Question> questions = questionDAO.getQuestionsByExam(examId);
        if (questions.isEmpty()) {
            System.out.println("This exam has no questions yet.");
            return;
        }

        System.out.println("\n=== " + exam.getExamTitle() + " ===");
        System.out.println("Questions: " + questions.size() + " | Time: " + exam.getDurationMinutes() + " minutes");
        System.out.println("Type A, B, C or D to answer. Type S to skip a question.");
        System.out.print("Press Enter to start...");
        sc.nextLine();

        long endTime = System.currentTimeMillis() + exam.getDurationMinutes() * 60L * 1000L;
        List<String> chosen = new ArrayList<>();
        int correct = 0, incorrect = 0, unanswered = 0;

        for (int i = 0; i < questions.size(); i++) {
            if (System.currentTimeMillis() >= endTime) {
                System.out.println("\n*** TIME IS UP! Submitting your exam automatically. ***");
                for (int j = i; j < questions.size(); j++) {
                    chosen.add(null);
                    unanswered++;
                }
                break;
            }

            Question q = questions.get(i);
            long secondsLeft = (endTime - System.currentTimeMillis()) / 1000;
            System.out.println("\nQuestion " + (i + 1) + " of " + questions.size()
                    + "   [Time left: " + (secondsLeft / 60) + "m " + (secondsLeft % 60) + "s]");
            System.out.println(q.getQuestionText());
            System.out.println("  A) " + q.getOptionA());
            System.out.println("  B) " + q.getOptionB());
            System.out.println("  C) " + q.getOptionC());
            System.out.println("  D) " + q.getOptionD());

            String answer;
            while (true) {
                System.out.print("Your answer (A/B/C/D, or S to skip): ");
                answer = sc.nextLine().trim().toUpperCase();
                if (answer.matches("[ABCDS]")) break;
                System.out.println("Please type A, B, C, D or S.");
            }

            if (answer.equals("S")) {
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

        System.out.println("\n===== RESULT =====");
        System.out.println("Exam        : " + exam.getExamTitle());
        System.out.println("Correct     : " + correct);
        System.out.println("Incorrect   : " + incorrect);
        System.out.println("Unanswered  : " + unanswered);
        System.out.println("Marks       : " + marks + " / " + possible);
        System.out.println("Percentage  : " + percentage + "%");

        boolean saved = resultDAO.saveResult(student.getStudentId(), examId, correct, incorrect,
                unanswered, marks, percentage, questions, chosen);
        System.out.println(saved ? "Your result has been saved." : "Could not save your result.");
    }

    private static void viewMyResults(Student student) {
        List<Result> results = resultDAO.getResultsByStudent(student.getStudentId());
        if (results.isEmpty()) {
            System.out.println("You have not taken any exam yet.");
            return;
        }
        System.out.println("\nExam                 | Right | Wrong | Skipped | Marks | Percent | Date");
        System.out.println("---------------------+-------+-------+---------+-------+---------+--------------------");
        for (Result r : results) {
            System.out.printf("%-20s | %-5d | %-5d | %-7d | %-5d | %-7.2f | %s%n",
                    r.getExamTitle(), r.getCorrectCount(), r.getIncorrectCount(),
                    r.getUnansweredCount(), r.getMarksObtained(), r.getPercentage(), r.getAttemptedOn());
        }
    }

    // ================= ADMIN =================

    private static void adminLogin() {
        System.out.println("\n--- Admin Login ---");
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        Admin admin = adminDAO.login(username, password);
        if (admin == null) {
            System.out.println("Invalid username or password.");
            return;
        }
        adminMenu(admin);
    }

    private static void adminMenu(Admin admin) {
        while (true) {
            System.out.println("\n--- Admin Menu (" + admin.getUsername() + ") ---");
            System.out.println("1. Manage subjects");
            System.out.println("2. Manage exams");
            System.out.println("3. Manage questions");
            System.out.println("4. Manage students");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> subjectMenu();
                case "2" -> examMenu();
                case "3" -> questionMenu();
                case "4" -> studentAdminMenu();
                case "5" -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ---------------- SUBJECTS ----------------

    private static void subjectMenu() {
        while (true) {
            System.out.println("\n--- Manage Subjects ---");
            System.out.println("1. Add subject");
            System.out.println("2. View all subjects");
            System.out.println("3. Update subject");
            System.out.println("4. Delete subject");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> addSubject();
                case "2" -> viewSubjects();
                case "3" -> updateSubject();
                case "4" -> deleteSubject();
                case "5" -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void addSubject() {
        System.out.print("Subject name: ");
        String name = sc.nextLine().trim();
        System.out.print("Description: ");
        String description = sc.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Subject name cannot be empty.");
            return;
        }
        boolean saved = subjectDAO.addSubject(new Subject(name, description));
        System.out.println(saved ? "Subject added." : "Could not add subject (name may already exist).");
    }

    private static void viewSubjects() {
        List<Subject> subjects = subjectDAO.getAllSubjects();
        if (subjects.isEmpty()) {
            System.out.println("No subjects found.");
            return;
        }
        System.out.println("\nID   | Name                 | Description");
        System.out.println("-----+----------------------+------------------------");
        for (Subject s : subjects) {
            System.out.printf("%-4d | %-20s | %s%n", s.getSubjectId(), s.getSubjectName(), s.getDescription());
        }
    }

    private static void updateSubject() {
        viewSubjects();
        int id = readInt("Enter subject ID to update: ");
        if (id < 0) return;
        System.out.print("New name: ");
        String name = sc.nextLine().trim();
        System.out.print("New description: ");
        String description = sc.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Subject name cannot be empty.");
            return;
        }
        boolean ok = subjectDAO.updateSubject(id, name, description);
        System.out.println(ok ? "Subject updated." : "Update failed (check the ID).");
    }

    private static void deleteSubject() {
        viewSubjects();
        int id = readInt("Enter subject ID to delete: ");
        if (id < 0) return;
        System.out.print("This also deletes its exams and questions. Type YES to confirm: ");
        if (!sc.nextLine().trim().equals("YES")) {
            System.out.println("Cancelled.");
            return;
        }
        boolean ok = subjectDAO.deleteSubject(id);
        System.out.println(ok ? "Subject deleted." : "Delete failed (check the ID).");
    }

    // ---------------- EXAMS ----------------

    private static void examMenu() {
        while (true) {
            System.out.println("\n--- Manage Exams ---");
            System.out.println("1. Add exam");
            System.out.println("2. View all exams");
            System.out.println("3. Update exam");
            System.out.println("4. Delete exam");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> addExam();
                case "2" -> viewExams();
                case "3" -> updateExam();
                case "4" -> deleteExam();
                case "5" -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void addExam() {
        viewSubjects();
        int subjectId = readInt("Subject ID for this exam: ");
        if (subjectId < 0) return;

        System.out.print("Exam title: ");
        String title = sc.nextLine().trim();
        int duration = readInt("Duration in minutes: ");
        int totalMarks = readInt("Total marks: ");
        int perQuestion = readInt("Marks per question: ");

        if (title.isEmpty() || duration <= 0 || totalMarks <= 0 || perQuestion <= 0) {
            System.out.println("Title is required and numbers must be positive.");
            return;
        }

        Exam e = new Exam();
        e.setExamTitle(title);
        e.setSubjectId(subjectId);
        e.setDurationMinutes(duration);
        e.setTotalMarks(totalMarks);
        e.setMarksPerQuestion(perQuestion);
        System.out.println(examDAO.addExam(e) ? "Exam added." : "Could not add exam (check the subject ID).");
    }

    private static void viewExams() {
        List<Exam> exams = examDAO.getAllExams();
        if (exams.isEmpty()) {
            System.out.println("No exams found.");
            return;
        }
        System.out.println("\nID   | Title                | Subject ID | Mins | Total | Per Q");
        System.out.println("-----+----------------------+------------+------+-------+------");
        for (Exam e : exams) {
            System.out.printf("%-4d | %-20s | %-10d | %-4d | %-5d | %d%n",
                    e.getExamId(), e.getExamTitle(), e.getSubjectId(),
                    e.getDurationMinutes(), e.getTotalMarks(), e.getMarksPerQuestion());
        }
    }

    private static void updateExam() {
        viewExams();
        int id = readInt("Enter exam ID to update: ");
        if (id < 0) return;
        Exam e = findExam(id);
        if (e == null) {
            System.out.println("No exam with that ID.");
            return;
        }

        System.out.print("New title: ");
        String title = sc.nextLine().trim();
        int duration = readInt("New duration in minutes: ");
        int totalMarks = readInt("New total marks: ");
        int perQuestion = readInt("New marks per question: ");

        if (title.isEmpty() || duration <= 0 || totalMarks <= 0 || perQuestion <= 0) {
            System.out.println("Invalid details. Nothing changed.");
            return;
        }
        e.setExamTitle(title);
        e.setDurationMinutes(duration);
        e.setTotalMarks(totalMarks);
        e.setMarksPerQuestion(perQuestion);
        System.out.println(examDAO.update(e) ? "Exam updated." : "Update failed.");
    }

    private static void deleteExam() {
        viewExams();
        int id = readInt("Enter exam ID to delete: ");
        if (id < 0) return;
        System.out.print("This also deletes its questions and results. Type YES to confirm: ");
        if (!sc.nextLine().trim().equals("YES")) {
            System.out.println("Cancelled.");
            return;
        }
        System.out.println(examDAO.delete(id) ? "Exam deleted." : "Delete failed (check the ID).");
    }

    private static Exam findExam(int examId) {
        for (Exam e : examDAO.getAllExams()) {
            if (e.getExamId() == examId) {
                return e;
            }
        }
        return null;
    }

    // ---------------- QUESTIONS ----------------

    private static void questionMenu() {
        viewExams();
        int examId = readInt("Enter exam ID to manage its questions: ");
        if (examId < 0) return;
        if (findExam(examId) == null) {
            System.out.println("No exam with that ID.");
            return;
        }

        while (true) {
            System.out.println("\n--- Manage Questions (Exam ID " + examId + ") ---");
            System.out.println("1. Add question");
            System.out.println("2. View questions");
            System.out.println("3. Update question");
            System.out.println("4. Delete question");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> addQuestion(examId);
                case "2" -> viewQuestions(examId);
                case "3" -> updateQuestion(examId);
                case "4" -> deleteQuestion(examId);
                case "5" -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // Asks for the question details. Returns null if something is invalid.
    private static Question askQuestion(int examId) {
        System.out.print("Question: ");
        String text = sc.nextLine().trim();
        System.out.print("Option A: ");
        String a = sc.nextLine().trim();
        System.out.print("Option B: ");
        String b = sc.nextLine().trim();
        System.out.print("Option C: ");
        String c = sc.nextLine().trim();
        System.out.print("Option D: ");
        String d = sc.nextLine().trim();
        System.out.print("Correct option (A/B/C/D): ");
        String correct = sc.nextLine().trim().toUpperCase();

        if (text.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
            System.out.println("Question and all four options are required.");
            return null;
        }
        if (!correct.matches("[ABCD]")) {
            System.out.println("Correct option must be A, B, C or D.");
            return null;
        }

        Question q = new Question();
        q.setExamId(examId);
        q.setQuestionText(text);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectOption(correct);
        return q;
    }

    private static void addQuestion(int examId) {
        Question q = askQuestion(examId);
        if (q == null) return;
        System.out.println(questionDAO.addQuestion(q) ? "Question added." : "Could not add question.");
    }

    private static boolean viewQuestions(int examId) {
        List<Question> list = questionDAO.getQuestionsByExam(examId);
        if (list.isEmpty()) {
            System.out.println("No questions yet for this exam.");
            return false;
        }
        for (Question q : list) {
            System.out.println("\n[ID " + q.getQuestionId() + "] " + q.getQuestionText());
            System.out.println("   A) " + q.getOptionA());
            System.out.println("   B) " + q.getOptionB());
            System.out.println("   C) " + q.getOptionC());
            System.out.println("   D) " + q.getOptionD());
            System.out.println("   Correct answer: " + q.getCorrectOption());
        }
        return true;
    }

    private static void updateQuestion(int examId) {
        if (!viewQuestions(examId)) return;
        int id = readInt("Enter question ID to update: ");
        if (id < 0) return;
        Question q = askQuestion(examId);
        if (q == null) return;
        q.setQuestionId(id);
        System.out.println(questionDAO.updateQuestion(q) ? "Question updated." : "Update failed (check the ID).");
    }

    private static void deleteQuestion(int examId) {
        if (!viewQuestions(examId)) return;
        int id = readInt("Enter question ID to delete: ");
        if (id < 0) return;
        System.out.println(questionDAO.deleteQuestion(id, examId) ? "Question deleted." : "Delete failed (check the ID).");
    }

    // ---------------- STUDENTS (admin) ----------------

    private static void studentAdminMenu() {
        while (true) {
            System.out.println("\n--- Manage Students ---");
            System.out.println("1. View all students");
            System.out.println("2. Delete a student");
            System.out.println("3. Back");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> viewStudents();
                case "2" -> deleteStudent();
                case "3" -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static boolean viewStudents() {
        List<Student> students = studentDAO.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return false;
        }
        System.out.println("\nID   | Name                 | Email                | Phone");
        System.out.println("-----+----------------------+----------------------+-----------");
        for (Student s : students) {
            System.out.printf("%-4d | %-20s | %-20s | %s%n",
                    s.getStudentId(), s.getFullName(), s.getEmail(), s.getPhone());
        }
        return true;
    }

    private static void deleteStudent() {
        if (!viewStudents()) return;
        int id = readInt("Enter student ID to delete: ");
        if (id < 0) return;
        System.out.print("This also deletes the student's results. Type YES to confirm: ");
        if (!sc.nextLine().trim().equals("YES")) {
            System.out.println("Cancelled.");
            return;
        }
        System.out.println(studentDAO.deleteStudent(id) ? "Student deleted." : "Delete failed (check the ID).");
    }

    // ---------------- HELPER ----------------

    // Reads a number safely. Returns -1 if the user typed something that is not a number.
    private static int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return -1;
        }
    }
}