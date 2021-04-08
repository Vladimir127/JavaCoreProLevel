package lesson6.test.java.home.example.homework;

import lesson6.main.java.home.example.homework.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    public static Stream<Arguments> dataForExtractElements() {
        List<Arguments> out = new ArrayList<>();

        out.add(Arguments.arguments(new int[] {1, 2, 4, 4, 2, 3, 4, 1, 7}, new int[]{1, 7}));
        out.add(Arguments.arguments(new int[] {4, 5}, new int[]{5}));
        out.add(Arguments.arguments(new int[] {4}, new int[]{}));

        return out.stream();
    }

    @ParameterizedTest
    @MethodSource("dataForExtractElements")
    void testExtractElementsCorrect(int[] array, int[] expected) {
        int[] actual = Main.extractElements(array);

        Assertions.assertArrayEquals(expected, actual);
    }

    @Test
    void testExtractElementsIncorrect() {
        int[] array = {1, 2, 3, 5, 6, 7};

        Assertions.assertThrows(RuntimeException.class, () -> Main.extractElements(array));
    }

    @Test
    void testExtractElementsEmpty() {
        int[] array = {};

        Assertions.assertThrows(RuntimeException.class, () -> Main.extractElements(array));
    }

    public static Stream<Arguments> dataForCheckArray() {
        List<Arguments> out = new ArrayList<>();

        out.add(Arguments.arguments(new int[] {1, 1, 1, 4, 4, 1, 4, 4}, true));
        out.add(Arguments.arguments(new int[] {1, 1, 1, 4, 4, 1, 4, 4}, true));
        out.add(Arguments.arguments(new int[] {1, 1, 1, 1, 1, 1}, false));
        out.add(Arguments.arguments(new int[] {1, 4, 4, 1, 1, 4, 3}, false));
        out.add(Arguments.arguments(new int[] {}, false));

        return out.stream();
    }

    @ParameterizedTest
    @MethodSource("dataForCheckArray")
    void testCheckArray(int[] array, boolean expected) {
        boolean actual = Main.checkArray(array);

        Assertions.assertEquals(expected, actual);
    }
}