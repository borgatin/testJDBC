package ru.innopolis.borg.jdbc;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * Created by avborg on 26.10.2016.
 */
public class Main {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your PostgerSQL JDBC Driver?");
            e.printStackTrace();
            return;
        }
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5434/test"
                , "postgres", "1Qwerty");
        try(Scanner sc = new Scanner(System.in)) {
            String commandNumber = "";
            while (!"0".equals(commandNumber)) {
                System.out.println("*********************************************");
                System.out.println("Выберите действие в программе");
                System.out.println("0 - Выход");
                System.out.println("1 - Добавление студента");
                System.out.println("2 - Добавление урока");
                System.out.println("3 - Запись студента на урок");
                System.out.println("4 - Получить список всех студентов");
                System.out.println("5 - Получить список всех занятий");
                System.out.println("6 - Получить студентов, посетивших определенное занятие");
                System.out.println("7 - Получить список студентов-прогульщиков");
                System.out.print("command>");
                commandNumber = sc.nextLine();


                switch (commandNumber) {
                    case "1": {//Добавление студента
                        addStudent(connection, sc);
                        break;
                    }
                    case "2": {//Добавление урока
                        addLesson(connection, sc);
                        break;

                    }
                    case "3": {//Запись студента на урок
                        recordStudentToLesson(connection, sc);
                        break;
                    }
                    case "4": {//Получить список всех студентов
                        getAllStudents(connection);
                        break;

                    }
                    case "5": {//Получить список всех занятий
                        getAllLessons(connection);
                        break;
                    }
                    case "6": { //Список студентов, посетивших определенное занятие
                        getStudentsByLessonID(connection, sc);
                        break;
                    }
                    case "7": {//Список прогульщиков
                        getStudentsWithoutLessons(connection);
                        break;

                    }
                }
                System.out.println("");

            }
        }finally {
            connection.close();
        }
    }

    private static void getStudentsWithoutLessons(Connection connection) throws SQLException {
        try(Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("Select lastname, firstname from users where id not in(Select u.id from users u inner join userlesson ul on u.id=ul.user_id) ")) {
            System.out.println("Cписок студентов на уроке id=: ");
            System.out.println(new StringBuilder("Lastname")
                    .append("\t")
                    .append("Firstname"));
            while (rs.next()) {
                System.out.println(rs.getString("lastname") +
                        "\t" +
                        "\t" +
                        rs.getString("firstname"));
            }
        }
    }

    private static void getStudentsByLessonID(Connection connection, Scanner sc) throws SQLException {
        System.out.print("Введите id урока: ");
        int lessonID = Integer.parseInt(sc.nextLine());
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("Select u.lastname, u.firstname from users u left join userlesson ul on u.id=ul.user_id where ul.lesson_id=" + lessonID))
        {System.out.println("Cписок студентов на уроке id=" + lessonID + ": ");
            System.out.println(new StringBuilder("Lastname")
                    .append("\t")
                    .append("Firstname"));
            while (rs.next()) {
                System.out.println(rs.getString("lastname") +
                        "\t" +
                        "\t" +
                        rs.getString("firstname"));
            }
        }
    }

    private static void getAllLessons(Connection connection) throws SQLException {
        try(PreparedStatement ps2 =
                connection.prepareStatement("Select id,topic,lesson_date  from Lesson");
        ResultSet rs = ps2.executeQuery()) {
            System.out.println("Общий список уроков: ");
            System.out.println(new StringBuilder("ID")
                    .append("\t")
                    .append("Date")
                    .append("\t")
                    .append("\t")
                    .append("Topic"));
            while (rs.next()) {
                System.out.println(rs.getInt("id") +
                        "\t" +
                        rs.getDate("lesson_date") +
                        "\t" +
                        rs.getString("topic"));
            }
        }
    }

    private static void getAllStudents(Connection connection) throws SQLException {
        try(PreparedStatement ps2 =
                connection.prepareStatement("Select id,Lastname,Firstname  from Users");
        ResultSet rs = ps2.executeQuery()) {
            System.out.println("Общий список студентов: ");
            System.out.println(new StringBuilder("ID")
                    .append("\t")
                    .append("Lastname")
                    .append("\t")
                    .append("Firstname"));
            while (rs.next()) {
                int UserID = rs.getInt("id");
                System.out.println(UserID + "\t" +
                        rs.getString("lastname") +
                        "\t" +
                        "\t" +
                        rs.getString("firstname"));
            }
        }
    }

    private static void recordStudentToLesson(Connection connection, Scanner sc) throws SQLException {
        System.out.print("Введите ID студента: ");
        String student_id = sc.nextLine();
        System.out.print("Введите ID урока: ");
        String lesson_id = sc.nextLine();

        try(PreparedStatement ps =
                connection.prepareStatement("insert into UserLesson (user_id, lesson_id) VALUES ( ?,?)")) {
            ps.setInt(1, Integer.parseInt(student_id));
            ps.setInt(2, Integer.parseInt(lesson_id));
            ps.executeUpdate();
            System.out.println("Студент с id=" + student_id + " записан на урок с id=" + lesson_id);
        }
    }

    private static void addLesson(Connection connection, Scanner sc) throws SQLException {
        System.out.print("Введите тему урока: ");
        String topic = sc.nextLine();
        boolean dateIsCorrect = false;
        java.util.Date date = new Date(0);
        while (!dateIsCorrect) {
            System.out.print("Введите дату урока (dd.mm.yyyy): ");
            String topicDate = sc.nextLine();
            SimpleDateFormat format1 = new SimpleDateFormat();
            format1.applyPattern("dd.mm.yyyy");
            try {

                date = format1.parse(topicDate);
                dateIsCorrect = true;
            } catch (ParseException e) {
                dateIsCorrect = false;
                System.out.println("Ошибка преобразования строки в дату. Повторите ввод.");
            }
        }
        Date sqlDate = new Date(date.getTime());

        try(PreparedStatement ps =
                connection.prepareStatement("insert into Lesson (topic, lesson_date) VALUES ( ?,?)")) {
            ps.setString(1, topic);
            ps.setDate(2, sqlDate);
            ps.executeUpdate();
            try(PreparedStatement ps2 =
                    connection.prepareStatement("Select id from Lesson where topic=? and lesson_date=?")) {
                ps2.setString(1, topic);
                ps2.setDate(2, sqlDate);
                try(ResultSet rs = ps2.executeQuery()) {
                    if (rs.next()) {
                        int lessonID = rs.getInt("id");
                        System.out.println("Создан урок " + topic + " " + date + " с id = " + lessonID);
                    }
                }
            }
        }
    }

    private static void addStudent(Connection connection, Scanner sc) throws SQLException {
        System.out.print("Введите имя студента: ");
        String lastname = sc.nextLine();
        System.out.print("Введите фамилию студента: ");
        String firstname = sc.nextLine();

        try(PreparedStatement ps =
                connection.prepareStatement("insert into users (lastname, firstname) VALUES ( ?,?)")) {
            ps.setString(1, lastname);
            ps.setString(2, firstname);
            ps.executeUpdate();
            try (PreparedStatement ps2 =
                         connection.prepareStatement("Select id from Users where lastname=? and firstname=?")) {
                ps2.setString(1, lastname);
                ps2.setString(2, firstname);
                ResultSet rs = ps2.executeQuery();
                if (rs.next()) {
                    int userID = rs.getInt("id");
                    System.out.println("Создан пользователь " + lastname + " " + firstname + " с id = " + userID);
                }
            }
        }
    }
}
