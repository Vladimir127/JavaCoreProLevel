import java.sql.*;

public class DBAuthService implements AuthService {
    private static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/dbtest";
    private static final String DB_USER="postgres";
    private static final String DB_PASSWORD="postgres";

    private static Connection connection;

    private static PreparedStatement findByLoginAndPassword;
    private static PreparedStatement changeNickname;

    private static DBAuthService instance;

    private DBAuthService() { }

    public static DBAuthService getInstance(){
        if (instance == null){
            openConnection();
            createPreparedStatements();

            instance = new DBAuthService();
        }

        return instance;
    }

    private static void openConnection() {
        try {
            connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void createPreparedStatements(){
        try {
            findByLoginAndPassword = connection.prepareStatement("SELECT * FROM chat_users WHERE lower(login) = LOWER(?) AND pass = ?");
            changeNickname = connection.prepareStatement("UPDATE chat_users SET nickname = ? WHERE nickname = ?");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        ResultSet resultSet = null;

        try {
            findByLoginAndPassword.setString(1, login);
            findByLoginAndPassword.setString(2, password);

            resultSet = findByLoginAndPassword.executeQuery();

            if (resultSet.next()){
                return resultSet.getString("nickname");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closeResultSet(resultSet);
        }

        return null;
    }

    private void closeResultSet(ResultSet resultSet) {
        if (resultSet == null){
            try {
                resultSet.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public int changeNickname(String oldNickname, String newNickname) {
        try {
            changeNickname.setString(1, newNickname);
            changeNickname.setString(2, oldNickname);
            return changeNickname.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }

    @Override
    public void close(){
        try {
            connection.close();
            findByLoginAndPassword.close();
            changeNickname.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
