import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Обработчик клиентов - класс, который содержит информацию о клиенте, необходимую серверу для работы с этим клиентом */
public class ClientHandler {
    private String nickname;

    private Socket socket;

    private DataInputStream in;
    private DataOutputStream out;

    private Server server;

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    /**
     * Конструктор класса
     * @param server сервер
     * @param socket сокет
     */
    public ClientHandler(Server server, Socket socket) {
        try {
            // Поскольку этот обработчик работает на сервере и рассылает остальным клиентам то, что получил от одного из
            // клиентов, здесь также необходима ссылка на сам сервер. то нужно для того, чтобы при получении сообщения
            // от своего клиента этот объект обратился к серверу и попросил его разослать сообщение всем остальным клиентам.
            this.server = server;

            // Получаем сокет из входного параметра и на основании этого сокета инициализируем входящий и исходящий потоки
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            logger.log(Level.INFO, "Создали ClientHandler");

            // поток для клиента
            server.getClientsExecutorService().submit(() -> {

                boolean continueChat = true;
                String msg = "";
                boolean isAuthorized = false;
                try {
                    // Засекаем время начала авторизации
                    long start = System.currentTimeMillis();

                    // цикл авторизации
                    while (!isAuthorized && continueChat) {

                        // Последнее ДЗ (8) в телеграм
                        // Проверяем, сколько времени прошло с начала процесса авторизации
                        if ((System.currentTimeMillis() - start) > 120000) {
                            // Если больше 120 секунд, присваиваем переменной continueChat
                            // значение false, чтобы цикл прервался
                            continueChat = false;

                            // Отправляем сообщение об ошибке sendMessage("/error timeout")
                            sendMessage("/error timeout");

                            logger.log(Level.WARNING, "Время ожидания авторизации истекло");
                        }

                        else if (in.available() > 0) {
                            // читаем сообщение от клиента
                            msg = in.readUTF();

                            // Если сообщение начинается с /auth, то пытаемся его авторизировать
                            if (msg.startsWith("/auth")) {
                                // Разбиваем сообщение на слова, разделяя их пробелами. Регулярное выражение "\\s" означает,
                                // что допустимо использовать несколько пробелов или символ табуляции
                                String[] tokens = msg.split("\\s");

                                // Пытаемся получить ник пользователя, передавая в качестве параметров первый и второй (если
                                // считать с нуля) элементы массива - логин и пароль
                                nickname = server.getAuthService().getNicknameByLoginAndPassword(tokens[1], tokens[2]);

                                // Если авторизация прошла успешно, то шлем клиенту сообщение authok (чтобы у него открылась
                                // панель чата) и выходим из цикла авторизации
                                if (nickname != null) {
                                    isAuthorized = true;
                                    sendMessage("/authok");
                                    server.subscribe(this);

                                    logger.log(Level.INFO, "Клиент " + nickname + " успешно авторизовался");
                                }

                                // иначе пишем клиенту об ошибке, добавляя "код" ошибки
                                else {
                                    sendMessage("/error authorization");

                                    logger.log(Level.WARNING, "Не удалось авторизовать клиента " + tokens[1]);
                                }
                            }

                            // Если клиент решил выйти без авторизации
                            if (msg.equalsIgnoreCase("/end")) {
                                continueChat = false;

                                logger.log(Level.INFO, "Клиент вышел из чата");
                            }
                        } // это скобка от ДЗ 8
                    }

                    // Начинаем читать сообщения
                    while (continueChat) {

                        // Считываем сообщение из входного потока
                        msg = in.readUTF();

                        // Если сообщение начинается со слеша, то это команда. В этом блоке будет обработка команд
                        if (msg.startsWith("/")) {

                            if (msg.startsWith("/changenick")){
                                String[] tokens = msg.split("\\s", 2);
                                String oldNickname = nickname;
                                server.getAuthService().changeNickname(nickname, tokens[1]);
                                nickname = tokens[1];
                                server.broadcastClientsList();

                                logger.log(Level.INFO, "Клиент " + oldNickname + " сменил ник на " + tokens[1]);
                            }

                            // клиент вышел
                            if (msg.equalsIgnoreCase("/end")) {
                                continueChat = false;

                                logger.log(Level.INFO, "Клиент " + nickname + " вышел из чата");
                            }

                            // приватное сообщение ДЗ7
                            else if (msg.startsWith("/w")) {

                                // Разбиваем сообщение на массив из трёх элементов (разделитель - пробел):
                                // команда, логин получателя и само сообщение
                                String[] tokens = msg.split("\\s", 3);
                                if (tokens.length == 3) {

                                    // Отправляем приватное сообщение, вызывая метод сервера privateMsg(),
                                    // при этом в качестве отправителя передаём данный объект ClientHandler,
                                    // а в качестве получателя и сообщения - элементы массива
                                    server.privateMsg(this, tokens[1], tokens[2]);

                                    logger.log(Level.INFO, "Клиент " + nickname + " отправил приватное сообщение клиенту " + tokens[1]);
                                }
                            }
                        }

                        // Обычное сообщение
                        else {
                            // Рассылаем сообщение всем клиентам, вызывая метод broadcastMessage на сервере
                            server.broadcastMessage(nickname + ": " + msg);

                            logger.log(Level.INFO, "Клиент " + nickname + " отправил сообщение всем пользователям: " + msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправляет сообщение с сервера данному клиенту
     * @param message Сообщению
     */
    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    /**
     * Выполняет отключение клиента от сервера
     */
    public void disconnect() {
        sendMessage("/end");
        System.out.println("disconnected " + nickname);

        // Удаляемся из списка клиентов сервера
        server.unsubscribe(this);
        try {
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // По отдельности закрываем потоки, чтобы в случае возникновения ошибки
        // при закрытии одного потока можно было продолжить закрывать остальные.
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}