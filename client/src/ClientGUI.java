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

public class ClientGUI extends JFrame {
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

    private String receiver;

    private final String all = "всем";

    private String[] clients = {all};

    private final JComboBox<String> selectClient = new JComboBox<>(clients);

    private final ActionListener listener = event -> {

        String message = textInput.getText();

        if (!message.isEmpty()) {
            if (receiver != null && !receiver.equalsIgnoreCase(all)) {

                message = "/w " + receiver + " " + message;
            }

            this.clientNetwork.sendMessage(message);

            textInput.setText("");
            selectClient.setSelectedItem(all);
            receiver = null;
        }
    };

    private final ClientNetwork clientNetwork = new ClientNetwork();

    private String login;

    private Path historyPath;

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

        selectClient.addActionListener(e -> {
            receiver = selectClient.getSelectedItem().toString();
        });
        JLabel toWho = new JLabel("Кому:");
        chatPanel.add(toWho);
        chatPanel.add(selectClient);
        chatPanel.add(sendButton);
        chatPanel.setVisible(false);
        this.add(chatPanel);
    }

    private void initializeLoginJPanel() {
        loginPanel.setBackground(Color.white);
        loginPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        loginPanel.setPreferredSize(new Dimension(300, 150));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Авторизация"));
        JTextField loginTextField = new JTextField();
        JLabel loginLabel = new JLabel("Ваше имя пользователя: ");
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
            login = loginTextField.getText();
            clientNetwork.sendMessage("/auth " + login + " " + String.valueOf(passwordTextField.getPassword()));

            loginTextField.setText("");
            passwordTextField.setText("");
        });
        loginPanel.add(buttonPanel);
        loginPanel.setVisible(true);
        this.add(loginPanel);
    }

    private void setCallBacks() {
        this.clientNetwork.setCallOnMsgRecieved(message -> {
            textArea.append(message + "\n");
            writeToFile(message + "\n");
        });

        this.clientNetwork.setCallOnChangeClientList(clientsList -> {

            clientsInformation.setText(clientsList);

            clients = (all + " "+ clientsList).split("\\s");

            selectClient.setModel(new DefaultComboBoxModel(clients));
        });

        this.clientNetwork.setCallOnAuth(s -> {
            loginPanel.setVisible(false);
            chatPanel.setVisible(true);

            initializeHistory();
        });

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