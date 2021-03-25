import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Класс, отвечающий за пользовательский интерфейс клиента TODO: Переделать на мой собственный интерфейс
 */
public class ClientGUI extends JFrame {
    /** Основные элементы пользовательского интерфейса */
    private final JPanel chatPanel = new JPanel();
    private final JPanel loginPanel = new JPanel();
    private final JTextArea textArea = new JTextArea();
    private final JTextArea clientsInformation = new JTextArea();
    private final JLabel textInputLabel = new JLabel("Сообщение: ");
    private final JTextField textInput = new JTextField();
    private final JScrollPane scrollPaneChat = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private final JScrollPane scrollPaneClients = new JScrollPane(clientsInformation, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    private final JButton sendButton = new JButton("Отправить");

    // Далее идут элементы для приватных сообщений, чтобы
    // можно было выбрать, кому отправить сообщение

    /** Логин выбранного получателя */
    private String receiver;

    /** Константа, содержащая пункт списка "Всем" */
    private final String all = "всем";

    /** Массив строк, который будет отображаться в раскрывающемся списке */
    private String[] clients = {all};

    /** Сам раскрывающийся список */
    private final JComboBox<String> selectClient = new JComboBox<>(clients);


    /** Обработчик нажатия клавиши enter или кнопки submit */
    private final ActionListener listener = event -> {

        // получаем введенный текст
        String message = textInput.getText();

        // если input не пустой то отправляем сообщение
        if (!message.isEmpty()) {
            if (receiver != null && !receiver.equalsIgnoreCase(all)) {

                // если выбран конечный получатель, то добавляем /w и имя получателя к сообщению
                message = "/w " + receiver + " " + message;
            }

            // Отправляем сообщение с помощью объекта clientNetwork
            this.clientNetwork.sendMessage(message);

            // Сбрасываем элементы управления
            textInput.setText("");
            selectClient.setSelectedItem(all);
            receiver = null;
        }
    };

    /** Объект, находящийся на стороне клиента,
     * с которым будет взаимодействовать пользовательский интерфейс,
     * предназначенный для взаимодействия с сервером */
    private final ClientNetwork clientNetwork = new ClientNetwork();

    /** Логин текущего пользователя для формирования имени файла с историей */
    private String login;

    /** Путь к файлу с историей */
    private Path historyPath;

    /**
     * Конструктор
     */
    public ClientGUI() {
        // имплементируем callback методы Callback-ov
        setCallBacks();

        // имплементируем основное окно
        setMainFrame();

        // инициализируем панель для чата
        initializeChatPanel();

        // инициализируем панель для авторизации
        initializeLoginJPanel();

        // запускаем подключение к серверу
        this.clientNetwork.connect();
        this.setVisible(true);
    }

    /**
     * Инициализирует основное окно
     */
    private void setMainFrame() {
        this.setSize(500, 500);
        this.setTitle("Сетевой чат");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(new FlowLayout());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {

                // при закрытии окна отправляем серверу сообщение об отключении
                clientNetwork.sendMessage("/end");
                super.windowClosing(event);
            }
        });
    }

    /**
     * Инициализирует панель для чата
     */
    private void initializeChatPanel() {
        textArea.setEditable(false);
        clientsInformation.setEditable(false);
        chatPanel.setBackground(Color.white);
        chatPanel.setPreferredSize(new Dimension(490, 490));
        scrollPaneChat.setPreferredSize(new Dimension(450, 350));

        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BorderLayout());
        scrollPaneClients.setPreferredSize(new Dimension(345, 35));
        JButton changeNickNameButton = new JButton("Сменить ник");
        changeNickNameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = JOptionPane.showInputDialog("Введите новый ник");

                if (!result.equals("")){
                    clientNetwork.sendMessage("/changenick " + result);
                }
            }
        });

        upperPanel.add(scrollPaneClients);
        upperPanel.add(changeNickNameButton, BorderLayout.EAST);

        chatPanel.add(upperPanel);
        chatPanel.add(scrollPaneChat);
        textInput.setPreferredSize(new Dimension(150, 25));
        chatPanel.add(textInputLabel);
        chatPanel.add(textInput);
        textInput.addActionListener(listener);
        sendButton.addActionListener(listener);

        // Обработчик выбора клиента из раскрывающегося списка selectClient
        selectClient.addActionListener(e -> {
            // клиент, которому необходимо послать сообщение, записывается в поле receiver
            receiver = selectClient.getSelectedItem().toString();
        });
        JLabel toWho = new JLabel("Кому:");
        chatPanel.add(toWho);
        chatPanel.add(selectClient);
        chatPanel.add(sendButton);
        chatPanel.setVisible(false);
        this.add(chatPanel);
    }

    /**
     * Инициализирует панель авторизации
     */
    private void initializeLoginJPanel() {
        loginPanel.setBackground(Color.white);
        loginPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        loginPanel.setPreferredSize(new Dimension(300, 150));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Авторизация"));
        JTextField loginTextField = new JTextField();
        JLabel loginLabel = new JLabel("Ваше имя пользователя: ");    // TODO: Переделать интерфейс
        JLabel passwordLabel = new JLabel("Ваш пароль: ");
        JPasswordField passwordTextField = new JPasswordField();
        loginTextField.setPreferredSize(new Dimension(100, 25));
        passwordTextField.setPreferredSize(new Dimension(100, 25));
        loginPanel.add(loginLabel);
        loginPanel.add(loginTextField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordTextField);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(280, 35));
        buttonPanel.setBackground(Color.WHITE);
        JButton button = new JButton("OK");
        buttonPanel.add(button);
        button.addActionListener(e -> {
            // При нажатии кнопки "Submit" программа отправляет на сервер сообщение формата /auth login password
            login = loginTextField.getText();
            clientNetwork.sendMessage("/auth " + login + " " + String.valueOf(passwordTextField.getPassword()));

            // Сбрасываем данные логина и пароля
            loginTextField.setText("");
            passwordTextField.setText("");
        });
        loginPanel.add(buttonPanel);
        loginPanel.setVisible(true);
        this.add(loginPanel);
    }

    /**
     * Имплементирует нужные коллбэки
     */
    private void setCallBacks() {
        // при получении сообщения от сервера добавляем его в textArea
        // и записываем в файл
        this.clientNetwork.setCallOnMsgRecieved(message -> {
            textArea.append(message + "\n");
            writeToFile(message + "\n");
        });

        // при получении нового списка клиентов
        this.clientNetwork.setCallOnChangeClientList(clientsList -> {

            // печатаем всех клиентов в окошке клиентов
            clientsInformation.setText(clientsList);

            // создаем массив присутствующих клиентов + варинт "все"
            clients = (all + " "+ clientsList).split("\\s");

            // устанавливаем этот массив комбобоксу
            selectClient.setModel(new DefaultComboBoxModel(clients));// передаем данные combobox
        });

        // при успешной авторизации мы прячем loginPanel и делаем видимой chatPanel,
        // а также загружаем историю сообщений
        this.clientNetwork.setCallOnAuth(s -> {
            loginPanel.setVisible(false);
            chatPanel.setVisible(true);

            initializeHistory();
        });

        // при сообщении об ошибке показываем pop-up
        this.clientNetwork.setCallOnError(message -> JOptionPane.showMessageDialog(null, message, "Ошибка",
                JOptionPane.ERROR_MESSAGE));
    }

    private void initializeHistory(){
        File directory = new File("history");
        if (!directory.exists()){
            directory.mkdirs();
        }

        String path = "history" + File.separator + login.toLowerCase() + "2021.txt";
        File file = new File(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        historyPath = Paths.get(path);

        readHistoryFromFile();
    }

    private void writeToFile(String text){
        try {
            Files.write(historyPath, text.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHistoryFromFile(){
        try {
            java.util.List<String> lines = Files.readAllLines(historyPath);

            int startIndex = 0;
            int countLines = 100;

            if (lines.size() > countLines){
                startIndex = lines.size() - countLines;
            }

            for (int i = startIndex; i < lines.size(); i++) {
                textArea.append(lines.get(i) + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}