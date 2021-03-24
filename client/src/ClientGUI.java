import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    private final JScrollPane js = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private final JScrollPane jsClients = new JScrollPane(clientsInformation, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
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

    /**
     * Конструктор
     */
    public ClientGUI() {
        // имплементируем callback методы Callback-ov
        setCallBacks();

        // имплементируем основное окно
        setMainFrame();

        // инициализируем панель для чата
        initilizeChatPanel();

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
    private void initilizeChatPanel() {
        textArea.setEditable(false);
        clientsInformation.setEditable(false);
        chatPanel.setBackground(Color.white);
        chatPanel.setPreferredSize(new Dimension(490, 490));
        js.setPreferredSize(new Dimension(450, 350));
        jsClients.setPreferredSize(new Dimension(450, 35));
        chatPanel.add(jsClients);
        chatPanel.add(js);
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
        JTextField login = new JTextField();
        JLabel loginLabel = new JLabel("Ваше имя пользователя: ");    // TODO: Переделать интерфейс
        JLabel passwordLabel = new JLabel("Ваш пароль: ");
        JPasswordField password = new JPasswordField();
        login.setPreferredSize(new Dimension(100, 25));
        password.setPreferredSize(new Dimension(100, 25));
        loginPanel.add(loginLabel);
        loginPanel.add(login);
        loginPanel.add(passwordLabel);
        loginPanel.add(password);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(280, 35));
        buttonPanel.setBackground(Color.WHITE);
        JButton button = new JButton("OK");
        buttonPanel.add(button);
        button.addActionListener(e -> {
            // При нажатии кнопки "Submit" программа отправляет на сервер сообщение формата /auth login password
            clientNetwork.sendMessage("/auth " + login.getText() + " " + String.valueOf(password.getPassword()));

            // Сбрасываем данные логина и пароля
            login.setText("");
            password.setText("");
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
        this.clientNetwork.setCallOnMsgRecieved(message -> textArea.append(message + "\n"));

        // при получении нового списка клиентов
        this.clientNetwork.setCallOnChangeClientList(clientsList -> {

            // печатаем всех клиентов в окошке клиентов
            clientsInformation.setText(clientsList);

            // создаем массив присутствующих клиентов + варинт "все"
            clients = (all + " "+ clientsList).split("\\s");

            // устанавливаем этот массив комбобоксу
            selectClient.setModel(new DefaultComboBoxModel(clients));// передаем данные combobox
        });

        // при успешной авторизации мы прячем loginPanel и делаем видимой chatPanel
        this.clientNetwork.setCallOnAuth(s -> {
            loginPanel.setVisible(false);
            chatPanel.setVisible(true);
        });

        // при сообщении об ошибке показываем pop-up
        this.clientNetwork.setCallOnError(message -> JOptionPane.showMessageDialog(null, message, "Ошибка",
                JOptionPane.ERROR_MESSAGE));
    }
}