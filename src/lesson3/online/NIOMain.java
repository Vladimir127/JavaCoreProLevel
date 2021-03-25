package lesson3.online;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class NIOMain {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get("folder/b/c/cat1.txt");

        List<String> lines = Files.readAllLines(path);

        new String(Files.readAllBytes(path));

        Files.write(path, "Hello!".getBytes());
    }
}
