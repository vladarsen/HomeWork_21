package com.vladarsenjtev;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestRunner {

    public static void start(Class cls) {

        Object object = initObject(cls);

        List<Method> beforeSuiteMethod;
        if ((beforeSuiteMethod = findMethod(cls, BeforeSuite.class)).size() > 1) {
            throw new RuntimeException(String.format("[%s] object [%s] method is not uniquely instantiated",
                    cls.getName(), BeforeSuite.class.getName()));
        } else if (!beforeSuiteMethod.isEmpty()) {
            executeListMethods(beforeSuiteMethod, object);
        }

        List<Method> testMethods;
        if ((testMethods = findMethod(cls, Test.class)).isEmpty()) {
            System.out.printf("Object [%s] has no test methods.", cls.getName());
            return;
        } else {
            Collections.sort(testMethods, (o1, o2) -> o1.getAnnotation(Test.class).order() - o2.getAnnotation(Test.class).order());
            executeListMethods(testMethods, object);
        }

        List<Method> afterSuiteMethod;
        if ((afterSuiteMethod = findMethod(cls, AfterSuite.class)).size() > 1) {
            throw new RuntimeException(String.format("[%s] object [%s] method is not uniquely instantiated",
                    cls.getName(), AfterSuite.class.getName()));
        } else if (!afterSuiteMethod.isEmpty()) {
            executeListMethods(afterSuiteMethod, object);
        }


    }

    private static void executeListMethods(List<Method> methodList, Object object, Object... args) {
        for (Method testM : methodList) {
            executeMethod(testM, object, args);
        }
    }

    private static void executeMethod(Method method, Object object, Object... args) {
        try {
            method.setAccessible(true);
            method.invoke(object, args);
            method.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Method> findMethod(Class cls,
                                           Class<? extends Annotation> annotation) {
        List<Method> methodList = new ArrayList<>();
        for (Method m : cls.getDeclaredMethods()
        ) {
            if (m.isAnnotationPresent(annotation)) {
                methodList.add(m);
            }
        }
        return methodList;
    }

    private static Object initObject(Class cls) {
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
