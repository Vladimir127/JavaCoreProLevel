import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    /** Список клиентов, подключённых к серверу */
    private List<ClientHandler> clients;

    /** Сервис авторизации через базу данных */
    private AuthService authService = new DBAuthService();

    /**
     * Возвращает сервис авторизации
     * @return Сервис авторизации
     */
    public AuthService getAuthService() {
        return authService;
    }

    /**
     * Конструктор
     */
    public Server() {
        this.clients = new ArrayList<>();

        // Создаём серверный сокет и ждём подключения клиентов
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Server is listening on 8189");

            // Поскольку количество клиентов не ограничено, цикл бесконечный
            while (true) {
                // Получили сокет, приняв клиента. На основе этого сокета создаём для клиента объект ClientHandler
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Рассылает сообщение всем клиентам
     * @param message Сообщение
     */
    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * Рассылает список имен всех клиентам
     */
    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder(15 * clients.size());
        sb.append("/clients ");

        for (ClientHandler o : clients) {
            sb.append(o.getNickname()).append(" ");
        }

        String out = sb.toString();
        broadcastMessage(out);
    }

    /**
     * Добавляет клиента в список
     * @param client Клиент
     */
    public void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientsList();
    }

    /**
     * Удаляет клиента из списка
     * @param client Клиент
     */
    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientsList();
    }

    /**
     * Отправляет личное сообщение конкретному клиенту
     * @param sender Отправитель
     * @param receiverNick Логин получателя
     * @param msg Сообщение
     */
    public void privateMsg(ClientHandler sender, String receiverNick, String msg) {

        if (sender.getNickname().equals(receiverNick)) {
            sender.sendMessage("Заметка для меня: " + msg);
            return;
        }

        for (ClientHandler receiver : clients) {
            if (receiver.getNickname().equals(receiverNick)) {
                receiver.sendMessage("От пользователя " + sender.getNickname() + ": " + msg);

                sender.sendMessage("Для пользователя " + receiverNick + ": " + msg);
                return;
            }
        }

        sender.sendMessage("Пользователь " + receiverNick + " не найден");
    }
}