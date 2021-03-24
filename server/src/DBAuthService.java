import java.sql.*;

/**
 * Сервис для авторизации с помощью базы данных
 */
public class DBAuthService implements AuthService {
    private static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/dbtest";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

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
        try (
                PreparedStatement stm = connection
                        .prepareStatement("SELECT * FROM chat_users WHERE login='" + login + "' AND pass='" + password + "'");

                ResultSet resultSet = stm.executeQuery()) {

            if (resultSet.next()) {
                return login;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }
}