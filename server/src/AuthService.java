/** Сервис для авторизауии. Сделан в виде интерфейса, потому что могут быть разные способы авторизации */
public interface AuthService {
    /**
     * Принимает на вход логин и пароль, и возвращает логин
     * @param login Логин
     * @param password Пароль
     * @return Если пользователь найден в базе, то логин этого пользователя, а если не нвйден - null.
     */
    String getNicknameByLoginAndPassword(String login, String password);
}
