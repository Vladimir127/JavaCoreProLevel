package lesson3.online;

import java.io.*;

public class IOMain {
    public static void main(String[] args) {
        try (InputStream is = new FileInputStream("folder/b/cat.txt")) {
            int symbol;
            while ((symbol = is.read()) != -1) {
                System.out.println((char) symbol);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = new BufferedInputStream(
                new FileInputStream("folder/b/cat.txt"))) {
            int symbol;
            while ((symbol = is.read()) != -1) {
                System.out.println((char) symbol);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = new FileInputStream("folder/b/cat.txt")) {
            byte[] bytes = new byte[5];

            int countBytes;
            while ((countBytes = is.read(bytes)) > 0) {
                System.out.println(new String(bytes, 0, countBytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (OutputStream os = new BufferedOutputStream(
                new FileOutputStream("folder/b/cat1.txt"))) {

            String message = "Hello !!!!";
            os.write(message.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                new FileOutputStream("folder/b/cat.txt"))) {

            //objectOutputStream.writeObject(cat);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                new FileInputStream("folder/b/cat1.txt"))) {

            //Cat cat1 = (Cat) objectInputStream.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
