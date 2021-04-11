import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler {
    private String nickname;

    private Socket socket;

    private DataInputStream in;
    private DataOutputStream out;

    private Server server;

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;

            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            logger.log(Level.INFO, "Создали ClientHandler");

            server.getClientsExecutorService().submit(() -> {

                boolean continueChat = true;
                String msg = "";
                boolean isAuthorized = false;
                try {
                    long start = System.currentTimeMillis();

                    while (!isAuthorized && continueChat) {
                        if ((System.currentTimeMillis() - start) > 120000) {
                            continueChat = false;

                            sendMessage("/error timeout");

                            logger.log(Level.WARNING, "Время ожидания авторизации истекло");
                        }

                        else if (in.available() > 0) {
                            msg = in.readUTF();

                            if (msg.startsWith("/auth")) {
                                String[] tokens = msg.split("\\s");

                                nickname = server.getAuthService().getNicknameByLoginAndPassword(tokens[1], tokens[2]);

                                if (nickname != null) {
                                    isAuthorized = true;
                                    sendMessage("/authok");
                                    server.subscribe(this);

                                    logger.log(Level.INFO, "Клиент " + nickname + " успешно авторизовался");
                                }

                                else {
                                    sendMessage("/error authorization");

                                    logger.log(Level.WARNING, "Не удалось авторизовать клиента " + tokens[1]);
                                }
                            }

                            if (msg.equalsIgnoreCase("/end")) {
                                continueChat = false;

                                logger.log(Level.INFO, "Клиент вышел из чата");
                            }
                        }
                    }

                    while (continueChat) {
                        msg = in.readUTF();

                        if (msg.startsWith("/")) {

                            if (msg.startsWith("/changenick")){
                                String[] tokens = msg.split("\\s", 2);
                                String oldNickname = nickname;
                                server.getAuthService().changeNickname(nickname, tokens[1]);
                                nickname = tokens[1];
                                server.broadcastClientsList();

                                logger.log(Level.INFO, "Клиент " + oldNickname + " сменил ник на " + tokens[1]);
                            }

                            if (msg.equalsIgnoreCase("/end")) {
                                continueChat = false;

                                logger.log(Level.INFO, "Клиент " + nickname + " вышел из чата");
                            }

                            else if (msg.startsWith("/w")) {

                                String[] tokens = msg.split("\\s", 3);
                                if (tokens.length == 3) {
                                    server.privateMsg(this, tokens[1], tokens[2]);

                                    logger.log(Level.INFO, "Клиент " + nickname + " отправил приватное сообщение клиенту " + tokens[1]);
                                }
                            }
                        }

                        else {
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

    public void disconnect() {
        sendMessage("/end");
        System.out.println("disconnected " + nickname);

        server.unsubscribe(this);
        try {
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

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