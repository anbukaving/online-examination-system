package com.onlineexam.web;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class WebServer {

    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setBaseDir(new File("target/tomcat").getAbsolutePath());
        tomcat.getConnector();

        Context context = tomcat.addContext("", new File(".").getAbsolutePath());

        // Student pages
        add(context, "home", new HomeServlet(), "/");
        add(context, "register", new RegisterServlet(), "/register");
        add(context, "login", new LoginServlet(), "/login");
        add(context, "dashboard", new DashboardServlet(), "/dashboard");
        add(context, "logout", new LogoutServlet(), "/logout");
        add(context, "exams", new ExamListServlet(), "/exams");
        add(context, "exam", new ExamServlet(), "/exam");
        add(context, "results", new ResultsServlet(), "/results");

        // Admin pages
        add(context, "adminLogin", new AdminLoginServlet(), "/admin/login");
        add(context, "adminDashboard", new AdminDashboardServlet(), "/admin/dashboard");
        add(context, "adminLogout", new AdminLogoutServlet(), "/admin/logout");
        add(context, "adminSubjects", new AdminSubjectsServlet(), "/admin/subjects");
        add(context, "adminExams", new AdminExamsServlet(), "/admin/exams");
        add(context, "adminQuestions", new AdminQuestionsServlet(), "/admin/questions");
        add(context, "adminStudents", new AdminStudentsServlet(), "/admin/students");

        tomcat.start();
        System.out.println("Server started: open http://localhost:8080 in your browser");
        tomcat.getServer().await();
    }

    private static void add(Context context, String name, jakarta.servlet.http.HttpServlet servlet,
                            String path) {
        Tomcat.addServlet(context, name, servlet);
        context.addServletMappingDecoded(path, name);
    }
}