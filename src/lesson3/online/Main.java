package lesson3.online;

import java.io.File;
import java.io.FilenameFilter;

public class Main {
    public static void main(String[] args) {

        // Получаем нужную папку
        File fileSrc = new File("folder/b");

        // Создаём фильтр
        FilenameFilter filter = new FilenameFilter() {

            /**
             * Применяет фильтр к текущему файлу
             * @param dir Родительская директория, в которой лежит этот файл
             * @param name Имя текущего файла
             * @return Условие, которому должно удовлетоврять имя файла
             */
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("cat");
            }
        };

        // Получаем массив файлов из этой директории с применением фильтра
        File[] files = fileSrc.listFiles(filter);

    }

}
