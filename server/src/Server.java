import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private List<ClientHandler> clients;

    private AuthService authService = DBAuthService.getInstance();

    private ExecutorService clientsExecutorService;

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public AuthService getAuthService() {
        return authService;
    }

    public ExecutorService getClientsExecutorService() {
        return clientsExecutorService;
    }

    public Server() {
        this.clients = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            logger.log(Level.INFO, "Сервер запущен. Порт: 8189");

            clientsExecutorService = Executors.newCachedThreadPool();

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            authService.close();
            clientsExecutorService.shutdown();
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder(15 * clients.size());
        sb.append("/clients ");

        for (ClientHandler o : clients) {
            sb.append(o.getNickname()).append(" ");
        }

        String out = sb.toString();
        broadcastMessage(out);
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientsList();
    }

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