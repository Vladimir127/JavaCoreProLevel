package main.java;

import java.sql.*;

/**
 * Сервис для авторизации с помощью базы данных
 */
public class DBAuthService implements AuthService{
    /** Строка подключения, имя пользователя и пароль */
    private static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/dbtest";
    private static final String DB_USER="postgres";
    private static final String DB_PASSWORD="postgres";

    /** Объект подключения для связи с базой данных */
    private static Connection connection;

    // Получаем соединение в статическом блоке, поскольку само это поле также является статическим
    static {
        try {
            connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try(
                // Создаём подготовленное выражение, хранящее запрос с параметрами на выборку пользователей
                PreparedStatement stm = connection
                        .prepareStatement("SELECT * FROM chat_users WHERE login='"+login+"' AND pass='"+password+"'");

                // Выполняем запрос и записываем результат в объект ResultSet
                ResultSet resultSet = stm.executeQuery()){

            // Если результирующая выборка имеет запись, возвращаем логин
            if (resultSet.next()){
                return login;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        // Если такую запись найти не удалось, возвращаем null
        return null;
    }
}
