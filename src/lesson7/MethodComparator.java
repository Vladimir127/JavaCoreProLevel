package lesson7;

import lesson7.annotations.Test;

import java.lang.reflect.Method;
import java.util.Comparator;

public class MethodComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Method method1 = (Method) o1;
        Method method2 = (Method) o2;

        if(method1.isAnnotationPresent(Test.class) && method2.isAnnotationPresent(Test.class)){
            Test testAnnotation1 = method1.getAnnotation(Test.class);
            Test testAnnotation2 = method2.getAnnotation(Test.class);

            return testAnnotation1.priority() - testAnnotation2.priority();
        }

        throw new RuntimeException("Попытка некорректного сравнения методов");
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
