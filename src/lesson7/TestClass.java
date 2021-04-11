package lesson7;

import lesson7.annotations.AfterSuit;
import lesson7.annotations.BeforeSuit;
import lesson7.annotations.Test;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, который может выполнять тесты
 */
public class TestClass {

    public static void main(String[] args) {
        start("lesson7.test.java.home.example.homework.CalculatorTest");
    }

    /**
     * Статический метод для запуска тестов
     * @param className Имя класса
     */
    public static void start(String className) {
        try {
            Class<?> aClass = Class.forName(className);
            start(aClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Статический метод для запуска тестов
     * @param aClass Класс с тестовыми методами, который нужно выполнить
     */
    public static void start(Class aClass) {

        List<Method> methodsWithAnnotations = new ArrayList<>();

        Method beforeSuitMethod = null;
        Method[] methods = aClass.getDeclaredMethods();
        Method afterSuitMethod = null;

        for (Method method : methods){
            if (method.isAnnotationPresent(Test.class)){
                methodsWithAnnotations.add(method);
            }

            if (method.isAnnotationPresent(BeforeSuit.class)){
                if (beforeSuitMethod != null){
                    throw new RuntimeException("Дублирование метода с аннотацией @BeforeSuit");
                }
                beforeSuitMethod = method;
            }

            if (method.isAnnotationPresent(AfterSuit.class)){
                if (afterSuitMethod != null){
                    throw new RuntimeException("Дублирование метода с аннотацией @AfterSuit");
                }
                afterSuitMethod = method;
            }
        }

        MethodComparator methodComparator = new MethodComparator();
        methodsWithAnnotations.sort(methodComparator);

        if (beforeSuitMethod != null) {
            methodsWithAnnotations.add(0, beforeSuitMethod);
        }

        if (afterSuitMethod != null) {
            methodsWithAnnotations.add(methodsWithAnnotations.size(), afterSuitMethod);
        }

        try {
            Object obj = aClass.getConstructor().newInstance();
            for (Method method : methodsWithAnnotations){
                method.invoke(obj, null);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
