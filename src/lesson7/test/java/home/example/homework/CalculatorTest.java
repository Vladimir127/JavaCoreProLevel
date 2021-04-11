package lesson7.test.java.home.example.homework;

import lesson7.annotations.AfterSuit;
import lesson7.annotations.BeforeSuit;
import lesson7.annotations.Test;
import lesson7.main.java.home.example.homework.Calculator;
import org.junit.jupiter.api.Assertions;

/**
 * Класс с методами для тестирования класса Main
 */
public class CalculatorTest {

    private int a;
    private int b;

    @Test(priority = 1)
    public void addTest(){
        int expected = 12;
        int actual = Calculator.add(a, b);
        Assertions.assertEquals(expected, actual);
    }

    @Test(priority = 4)
    public void subtractTest(){
        int expected = 4;
        int actual = Calculator.subtract(a, b);
        Assertions.assertEquals(expected, actual);
    }

    @Test(priority = 3)
    public void multiplyTest(){
        int expected = 32;
        int actual = Calculator.multiply(a, b);
        Assertions.assertEquals(expected, actual);
    }

    @Test(priority = 2)
    public void divideTest(){
        int expected = 2;
        int actual = Calculator.divide(a, b);
        Assertions.assertEquals(expected, actual);
    }

    @BeforeSuit
    public void initialize(){
        a = 8;
        b = 4;
    }

    @AfterSuit
    public void nullify(){
        a = 0;
        b = 0;
    }
}
