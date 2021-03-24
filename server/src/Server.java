package main.java;

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
        // Инициализируем список клиентов
        this.clients = new ArrayList<>();

        // Создаём серверный сокет и ждём подключения клиентов
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Server is listening on 8189");

            // Поскольку количество клиентов не ограничено, цикл бесконечный
            while (true) {
                // Получили сокет, приняв клиента
                Socket socket = serverSocket.accept();

                // На основе этого сокета создаём для клиента объект ClientHandler
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
        // Создаём StringBuilder, указывая размер строки - длину имени (15) умножаем на количество клиентов.
        StringBuilder sb = new StringBuilder(15 * clients.size());

        // Добавляем в начало строки служебное слово
        sb.append("/clients ");

        // После этого добавляем через пробел ник каждого клиента и формируем строку
        for (ClientHandler o : clients) {
            sb.append(o.getNickname()).append(" ");
        }
        String out = sb.toString();

        // Рассылаем сообщение с помощью обычного метода broadcastMessage()
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

        // Если получатель равен отправителю, в начало сообщения ставим
        // не ник отправителя, а словосочетание "Пометка для себя"
        if (sender.getNickname().equals(receiverNick)) {
            sender.sendMessage("Заметка для меня: " + msg);
            return;
        }

        // Выполняем поиск клиента с нужным ником в коллекции клиентов
        for (ClientHandler receiver : clients) {
            if (receiver.getNickname().equals(receiverNick)) {
                // Если клиент с нужным именем нашёлся, отправляем ему сообщение, в котором указываем, от кого оно,
                receiver.sendMessage("from " + sender.getNickname() + ": " + msg);

                // А отправителю отправляем то же сообщение, в котором указано, для кого оно (чтобы оно сохранилось в истории)
                sender.sendMessage("for " + receiverNick + ": " + msg);
                return;
            }
        }

        // Если клиент с нужным именем не нашёлся, отправителю отправляется соответствующее сообщение
        sender.sendMessage("Client " + receiverNick + " is not found");
    }
}