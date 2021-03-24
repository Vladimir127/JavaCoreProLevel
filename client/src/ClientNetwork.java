package main.java;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Класс, отвечающий за взаимодействие клиента и сервера, симметричный классу ClientHandler на стороне сервера
 */
public class ClientNetwork {
    /** Сокет для взаимодействия клиента с сервером */
    private Socket socket;

    /** Потоки ввода и вывода для взаимодействия клиента с сервером */
    private DataInputStream in;
    private DataOutputStream out;

    /** Коллбэки для обновления пользовательского интерфейса, когда с сервера поступают какие-то изменения */
    private Callback<String> callOnMsgRecieved;
    private Callback<String> callOnChangeClientList;
    private Callback<String> callOnAuth;
    private Callback<String> callOnError;

    /**
     * Соединяется с сервером. Вызывается из пользовательского интерфеса (ClientGUI) после его инициализации
     */
    public void connect() {
        try {
            // Инициализируем сокет и потоки
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Отдельный поток для чтения сообщений от сервера
            new Thread(() -> {
                boolean goOn = true;
                boolean isAuthorized = false;
                try {

                    // цикл авторизации
                    while (!isAuthorized && goOn) {

                        // читаем сообщения сервера
                        String message = in.readUTF();

                        // если от сервера пришло сообщение, что авторизация прошла успешно, то
                        if (message.startsWith("/authok")) {

                            // вызываем метод Callback-a авторизации, который имплиментирован ClientGUI, чтобы закрыть
                            // окно авторизации и открыть окно чата
                            callOnAuth.callback("/authok");

                            // после этого можно выйти из цикла
                            isAuthorized = true;
                        } else if (message.equalsIgnoreCase("/end")) {

                            //если сервер написал end, выходим из обоих циклов и отключаемся
                            goOn = false;
                        } else if (message.startsWith("/error")){

                            // Разбиваем сообщение на слова, разделяя их пробелами. Это необходимо,
                            // поскольку у нас теперь имеются два вида ошибок
                            String[] tokens = message.split("\\s");

                            // Если "код" ошибки - авторизация, вызываем второй вид коллбэка, который выводит на экран
                            // соответствующее сообщение об ошибке
                            if (tokens[1].equalsIgnoreCase("authorization")) {
                                callOnError.callback("Неверное имя пользователя или пароль");
                            }

                            // Если код ошибки - таймаут, то в этом случае не только отображаем сообщение об ошибке,
                            // но и прерываем цикл авторизации
                            else if (tokens[1].equalsIgnoreCase("timeout")){
                                callOnError.callback("Время ожидания авторизации истекло");
                                goOn = false;
                            }
                        }
                    }

                    // Цикл для отправки сообщений
                    while (goOn) {

                        // читаем сообщение сервера
                        String msg = in.readUTF();

                        // Если сообщение начинается со служебного слова /end, выходим из цикла
                        if (msg.equalsIgnoreCase("/end")) {
                            goOn = false;
                        }

                        // Если сервер прислал список клиентов
                        // Вызываем третий вид коллбэка, который отобразит этот список в специальном поле.
                        // В качестве параметра отправляем список клиентов, отсекая /clients
                        else if (msg.startsWith("/clients ")) {
                            callOnChangeClientList.callback(msg.substring(9));
                        }

                        // при получении обычного сообщения без префиксов вызываем четвёртый вид коллбэка,
                        // который добавит сообщение к истории сообщений
                        else {
                            callOnMsgRecieved.callback(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start(); // Запускаем поток
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправляет сообщение на сервер
     * @param msg Сообщение
     * @return Истина, если сообщение успешно отправлено, иначе - ложь
     */
    public boolean sendMessage(String msg) {
        try {
            // Записываем сообщение в выходной поток
            out.writeUTF(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Закрывает соединение с сервером
     */
    public void closeConnection() {
        // закончили работать, все выключаем и сокеты и потоки ввода-вывода
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

    /** Сеттеры, с помощью которых мы в ClientGUI можем задать желаемое поведение */
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