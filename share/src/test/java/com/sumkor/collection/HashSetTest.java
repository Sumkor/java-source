package com.sumkor.collection;

import org.junit.Test;

import java.util.HashSet;

/**
 * @author Sumkor
 * @since 2021/5/26
 */
public class HashSetTest {

    /**
     * 假设 hashCode 相等，equals 相等的两个对象，放入 HashSet 第二个不会覆盖第一个。
     * 因为 HashSet 底层是 HashMap，其 key 完全相等时只会造成 value 覆盖，不会出现 key 的覆盖。
     *
     * 假设 hashCode 相等，equals 不相等的两个对象，放入 HashSet 会出现 hash 冲突，两个对象均会保留。
     */
    @Test
    public void add() {
        HashSet<Student> set = new HashSet<>();

        Student student01 = new Student("haha");
        student01.setSex("man");
        System.out.println("student01 = " + student01);
        set.add(student01);

        Student student02 = new Student("haha");
        student02.setSex("woman");
        System.out.println("student02 = " + student02);
        set.add(student02);

        System.out.println(student01.equals(student02));

        System.out.println("set = " + set);

        /**
         * student01 = haha man
         * student02 = haha woman
         * true
         * set = [haha man]
         */
    }

    class Student {

        String name;

        String sex;

        public Student(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        /**
         * 只使用 name 属性
         */
        @Override
        public int hashCode() {
            return name.hashCode();
        }

        /**
         * 只使用 name 属性
         */
        @Override
        public boolean equals(Object anObject) {
            if (this == anObject) {
                return true;
            }
            if (anObject instanceof Student) {
                Student student = (Student) anObject;
                return name.equals(student.getName());
            }
            return false;
        }

        @Override
        public String toString() {
            return name + " " + sex;
        }
    }
}
