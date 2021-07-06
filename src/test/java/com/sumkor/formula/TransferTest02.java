package com.sumkor.formula;

import com.sun.jmx.remote.internal.ArrayQueue;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TransferTest 的增强版，支持多位数、小数点
 *
 * @author Sumkor
 * @since 2021/7/6
 */
public class TransferTest02 {

    @Test
    public void calculateTest() {
        String str = "5-1*(5+6)+2"; // 5156+*2+-
//        String str = "1+((5*6)+2)";
        System.out.println(calculate(str));
    }

    public BigDecimal calculate(String mathStr) {
        if (mathStr == null || mathStr.length() == 0) {
            return null;
        }
        // 后缀表达式栈
        LinkedList<String> postfixList = new LinkedList<>();
        // 运算符栈
        Stack<Character> optStack = new Stack<>();
        // 多位数栈
        LinkedList<Character> multiDigitStack = new LinkedList<>();
        char[] arr = mathStr.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char s = arr[i];
            if (Character.isDigit(s) || '.' == s) {
                multiDigitStack.push(s);
            } else {
                // 取出多位数栈中的数据
                if (!multiDigitStack.isEmpty()) {
                    StringBuilder temp = new StringBuilder();
                    while (!multiDigitStack.isEmpty()) {
                        temp.append(multiDigitStack.removeLast());
                    }
                    postfixList.push(temp.toString());
                }
            }
            // 如果当前字符是左括号，将其压入运算符栈
            if ('(' == s) {
                optStack.push(s);
            }
            // 如果当前字符为运算符
            else if ('+' == s || '-' == s || '*' == s || '/' == s) {
                if (!optStack.isEmpty()) {
                    char stackTop = optStack.pop();
                    // 当此运算符的优先级高于栈顶元素的时候，则将此运算符压入运算符栈
                    if (compare(s, stackTop)) {
                        optStack.push(stackTop);
                    }
                    // 否则，弹出栈顶运算符到后缀表达式，并且将当前运算符压栈
                    else {
                        postfixList.push(String.valueOf(stackTop));
                    }
                }
                optStack.push(s);
            }
            // 如果当前字符是右括号，反复将运算符栈顶元素弹出到后缀表达式，直到栈顶元素是左括号（为止，并将左括号从栈中弹出丢弃。
            else if (s == ')') {
                while (!optStack.isEmpty()) {
                    char stackTop = optStack.pop();
                    if (stackTop != '(') {
                        postfixList.push(String.valueOf(stackTop));
                    } else {
                        break;
                    }
                }
            }
        }
        // 取出多位数栈中的数据
        if (!multiDigitStack.isEmpty()) {
            StringBuilder temp = new StringBuilder();
            while (!multiDigitStack.isEmpty()) {
                temp.append(multiDigitStack.removeLast());
            }
            postfixList.push(temp.toString());
        }
        while (!optStack.isEmpty()) {
            postfixList.push(String.valueOf(optStack.pop()));
        }
        Collections.reverse(postfixList);
        System.out.println("后缀表达式：" + postfixList.toString());
        // 操作数栈
        Stack<BigDecimal> numStack = new Stack<>();
        while (!postfixList.isEmpty()) {
            String item = postfixList.pop();
            BigDecimal a, b;
            switch (item) {
                case "+":
                    a = numStack.pop();
                    b = numStack.pop();
                    numStack.push(b.add(a));
                    break;
                case "-":
                    a = numStack.pop();
                    b = numStack.pop();
                    numStack.push(b.subtract(a));
                    break;
                case "*":
                    a = numStack.pop();
                    b = numStack.pop();
                    numStack.push(b.multiply(a));
                    break;
                case "/":
                    a = numStack.pop();
                    b = numStack.pop();
                    numStack.push(b.divide(a));
                    break;
                default:
                    numStack.push(new BigDecimal(item));
                    break;
            }
        }
        return numStack.pop();
    }

    /**
     * 比较优先级
     * 返回 true 表示 curr 优先级大于 stackTop
     */
    private static boolean compare(char curr, char stackTop) {
        if (stackTop == '(') { // 左括号会直接入栈，这里是其他运算符与栈顶左括号对比
            return true;
        }
        if (curr == '*' || curr == '/') {
            return stackTop == '+' || stackTop == '-';
        }
        return false;
    }

    @Test
    public void stackToString() {
        Stack<String> stack = new Stack<>();
        stack.push("a");
        stack.push("b");
        stack.push("c");
        System.out.println(stack.toString());
        stack.clear();
        System.out.println(stack.toString());
    }
}
