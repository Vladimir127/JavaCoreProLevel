package lesson6.main.java.home.example.homework;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] array = {1, 2, 4, 4, 2, 3, 4, 1, 7};
        int[] result = extractElements(array);
        System.out.println(Arrays.toString(result));

        array = new int[] {1, 1, 1, 4, 4, 1, 4, 4};
        boolean checkResult = checkArray(array);
        System.out.println("Результат проверки: " + checkResult);
    }

    /**
     * Формирует и возвращает новый массив путем вытаскивания из исходного массива элементов, идущих после последней четверки.
     * @param array Исходный массив
     * @return Сформированный массив
     */
    public static int[] extractElements(int[] array){
        for (int i = array.length - 1; i >= 0 ; i--) {
            if (array[i] == 4){
                int length = array.length - i - 1;
                int[] resultArray = new int[length];
                System.arraycopy(array, i + 1, resultArray, 0, length);
                return resultArray;
            }
        }

        throw new RuntimeException("Неверный формат массива");
    }

    /**
     * Проверяет состав массива из чисел 1 и 4
     * @param array Проверяемый массив
     * @return Истина, если массив состоит только из единиц и четвёрок.
     * Ложь, если в массиве нет хотя бы одной единицы или четвёрки, или если присутствуют другие цифры.
     */
    public static boolean checkArray(int[] array){
        boolean has1 = false;
        boolean has4 = false;

        for (int i = 0; i < array.length; i++){
            if (array[i] == 1){
                has1 = true;
            } else if (array[i] == 4){
                has4 = true;
            } else {
                return false;
            }
        }

        if (has1 && has4){
            return true;
        }

        return false;
    }
}


