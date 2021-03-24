/** Сервис для авторизауии. Сделан в виде интерфейса, потому что могут быть разные способы авторизации */
public interface AuthService {
    /**
     * Принимает на вход логин и пароль, и возвращает ник
     * @param login Логин
     * @param password Пароль
     * @return Если пользователь найден в базе, то ник этого пользователя, а если не нвйден - null.
     */
    String getNicknameByLoginAndPassword(String login, String password);

    /**
     * Изменяет ник пользователя
     * @param oldNickname Старый ник
     * @param newNickname Новый ник
     * @return Количество затронутых строк в базе данных
     */
    int changeNickname(String oldNickname, String newNickname);

    /**
     * Закрывает соединение
     */
    void close();
}
