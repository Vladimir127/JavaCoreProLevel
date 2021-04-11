public interface AuthService {
    String getNicknameByLoginAndPassword(String login, String password);

    int changeNickname(String oldNickname, String newNickname);

    void close();
}
