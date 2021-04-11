import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientNetwork {
    private Socket socket;

    private DataInputStream in;
    private DataOutputStream out;

    private Callback<String> callOnMsgRecieved;
    private Callback<String> callOnChangeClientList;
    private Callback<String> callOnAuth;
    private Callback<String> callOnError;

    public void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                boolean goOn = true;
                boolean isAuthorized = false;
                try {

                    while (!isAuthorized && goOn) {

                        String message = in.readUTF();

                        if (message.startsWith("/authok")) {
                            callOnAuth.callback("/authok");
                            isAuthorized = true;
                        } else if (message.equalsIgnoreCase("/end")) {
                            goOn = false;
                        } else if (message.startsWith("/error")){
                            String[] tokens = message.split("\\s");

                            if (tokens[1].equalsIgnoreCase("authorization")) {
                                callOnError.callback("Неверное имя пользователя или пароль");
                            }

                            else if (tokens[1].equalsIgnoreCase("timeout")){
                                callOnError.callback("Время ожидания авторизации истекло");
                                goOn = false;
                            }
                        }
                    }

                    while (goOn) {

                        String msg = in.readUTF();

                        if (msg.equalsIgnoreCase("/end")) {
                            goOn = false;
                        }

                        else if (msg.startsWith("/clients ")) {
                            callOnChangeClientList.callback(msg.substring(9));
                        }

                        else {
                            callOnMsgRecieved.callback(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void closeConnection() {
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

    public void setCallOnAuth(Callback<String> callOnAuth) {
        this.callOnAuth = callOnAuth;
    }

    public void setCallOnMsgRecieved(Callback<String> callOnMsgRecieved) {
        this.callOnMsgRecieved = callOnMsgRecieved;
    }

    public void setCallOnChangeClientList(Callback<String> callOnChangeClientList) {
        this.callOnChangeClientList = callOnChangeClientList;
    }

    public void setCallOnError(Callback<String> callOnError) {
        this.callOnError = callOnError;
    }
}