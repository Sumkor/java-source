package com.sumkor.formula;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Stack;

/**
 * TransferTest 的增强版，支持多位数、小数点
 *
 * @author Sumkor
 * @since 2021/7/6
 */
public class TransferTest {

    @Test
    public void calculateTest() {
//        String str = "5-1*(5+6)+2";
//        String str = "50-1*(5+6)+2";
//        String str = "(50.5-1)*(5+6)+2";
//        String str = "1+2*(3-4*(5+6))";
//        String str = "1/2*(3-4*(5+6))";
        String str = "43*(2+1)+2*32+98";
        System.out.println(calculate(str));
    }
    /**
     * 1. 将中缀表达式转后缀表达式，例如 a+b*(c-d) 转为后缀表达式就是 abcd-*+
     * 2. 根据后缀表达式进行计算
     */
    public static BigDecimal calculate(String mathStr) {
        if (mathStr == null || mathStr.length() == 0) {
            return null;
        }
        // 后缀表达式链（LIFO）
        LinkedList<String> postfixList = new LinkedList<>();
        // 运算符栈（FIFO）
        Stack<Character> optStack = new Stack<>();
        // 多位数链（LIFO）
        LinkedList<Character> multiDigitList = new LinkedList<>();
        char[] arr = mathStr.toCharArray();
        for (char c : arr) {
            if (Character.isDigit(c) || '.' == c) {
                multiDigitList.addLast(c);
            } else {
                // 处理当前的运算符之前，先处理多位数链中暂存的数据
                if (!multiDigitList.isEmpty()) {
                    StringBuilder temp = new StringBuilder();
                    while (!multiDigitList.isEmpty()) {
                        temp.append(multiDigitList.removeFirst());
                    }
                    postfixList.addLast(temp.toString());
                }
            }
            // 如果当前字符是左括号，将其压入运算符栈
            if ('(' == c) {
                optStack.push(c);
            }
            // 如果当前字符为运算符
            else if ('+' == c || '-' == c || '*' == c || '/' == c) {
                while (!optStack.isEmpty()) {
                    char stackTop = optStack.pop();
                    // 若当前运算符的优先级高于栈顶元素，则一起入栈
                    if (compare(c, stackTop)) {
                        optStack.push(stackTop);
                        break;
                    }
                    // 否则，弹出栈顶运算符到后缀表达式，继续下一次循环
                    else {
                        postfixList.addLast(String.valueOf(stackTop));
                    }
                }
                optStack.push(c);
            }
            // 如果当前字符是右括号，反复将运算符栈顶元素弹出到后缀表达式，直到栈顶元素是左括号（为止，并将左括号从栈中弹出丢弃。
            else if (c == ')') {
                while (!optStack.isEmpty()) {
                    char stackTop = optStack.pop();
                    if (stackTop != '(') {
                        postfixList.addLast(String.valueOf(stackTop));
                    } else {
                        break;
                    }
                }
            }
        }
        // 遍历结束时，若多位数链中具有数据，说明公式是以数字结尾
        if (!multiDigitList.isEmpty()) {
            StringBuilder temp = new StringBuilder();
            while (!multiDigitList.isEmpty()) {
                temp.append(multiDigitList.removeFirst());
            }
            postfixList.addLast(temp.toString());
        }
        // 遍历结束时，运算符栈若有数据，说明是由括号所致，需要补回去
        while (!optStack.isEmpty()) {
            postfixList.addLast(String.valueOf(optStack.pop()));
        }
//        System.out.println("后缀表达式：" + postfixList.toString());
        // 操作数栈
        Stack<BigDecimal> numStack = new Stack<>();
        while (!postfixList.isEmpty()) {
            String item = postfixList.removeFirst();
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
                    numStack.push(b.divide(a, 2, RoundingMode.HALF_UP));
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
