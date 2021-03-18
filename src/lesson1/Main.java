package lesson1;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String[] array = {"Один", "Два", "Три", "Четыре", "Пять"};

        System.out.println("Задание 1. Замена элементов в массиве\nИсходный массив: " + Arrays.toString(array));
        int i = 2, j = 3;
        changeElements(array, i, j);
        System.out.println("Массив после замены элементов " + i + " и " + j + ": " + Arrays.toString(array));

        System.out.println("\nЗадание 2. Преобразование массива в ArrayList\nИсходный массив: " + Arrays.toString(array));
        ArrayList<String> arrayList = convertArrayToArrayList(array);
        System.out.println("Полученный ArrayList: " + arrayList.toString());
    }

    /**
     * Меняет два элемента массива местами
     * @param array Исходный массив
     * @param i Индекс первого заменяемого элемента
     * @param j Индекс второго заменяемого элемента
     * @param <T> Тип элементов массива
     */
    public static <T> void changeElements(T[] array, int i, int j){
        T element = array[i];
        array[i] = array[j];
        array[j] = element;
    }

    /**
     * Преобразует массив в ArrayList
     * @param array Исходный массив
     * @param <T> Тип элементов массива
     * @return Объект ArrayList, содержащий элементы массива
     */
    public static <T> ArrayList<T> convertArrayToArrayList(T[] array){
        ArrayList<T> result = new ArrayList<>(Arrays.asList(array));
        return result;
    }
}
