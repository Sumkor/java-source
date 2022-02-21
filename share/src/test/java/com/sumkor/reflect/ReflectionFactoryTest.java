package com.sumkor.reflect;

import org.junit.Test;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;

/**
 * @author Sumkor
 * @since 2022/2/21
 */
public class ReflectionFactoryTest {

    class Student {
        private String id;
        private String name;

        public Student(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    public void test() throws Exception {
        ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
        Constructor<?> constructor = reflectionFactory.newConstructorForSerialization(Student.class, Object.class.getDeclaredConstructor());
        constructor.setAccessible(true);
        Student student = (Student) constructor.newInstance();
        System.out.println(student.getId());
        System.out.println(student.getName());
        System.out.println(student.getClass());
    }
}
