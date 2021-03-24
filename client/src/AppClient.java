package main.java;

import javax.swing.*;

/**
 * Запускалка для клиента
 */
public class AppClient {
    public static void main(String[] args) {
        // В отдельном потоке, предназначенном для пользовательского интерфейса, создаём и открываем клиентское окно
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }
}
